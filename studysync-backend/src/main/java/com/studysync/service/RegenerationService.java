package com.studysync.service;

import com.studysync.dto.MentalLoadDtos.TodayLoadResponse;
import com.studysync.dto.TaskDtos.MissedDayImpactResponse;
import com.studysync.dto.TaskDtos.MissedTaskImpactResponse;
import com.studysync.entity.*;
import com.studysync.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Consequence & Regeneration Engine.
 *
 * Differentiates between a single/partial missed task and a fully missed day,
 * and factors the student's CURRENT mental load into how the catch-up plan is
 * rebuilt:
 *  - Partial miss: regenerates from TOMORROW onward only, leaving the rest of
 *    today's already-scheduled tasks untouched (a light-touch adjustment).
 *  - Whole day missed: a stronger signal of struggle -- always applies at
 *    least a moderate "damping" window (a temporarily reduced daily cap for
 *    the next few days) so the catch-up plan doesn't immediately re-cram the
 *    same volume back in.
 *  - In both cases, if the student's mental load is already HIGH/OVERLOADED
 *    at the moment of the miss, damping is made stronger and longer --
 *    mental load is a direct input into how aggressively we reschedule.
 */
@Service
@RequiredArgsConstructor
public class RegenerationService {

    private final TaskRepository taskRepository;
    private final SchedulerService schedulerService;
    private final NotificationService notificationService;
    private final FreeHourService freeHourService;
    private final MentalLoadService mentalLoadService;

    @Transactional
    public MissedTaskImpactResponse handleMissedTask(Task task) {
        Goal goal = task.getGoal();
        User user = goal.getUser();
        Topic topic = task.getTopic();
        double missedHours = task.getAllocatedHours();

        task.setStatus(TaskStatus.MISSED);
        taskRepository.save(task);

        int estimatedDelayDays = estimateDelayDays(goal, missedHours);

        // Only attempt to squeeze the missed hours into today's remaining free time if
        // the student isn't already overloaded right now -- otherwise piling more onto
        // today would work against the whole point of the Mental Load Balancer.
        TodayLoadResponse currentLoad = mentalLoadService.computeAndLogTodayLoad(user);
        double recoveredToday = 0;
        if (topic != null && !"OVERLOADED".equals(currentLoad.getStatus())) {
            recoveredToday = schedulerService.topUpToday(goal, topic, missedHours);
        }
        double hoursStillOwed = Math.max(0, missedHours - recoveredToday);

        Damping damping = computeDamping(user, false);
        if (hoursStillOwed > 0.01) {
            LocalDate regenerateFrom = LocalDate.now().plusDays(1); // preserve today's other pending tasks
            schedulerService.regenerateSchedule(goal, regenerateFrom, damping.factor, damping.days);
        }

        String message;
        if (recoveredToday > 0.01 && hoursStillOwed <= 0.01) {
            message = String.format(
                    "You missed \"%s\" (%.1f hrs), but there was enough free time left today to fully make it up -- " +
                    "check today's schedule for the added session.",
                    task.getTitle(), missedHours
            );
        } else if (recoveredToday > 0.01) {
            message = String.format(
                    "You missed \"%s\" (%.1f hrs). %.1f hrs was fit into today's remaining free time, and the rest " +
                    "was pushed into your upcoming schedule (goal may shift by ~%d day(s)).",
                    task.getTitle(), missedHours, recoveredToday, estimatedDelayDays
            );
        } else {
            message = String.format(
                    "You missed \"%s\" (%.1f hrs). Based on your current free time and mental load capacity, " +
                    "this may push your goal \"%s\" by approximately %d day(s). Your upcoming schedule has been adjusted.",
                    task.getTitle(), missedHours, goal.getTitle(), estimatedDelayDays
            );
        }

        notificationService.create(user, NotificationType.TASK_MISSED, message, task.getId(), goal.getId());
        if (hoursStillOwed > 0.01) {
            notificationService.create(user, NotificationType.PLAN_REGENERATED,
                    "Your study plan for \"" + goal.getTitle() + "\" has been regenerated to redistribute missed work.",
                    null, goal.getId());
        }

        return new MissedTaskImpactResponse(task.getId(), message, estimatedDelayDays, hoursStillOwed > 0.01);
    }

