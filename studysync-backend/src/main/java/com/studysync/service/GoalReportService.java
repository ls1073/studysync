package com.studysync.service;

import com.studysync.dto.GoalReportDtos.*;
import com.studysync.entity.*;
import com.studysync.exception.ResourceNotFoundException;
import com.studysync.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Generates a full report for a single goal spanning its entire start-to-target
 * date range (not just the current week), with a subject-wise breakdown and
 * rule-based improvement suggestions -- useful once a goal is completed, but
 * available at any point to check overall progress.
 */
@Service
@RequiredArgsConstructor
public class GoalReportService {

    private final GoalRepository goalRepository;
    private final TaskRepository taskRepository;
    private final MentalLoadLogRepository mentalLoadLogRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;

    @Transactional(readOnly = true)
    public GoalReportResponse generateReport(User user, Long goalId) {
        Goal goal = goalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        LocalDate reportEnd = LocalDate.now().isBefore(goal.getTargetDate()) ? LocalDate.now() : goal.getTargetDate();
        List<Task> allTasks = taskRepository.findByGoalUserAndDateBetweenOrderByDateAscStartTimeAsc(
                user, goal.getStartDate(), reportEnd);

        List<Task> realTasks = allTasks.stream().filter(t -> !Boolean.TRUE.equals(t.getIsRestBlock())).toList();

        int total = realTasks.size();
        int completed = (int) realTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        int missed = (int) realTasks.stream().filter(t -> t.getStatus() == TaskStatus.MISSED).count();
        double completionRate = total == 0 ? 0.0 : Math.round((completed * 10000.0 / total)) / 100.0;

        double hoursStudied = realTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .mapToDouble(Task::getAllocatedHours)
                .sum();

        List<MentalLoadLog> loadLogs = mentalLoadLogRepository.findByUserAndDateBetweenOrderByDateAsc(
                user, goal.getStartDate(), reportEnd);
        double avgLoad = loadLogs.isEmpty() ? 0.0 : loadLogs.stream().mapToDouble(MentalLoadLog::getLoadScore).average().orElse(0.0);

        List<SubjectTimeBreakdown> breakdown = new ArrayList<>();
        for (Subject subject : subjectRepository.findByGoal(goal)) {
            List<Topic> topics = topicRepository.findBySubject(subject);
            int topicsTotal = topics.size();
            int topicsCompleted = (int) topics.stream().filter(Topic::getCompleted).count();

            double hoursForSubject = realTasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.DONE && t.getTopic() != null
                            && t.getTopic().getSubject() != null
                            && t.getTopic().getSubject().getId().equals(subject.getId()))
                    .mapToDouble(Task::getAllocatedHours)
                    .sum();

            breakdown.add(new SubjectTimeBreakdown(subject.getName(), Math.round(hoursForSubject * 100) / 100.0,
                    topicsCompleted, topicsTotal));
        }

        List<String> suggestions = buildSuggestions(goal, total, completed, missed, completionRate, avgLoad, breakdown);

        return new GoalReportResponse(goal.getTitle(), goal.getStartDate(), goal.getTargetDate(), goal.getStatus(),
                total, completed, missed, completionRate, Math.round(hoursStudied * 100) / 100.0,
                Math.round(avgLoad * 100) / 100.0, breakdown, suggestions);
    }

    private List<String> buildSuggestions(Goal goal, int total, int completed, int missed, double completionRate,
                                           double avgLoad, List<SubjectTimeBreakdown> breakdown) {
        List<String> tips = new ArrayList<>();

        if (total == 0) {
            tips.add("No tasks were recorded for this goal yet.");
            return tips;
        }

        if (goal.getStatus() == GoalStatus.COMPLETED && completionRate >= 90) {
            tips.add("Excellent consistency -- you completed " + completionRate + "% of everything scheduled for this goal.");
        } else if (completionRate < 60) {
            tips.add("Overall completion was " + completionRate + "% for this goal. For your next goal, consider a longer runway or fewer daily hours so the plan is easier to stick to.");
        }

        if (missed > 0 && missed >= completed) {
            tips.add("You missed as many or more tasks than you completed. Try reviewing your Academic Calendar setup to make sure your free-hour estimate was realistic.");
        }

        if (avgLoad >= 70) {
            tips.add("Your average mental load stayed high throughout this goal. Consider a lower daily study cap or a longer timeline for your next goal.");
        }

        Optional<SubjectTimeBreakdown> weakest = breakdown.stream()
                .filter(b -> b.getTopicsTotal() > 0)
                .min(Comparator.comparingDouble(b -> (double) b.getTopicsCompleted() / b.getTopicsTotal()));
        weakest.ifPresent(w -> {
            if (w.getTopicsTotal() > 0 && (double) w.getTopicsCompleted() / w.getTopicsTotal() < 0.7) {
                tips.add("\"" + w.getSubjectName() + "\" had the lowest completion rate among your subjects -- worth revisiting if it's relevant to future goals.");
            }
        });

        if (tips.isEmpty()) {
            tips.add("Solid, steady progress overall on this goal.");
        }

        return tips;
    }
}
