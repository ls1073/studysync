package com.studysync.service;

import com.studysync.dto.DashboardDtos.DashboardResponse;
import com.studysync.dto.GoalDtos.GoalSummaryResponse;
import com.studysync.dto.TaskDtos.TaskResponse;
import com.studysync.entity.Goal;
import com.studysync.entity.GoalStatus;
import com.studysync.entity.Task;
import com.studysync.entity.TaskStatus;
import com.studysync.entity.User;
import com.studysync.repository.GoalRepository;
import com.studysync.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TaskRepository taskRepository;
    private final GoalRepository goalRepository;
    private final MentalLoadService mentalLoadService;
    private final NotificationService notificationService;

    /**
     * @param goalId when provided, "today's tasks" is scoped to just that goal
     *               (the goal switcher). Mental load and the active-goals list
     *               stay whole-student (one brain, one capacity), regardless.
     */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(User user, Long goalId) {
        LocalDate today = LocalDate.now();

        List<Task> todaysRawTasks = goalId != null
                ? taskRepository.findByGoalIdAndDateOrderByStartTimeAsc(goalId, today)
                : taskRepository.findByGoalUserAndDateOrderByStartTimeAsc(user, today);

        List<TaskResponse> todayTasks = new ArrayList<>();
        for (Task t : todaysRawTasks) {
            todayTasks.add(new TaskResponse(t.getId(), t.getGoal().getId(), t.getGoal().getTitle(), t.getTitle(), t.getIsRestBlock(),
                    t.getDate(), t.getStartTime(), t.getEndTime(), t.getAllocatedHours(),
                    t.getStatus(), t.getEffortRating(), t.getIsRegenerated()));
        }

        List<GoalSummaryResponse> activeGoals = new ArrayList<>();
        List<Goal> goals = goalRepository.findByUserAndStatus(user, GoalStatus.ACTIVE);
        long totalTasksAllGoals = 0;
        long doneTasksAllGoals = 0;

        for (Goal g : goals) {
            long total = taskRepository.countByGoalAndIsRestBlockFalse(g);
            long done = taskRepository.countByGoalAndStatusAndIsRestBlockFalse(g, TaskStatus.DONE);
            totalTasksAllGoals += total;
            doneTasksAllGoals += done;

            int daysRemaining = (int) Math.max(0, ChronoUnit.DAYS.between(today, g.getTargetDate()));
            double percent = total == 0 ? 0.0 : Math.round((done * 10000.0 / total)) / 100.0;
            activeGoals.add(new GoalSummaryResponse(g.getId(), g.getTitle(), g.getStartDate(), g.getTargetDate(), g.getStatus(), daysRemaining, percent));
        }

        double overallCompletionRate = totalTasksAllGoals == 0 ? 0.0
                : Math.round((doneTasksAllGoals * 10000.0 / totalTasksAllGoals)) / 100.0;

        var mentalLoad = mentalLoadService.computeAndLogTodayLoad(user);
        long unread = notificationService.countUnread(user);

        return new DashboardResponse(todayTasks, activeGoals, mentalLoad, unread, overallCompletionRate);
    }
}