    @Transactional
    public MissedDayImpactResponse handleMissedDay(User user, List<Task> tasksToMiss) {
        Map<Goal, List<Task>> byGoal = tasksToMiss.stream().collect(Collectors.groupingBy(Task::getGoal));
        int totalMarked = 0;

        Damping damping = computeDamping(user, true);

        for (Map.Entry<Goal, List<Task>> entry : byGoal.entrySet()) {
            Goal goal = entry.getKey();
            List<Task> goalTasks = entry.getValue();

            double totalMissedHours = 0;
            for (Task t : goalTasks) {
                t.setStatus(TaskStatus.MISSED);
                totalMissedHours += t.getAllocatedHours();
                totalMarked++;
            }
            taskRepository.saveAll(goalTasks);

            int estimatedDelayDays = estimateDelayDays(goal, totalMissedHours);
            schedulerService.regenerateSchedule(goal, LocalDate.now().plusDays(1), damping.factor, damping.days);

            String message = String.format(
                    "You missed your whole day's plan for \"%s\" (%d tasks, %.1f hrs). This may push your goal " +
                    "by approximately %d day(s). To avoid overloading you, the next %d day(s) have been scheduled " +
                    "a bit lighter than usual before returning to your normal pace.",
                    goal.getTitle(), goalTasks.size(), totalMissedHours, estimatedDelayDays, damping.days
            );
            notificationService.create(user, NotificationType.TASK_MISSED, message, null, goal.getId());
            notificationService.create(user, NotificationType.PLAN_REGENERATED,
                    "Your study plan for \"" + goal.getTitle() + "\" has been regenerated after a missed day.",
                    null, goal.getId());
        }

        return new MissedDayImpactResponse(LocalDate.now(), byGoal.size(), totalMarked,
                "Marked " + totalMarked + " task(s) across " + byGoal.size() + " goal(s) as missed and regenerated your schedule.");
    }

    /**
     * Decides how much (and how long) to lighten the daily study cap during
     * regeneration. A whole-day miss always gets at least moderate damping.
     * Either kind of miss gets stronger damping if the student's mental load
     * was already elevated at the moment of the miss.
     */
    private Damping computeDamping(User user, boolean wholeDayMissed) {
        TodayLoadResponse currentLoad = mentalLoadService.computeAndLogTodayLoad(user);

        double factor = 1.0;
        int days = 0;

        if ("OVERLOADED".equals(currentLoad.getStatus())) {
            factor = 0.75;
            days = 4;
        } else if ("HIGH".equals(currentLoad.getStatus())) {
            factor = 0.85;
            days = 3;
        }

        if (wholeDayMissed) {
            factor = Math.min(factor, 0.85);
            days = Math.max(days, 3);
        }

        return new Damping(factor, days);
    }

    private int estimateDelayDays(Goal goal, double missedHours) {
        User user = goal.getUser();
        LocalDate today = LocalDate.now();

        double avgFreeHoursPerDay = 0;
        int sampledDays = 0;
        for (int i = 0; i < 7 && !today.plusDays(i).isAfter(goal.getTargetDate()); i++) {
            avgFreeHoursPerDay += freeHourService.computeTotalFreeHours(user, today.plusDays(i));
            sampledDays++;
        }
        if (sampledDays == 0 || avgFreeHoursPerDay <= 0) {
            return 1;
        }
        avgFreeHoursPerDay = avgFreeHoursPerDay / sampledDays;
        if (avgFreeHoursPerDay <= 0) return 1;

        int days = (int) Math.ceil(missedHours / avgFreeHoursPerDay);
        return Math.max(1, days);
    }

    private record Damping(double factor, int days) {}
}
