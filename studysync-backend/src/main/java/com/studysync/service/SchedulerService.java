package com.studysync.service;

import com.studysync.entity.*;
import com.studysync.repository.TaskRepository;
import com.studysync.repository.TopicRepository;
import com.studysync.repository.UserCapacityProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Goal-Backward Task Allocator with Rest-Aware, Importance-Ordered, Multi-Goal-
 * Safe Scheduling.
 *
 * Distributes a goal's remaining topic hours across the student's genuinely free
 * hours between the goal's start date and target date. Topics are picked in
 * order of their own importance (weight) first, so high-priority topics get
 * worked on before lower-priority ones -- but a day is not artificially
 * restricted to one topic/subject: once a topic's remaining hours run out
 * partway through a day, the next topic in priority order continues filling
 * that same day's remaining free time.
 *
 * Cross-goal safety: before allocating any day, time already claimed by the
 * student's OTHER active goals on that date is treated as busy too, so two
 * goals can never double-book the same real-world hour.
 *
 * The daily cap is: base capacity (from mental-load threshold) x holiday boost
 * x damping factor (temporarily reduced after a missed task/day). No single
 * continuous study block exceeds the configured cap before a rest block is
 * inserted. Goals that are no longer ACTIVE are never scheduled or regenerated.
 */
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final FreeHourService freeHourService;
    private final TopicRepository topicRepository;
    private final TaskRepository taskRepository;
    private final UserCapacityProfileRepository capacityProfileRepository;

    @Value("${app.scheduling.max-daily-study-hours}")
    private double maxDailyStudyHours;

    @Value("${app.scheduling.min-task-block-hours}")
    private double minTaskBlockHours;

    @Value("${app.scheduling.max-continuous-study-minutes}")
    private int maxContinuousStudyMinutes;

    @Value("${app.scheduling.holiday-load-boost-factor}")
    private double holidayLoadBoostFactor;

    @Transactional
    public int generateInitialSchedule(Goal goal) {
        if (goal.getStatus() != GoalStatus.ACTIVE) return 0;
        LocalDate effectiveStart = goal.getStartDate().isBefore(LocalDate.now()) ? LocalDate.now() : goal.getStartDate();
        return allocate(goal, effectiveStart, false, 1.0, 0);
    }

    @Transactional
    public int regenerateSchedule(Goal goal, LocalDate fromDate) {
        return regenerateSchedule(goal, fromDate, 1.0, 0);
    }

    @Transactional
    public int regenerateSchedule(Goal goal, LocalDate fromDate, double dampingFactor, int dampingDays) {
        if (goal.getStatus() != GoalStatus.ACTIVE) return 0;

        List<Task> futurePending = taskRepository.findByGoalAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(goal, fromDate)
                .stream()
                .filter(t -> t.getStatus() == TaskStatus.PENDING)
                .toList();
        taskRepository.deleteAll(futurePending);

        return allocate(goal, fromDate, true, dampingFactor, dampingDays);
    }

    @Transactional
    public double topUpToday(Goal goal, Topic topic, double hoursNeeded) {
        if (goal.getStatus() != GoalStatus.ACTIVE || topic == null || hoursNeeded <= 0) return 0;

        User user = goal.getUser();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        double dailyCap = getDailyCapHours(user);
        if (freeHourService.isHoliday(user, today)) {
            dailyCap *= holidayLoadBoostFactor;
        }

        // "existingToday" intentionally spans ALL of the user's goals (not just this
        // one) so a same-day top-up can never double-book time another goal already claimed.
        List<Task> existingToday = taskRepository.findByGoalUserAndDateOrderByStartTimeAsc(user, today);
        double alreadyUsedToday = existingToday.stream()
                .filter(t -> t.getStatus() != TaskStatus.MISSED)
                .mapToDouble(Task::getAllocatedHours)
                .sum();
        double remainingBudget = Math.max(0, dailyCap - alreadyUsedToday);
        double toAllocate = Math.min(hoursNeeded, remainingBudget);
        if (toAllocate < minTaskBlockHours) return 0;

        List<FreeSlot> openSlots = subtractOccupiedIntervals(
                freeHourService.computeFreeSlots(user, today), existingToday, now);

        double allocated = 0;
        List<Task> newTasks = new ArrayList<>();
        double maxContinuousHours = maxContinuousStudyMinutes / 60.0;

        for (FreeSlot slot : openSlots) {
            if (allocated >= toAllocate) break;
            double slotHours = slot.hours();
            double thisAlloc = Math.min(Math.min(slotHours, toAllocate - allocated), maxContinuousHours);
            thisAlloc = Math.round(thisAlloc * 4) / 4.0;
            if (thisAlloc < minTaskBlockHours) continue;

            LocalTime end = slot.getStart().plusMinutes((long) (thisAlloc * 60));
            newTasks.add(Task.builder()
                    .goal(goal).topic(topic).title(topic.getName()).isRestBlock(false)
                    .date(today).startTime(slot.getStart()).endTime(end)
                    .allocatedHours(thisAlloc).status(TaskStatus.PENDING).isRegenerated(true)
                    .build());
            allocated += thisAlloc;
        }

        if (!newTasks.isEmpty()) {
            taskRepository.saveAll(newTasks);
        }
        return allocated;
    }

    /**
     * Subtracts a set of already-scheduled tasks' time ranges (and optionally
     * everything before `notBefore`) from a list of raw free slots. Used both for
     * "what's truly open today from right now" and for cross-goal conflict
     * prevention on future days.
     */
    private List<FreeSlot> subtractOccupiedIntervals(List<FreeSlot> raw, List<Task> occupyingTasks, LocalTime notBefore) {
        List<Task> occupying = occupyingTasks.stream().filter(t -> t.getStatus() != TaskStatus.MISSED).toList();

        List<FreeSlot> result = new ArrayList<>();
        for (FreeSlot slot : raw) {
            LocalTime cursor = (notBefore != null && slot.getStart().isBefore(notBefore)) ? notBefore : slot.getStart();
            LocalTime slotEnd = slot.getEnd();
            if (!cursor.isBefore(slotEnd)) continue;

            List<Task> overlapping = occupying.stream()
                    .filter(t -> t.getStartTime().isBefore(slotEnd) && t.getEndTime().isAfter(cursor))
                    .sorted(Comparator.comparing(Task::getStartTime))
                    .toList();

            LocalTime pos = cursor;
            for (Task t : overlapping) {
                LocalTime bStart = t.getStartTime().isBefore(pos) ? pos : t.getStartTime();
                LocalTime bEnd = t.getEndTime().isAfter(slotEnd) ? slotEnd : t.getEndTime();
                if (bStart.isAfter(pos)) result.add(new FreeSlot(pos, bStart));
                if (bEnd.isAfter(pos)) pos = bEnd;
            }
            if (pos.isBefore(slotEnd)) result.add(new FreeSlot(pos, slotEnd));
        }
        return result;
    }

    private int allocate(Goal goal, LocalDate fromDate, boolean markRegenerated, double dampingFactor, int dampingDays) {
        User user = goal.getUser();
        LocalDate targetDate = goal.getTargetDate();

        List<Topic> remainingTopics = topicRepository.findBySubjectGoalIdAndCompletedFalseOrderByWeightDescIdAsc(goal.getId());
        if (remainingTopics.isEmpty()) {
            return 0;
        }

        double baseDailyCapHours = getDailyCapHours(user);
        int restMinutes = getRestBlockMinutes(user);
        double maxContinuousHours = maxContinuousStudyMinutes / 60.0;

        int topicIndex = 0;
        double topicHoursLeftInCurrent = remainingTopics.get(0).getHoursRemaining();
        int tasksCreated = 0;

        LocalDate cursorDate = fromDate;

        while (!cursorDate.isAfter(targetDate) && topicIndex < remainingTopics.size()) {
            double dailyCapHours = baseDailyCapHours;

            if (freeHourService.isHoliday(user, cursorDate)) {
                dailyCapHours *= holidayLoadBoostFactor;
            }
            if (dampingDays > 0 && ChronoUnit.DAYS.between(fromDate, cursorDate) < dampingDays) {
                dailyCapHours *= dampingFactor;
            }

            List<FreeSlot> rawFreeSlots = freeHourService.computeFreeSlots(user, cursorDate);
            List<Task> otherGoalsTasks = taskRepository.findOtherGoalsTasksForDate(user, cursorDate, goal.getId());
            List<FreeSlot> freeSlots = otherGoalsTasks.isEmpty()
                    ? rawFreeSlots
                    : subtractOccupiedIntervals(rawFreeSlots, otherGoalsTasks, null);

            double dayBudget = dailyCapHours;
            int continuousMinutes = 0;

            List<Task> tasksForDay = new ArrayList<>();

            for (FreeSlot slot : freeSlots) {
                double slotRemainingHours = slot.hours();
                LocalTime cursor = slot.getStart();

                while (slotRemainingHours >= minTaskBlockHours && dayBudget >= minTaskBlockHours && topicIndex < remainingTopics.size()) {

                    if (continuousMinutes >= maxContinuousStudyMinutes && slotRemainingHours > 0) {
                        double restHours = Math.min(restMinutes / 60.0, slotRemainingHours);
                        if (restHours * 60 >= 5) {
                            LocalTime restEnd = cursor.plusMinutes((long) (restHours * 60));
                            Task rest = Task.builder()
                                    .goal(goal).topic(null).title("Rest & Recharge").isRestBlock(true)
                                    .date(cursorDate).startTime(cursor).endTime(restEnd)
                                    .allocatedHours(restHours).status(TaskStatus.PENDING)
                                    .isRegenerated(markRegenerated).build();
                            tasksForDay.add(rest);
                            cursor = restEnd;
                            slotRemainingHours -= restHours;
                        }
                        continuousMinutes = 0;
                        if (slotRemainingHours < minTaskBlockHours) break;
                    }

                    Topic currentTopic = remainingTopics.get(topicIndex);
                    double remainingContinuous = maxContinuousHours - (continuousMinutes / 60.0);
                    double allocation = Math.min(Math.min(slotRemainingHours, dayBudget),
                            Math.min(topicHoursLeftInCurrent, remainingContinuous));
                    allocation = Math.round(allocation * 4) / 4.0;
                    if (allocation < minTaskBlockHours) {
                        allocation = Math.min(minTaskBlockHours, Math.min(slotRemainingHours, dayBudget));
                    }
                    if (allocation <= 0) break;

                    LocalTime taskEnd = cursor.plusMinutes((long) (allocation * 60));

                    Task task = Task.builder()
                            .goal(goal).topic(currentTopic).title(currentTopic.getName())
                            .isRestBlock(false).date(cursorDate).startTime(cursor).endTime(taskEnd)
                            .allocatedHours(allocation).status(TaskStatus.PENDING)
                            .isRegenerated(markRegenerated).build();
                    tasksForDay.add(task);
                    tasksCreated++;

                    cursor = taskEnd;
                    slotRemainingHours -= allocation;
                    dayBudget -= allocation;
                    topicHoursLeftInCurrent -= allocation;
                    continuousMinutes += (int) (allocation * 60);

                    if (topicHoursLeftInCurrent <= 0.01) {
                        topicIndex++;
                        if (topicIndex < remainingTopics.size()) {
                            topicHoursLeftInCurrent = remainingTopics.get(topicIndex).getHoursRemaining();
                        }
                    }
                }

                if (dayBudget < minTaskBlockHours) break;
            }

            if (!tasksForDay.isEmpty()) {
                taskRepository.saveAll(tasksForDay);
            }

            cursorDate = cursorDate.plusDays(1);
        }

        return tasksCreated;
    }

    private double getDailyCapHours(User user) {
        double threshold = capacityProfileRepository.findByUser(user)
                .map(UserCapacityProfile::getCurrentThreshold)
                .orElse(65.0);
        double factor = Math.max(0.3, Math.min(1.0, threshold / 100.0));
        return maxDailyStudyHours * factor;
    }

    private int getRestBlockMinutes(User user) {
        double threshold = capacityProfileRepository.findByUser(user)
                .map(UserCapacityProfile::getCurrentThreshold)
                .orElse(65.0);
        if (threshold >= 75) return 15;
        if (threshold >= 55) return 18;
        return 20;
    }
}
