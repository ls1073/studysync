package com.studysync.service;

import com.studysync.dto.ReportDtos.*;
import com.studysync.entity.MentalLoadLog;
import com.studysync.entity.Task;
import com.studysync.entity.TaskStatus;
import com.studysync.entity.User;
import com.studysync.repository.MentalLoadLogRepository;
import com.studysync.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Generates a weekly progress report with a subject-wise time breakdown and
 * rule-based (not AI-generated) improvement tips derived directly from the
 * student's own completion, miss, and mental-load data for the week.
 */
@Service
@RequiredArgsConstructor
public class WeeklyReportService {

    private final TaskRepository taskRepository;
    private final MentalLoadLogRepository mentalLoadLogRepository;

    @Transactional(readOnly = true)
    public WeeklyReportResponse generateReport(User user, LocalDate weekStart, LocalDate weekEnd, Long goalId) {
        List<Task> allTasks = goalId != null
                ? taskRepository.findByGoalIdAndDateBetweenOrderByDateAscStartTimeAsc(goalId, weekStart, weekEnd)
                : taskRepository.findByGoalUserAndDateBetweenOrderByDateAscStartTimeAsc(user, weekStart, weekEnd);

        List<Task> realTasks = allTasks.stream().filter(t -> !Boolean.TRUE.equals(t.getIsRestBlock())).toList();
        List<Task> restTasks = allTasks.stream().filter(t -> Boolean.TRUE.equals(t.getIsRestBlock())).toList();

        int total = realTasks.size();
        int completed = (int) realTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        int missed = (int) realTasks.stream().filter(t -> t.getStatus() == TaskStatus.MISSED).count();
        double completionRate = total == 0 ? 0.0 : Math.round((completed * 10000.0 / total)) / 100.0;

        double hoursStudied = realTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .mapToDouble(Task::getAllocatedHours)
                .sum();

        double restHours = restTasks.stream().mapToDouble(Task::getAllocatedHours).sum();

        List<MentalLoadLog> loadLogs = mentalLoadLogRepository.findByUserAndDateBetweenOrderByDateAsc(user, weekStart, weekEnd);
        double avgLoad = loadLogs.isEmpty() ? 0.0 : loadLogs.stream().mapToDouble(MentalLoadLog::getLoadScore).average().orElse(0.0);

        Map<String, double[]> subjectMap = new LinkedHashMap<>(); // name -> [hours, count]
        for (Task t : realTasks) {
            if (t.getStatus() != TaskStatus.DONE || t.getTopic() == null || t.getTopic().getSubject() == null) continue;
            String subjectName = t.getTopic().getSubject().getName();
            subjectMap.putIfAbsent(subjectName, new double[]{0.0, 0.0});
            double[] agg = subjectMap.get(subjectName);
            agg[0] += t.getAllocatedHours();
            agg[1] += 1;
        }
        List<SubjectBreakdown> breakdown = new ArrayList<>();
        for (Map.Entry<String, double[]> e : subjectMap.entrySet()) {
            breakdown.add(new SubjectBreakdown(e.getKey(), Math.round(e.getValue()[0] * 100) / 100.0, (int) e.getValue()[1]));
        }

        List<String> tips = buildTips(total, completed, missed, completionRate, avgLoad, hoursStudied, restHours);

        return new WeeklyReportResponse(weekStart, weekEnd, total, completed, missed, completionRate,
                Math.round(hoursStudied * 100) / 100.0, Math.round(avgLoad * 100) / 100.0,
                Math.round(restHours * 100) / 100.0, breakdown, tips);
    }

    private List<String> buildTips(int total, int completed, int missed, double completionRate,
                                    double avgLoad, double hoursStudied, double restHours) {
        List<String> tips = new ArrayList<>();

        if (total == 0) {
            tips.add("No tasks were scheduled this week yet -- create or continue a goal to start building your study history.");
            return tips;
        }

        if (completionRate >= 80) {
            tips.add("Strong week -- you completed " + completionRate + "% of your scheduled tasks. Keep this pace going.");
        } else if (completionRate < 50) {
            tips.add("Your completion rate was " + completionRate + "% this week. Consider setting fewer, more realistic daily tasks rather than spreading thin.");
        }

        if (missed > completed) {
            tips.add("You missed more tasks than you completed. Try focusing on 1-2 priority subjects at a time instead of juggling everything at once.");
        }

        if (avgLoad >= 75) {
            tips.add("Your average mental load stayed high this week. Your rest blocks may need to be longer -- keep rating task effort honestly so StudySync can recalibrate your capacity.");
        } else if (avgLoad > 0 && avgLoad < 35) {
            tips.add("Your mental load was quite low this week -- you may have room to take on slightly more study volume if you want to accelerate toward your goal.");
        }

        if (hoursStudied > 0 && restHours > 0 && (restHours / hoursStudied) < 0.08) {
            tips.add("You're taking very little rest relative to your study hours. Longer, more frequent breaks often improve retention rather than hurt progress.");
        }

        if (tips.isEmpty()) {
            tips.add("Steady week overall. Keep marking tasks done/missed accurately so your schedule keeps adapting to your real capacity.");
        }

        return tips;
    }
}
