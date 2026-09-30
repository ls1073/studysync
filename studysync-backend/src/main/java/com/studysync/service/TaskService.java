package com.studysync.service;

import com.studysync.dto.TaskDtos.*;
import com.studysync.entity.*;
import com.studysync.exception.BadRequestException;
import com.studysync.exception.ResourceNotFoundException;
import com.studysync.repository.TaskRepository;
import com.studysync.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TopicRepository topicRepository;
    private final MentalLoadService mentalLoadService;
    private final RegenerationService regenerationService;
    private final GoalService goalService;

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksForDate(User user, LocalDate date, Long goalId) {
        List<Task> tasks = goalId != null
                ? taskRepository.findByGoalIdAndDateOrderByStartTimeAsc(goalId, date)
                : taskRepository.findByGoalUserAndDateOrderByStartTimeAsc(user, date);
        return toResponseList(tasks);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getUpcomingTasks(User user, LocalDate from, LocalDate to, Long goalId) {
        List<Task> tasks = goalId != null
                ? taskRepository.findByGoalIdAndDateBetweenOrderByDateAscStartTimeAsc(goalId, from, to)
                : taskRepository.findByGoalUserAndDateBetweenOrderByDateAscStartTimeAsc(user, from, to);
        return toResponseList(tasks);
    }

    /** Fixed: this now correctly returns only PAST tasks (date <= today), not upcoming ones. */
    @Transactional(readOnly = true)
    public List<TaskResponse> getHistory(User user, Long goalId) {
        LocalDate today = LocalDate.now();
        List<Task> tasks = goalId != null
                ? taskRepository.findTop30ByGoalIdAndDateLessThanEqualOrderByDateDescStartTimeDesc(goalId, today)
                : taskRepository.findTop30ByGoalUserAndDateLessThanEqualOrderByDateDescStartTimeDesc(user, today);
        return toResponseList(tasks);
    }

    @Transactional
    public TaskResponse completeTask(User user, Long taskId, CompleteTaskRequest request) {
        Task task = getOwnedTask(user, taskId);

        if (Boolean.TRUE.equals(task.getIsRestBlock())) {
            throw new BadRequestException("Rest blocks don't need to be marked complete");
        }
        if (task.getStatus() != TaskStatus.PENDING) {
            throw new BadRequestException("Only pending tasks can be marked as complete");
        }

        task.setStatus(TaskStatus.DONE);
        task.setEffortRating(request.getEffortRating());
        task.setCompletedAt(LocalDateTime.now());
        task = taskRepository.save(task);

        Topic topic = task.getTopic();
        if (topic != null) {
            double newRemaining = Math.max(0.0, topic.getHoursRemaining() - task.getAllocatedHours());
            topic.setHoursRemaining(newRemaining);
            if (newRemaining <= 0.01) {
                topic.setCompleted(true);
            }
            topicRepository.save(topic);

            if (Boolean.TRUE.equals(topic.getCompleted())) {
                goalService.checkAndMarkCompletedIfAllTopicsDone(task.getGoal());
            }
        }

        mentalLoadService.computeAndLogTodayLoad(user);
        mentalLoadService.recalibrateThreshold(user);

        return toResponse(task);
    }

    @Transactional
    public MissedTaskImpactResponse markMissed(User user, Long taskId) {
        Task task = getOwnedTask(user, taskId);
        if (Boolean.TRUE.equals(task.getIsRestBlock())) {
            throw new BadRequestException("Rest blocks can't be marked missed");
        }
        if (task.getStatus() != TaskStatus.PENDING) {
            throw new BadRequestException("Only pending tasks can be marked as missed");
        }
        MissedTaskImpactResponse response = regenerationService.handleMissedTask(task);
        mentalLoadService.computeAndLogTodayLoad(user);
        mentalLoadService.recalibrateThreshold(user);
        return response;
    }

    @Transactional
    public MissedDayImpactResponse markDayMissed(User user, LocalDate date) {
        List<Task> tasks = taskRepository.findByGoalUserAndDateOrderByStartTimeAsc(user, date).stream()
                .filter(t -> t.getStatus() == TaskStatus.PENDING && !Boolean.TRUE.equals(t.getIsRestBlock()))
                .toList();
        if (tasks.isEmpty()) {
            throw new BadRequestException("No pending tasks to mark missed for " + date);
        }
        MissedDayImpactResponse response = regenerationService.handleMissedDay(user, tasks);
        mentalLoadService.computeAndLogTodayLoad(user);
        mentalLoadService.recalibrateThreshold(user);
        return response;
    }

    /**
     * Runs once daily just after midnight: any real (non-rest) task still PENDING
     * whose date has passed is automatically marked MISSED and triggers the
     * consequence + regeneration flow.
     */
    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void autoDetectMissedTasks() {
        LocalDate today = LocalDate.now();
        List<Task> overdue = taskRepository.findAll().stream()
                .filter(t -> t.getStatus() == TaskStatus.PENDING
                        && t.getDate().isBefore(today)
                        && !Boolean.TRUE.equals(t.getIsRestBlock()))
                .toList();

        for (Task task : overdue) {
            regenerationService.handleMissedTask(task);
        }
    }

    private Task getOwnedTask(User user, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (!task.getGoal().getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Task not found");
        }
        return task;
    }

    private TaskResponse toResponse(Task t) {
        return new TaskResponse(
                t.getId(), t.getGoal().getId(), t.getGoal().getTitle(), t.getTitle(), t.getIsRestBlock(),
                t.getDate(), t.getStartTime(), t.getEndTime(), t.getAllocatedHours(),
                t.getStatus(), t.getEffortRating(), t.getIsRegenerated()
        );
    }

    private List<TaskResponse> toResponseList(List<Task> tasks) {
        List<TaskResponse> result = new ArrayList<>();
        for (Task t : tasks) {
            result.add(toResponse(t));
        }
        return result;
    }
}
