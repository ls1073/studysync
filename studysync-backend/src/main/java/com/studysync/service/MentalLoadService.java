package com.studysync.service;

import com.studysync.dto.MentalLoadDtos.*;
import com.studysync.entity.*;
import com.studysync.repository.MentalLoadLogRepository;
import com.studysync.repository.TaskRepository;
import com.studysync.repository.UserCapacityProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The Mental Load Balancer.
 *
 * Builds a 0-100 daily load score from four signals (task density, delay pattern,
 * miss ratio, self-reported effort), compares it against the student's personal
 * threshold, and self-calibrates that threshold over time using an exponential
 * moving average -- so the system converges on each student's real sustainable
 * capacity instead of applying one fixed rule to everyone.
 */
@Service
@RequiredArgsConstructor
public class MentalLoadService {

    private final TaskRepository taskRepository;
    private final MentalLoadLogRepository mentalLoadLogRepository;
    private final UserCapacityProfileRepository capacityProfileRepository;
    private final FreeHourService freeHourService;

    @Transactional
    public TodayLoadResponse computeAndLogTodayLoad(User user) {
        LocalDate today = LocalDate.now();
        UserCapacityProfile profile = getOrCreateProfile(user);

        List<Task> todaysTasks = taskRepository.findByGoalUserAndDateOrderByStartTimeAsc(user, today).stream()
                .filter(t -> !Boolean.TRUE.equals(t.getIsRestBlock()))
                .toList();

        double freeHoursToday = freeHourService.computeTotalFreeHours(user, today);
        double allocatedHours = todaysTasks.stream().mapToDouble(Task::getAllocatedHours).sum();
        double taskDensity = freeHoursToday <= 0 ? (allocatedHours > 0 ? 1.0 : 0.0)
                : Math.min(1.0, allocatedHours / freeHoursToday);

        // recent history for delay/miss ratios (last 14 days of tasks tied to this user, rest blocks excluded)
        List<Task> recentTasks = taskRepository.findByGoalUserAndDateBetweenOrderByDateAscStartTimeAsc(
                user, today.minusDays(14), today.minusDays(1)).stream()
                .filter(t -> !Boolean.TRUE.equals(t.getIsRestBlock()))
                .toList();

        long recentTotal = recentTasks.size();
        long recentMissed = recentTasks.stream().filter(t -> t.getStatus() == TaskStatus.MISSED).count();
        double missRatio = recentTotal == 0 ? 0.0 : (double) recentMissed / recentTotal;

        // Simplified delay signal: proportion of recent tasks that were missed outright counts as "delay"
        // for this course-project-scope model (real timestamp-vs-slot comparison is a natural extension).
        double delayRatio = recentTotal == 0 ? 0.0 : Math.min(1.0, recentMissed / (double) Math.max(1, recentTotal));

        List<Integer> recentEfforts = new ArrayList<>();
        for (Task t : recentTasks) {
            if (t.getEffortRating() != null) recentEfforts.add(t.getEffortRating());
        }
        double avgEffort = recentEfforts.isEmpty() ? 3.0
                : recentEfforts.stream().mapToInt(Integer::intValue).average().orElse(3.0);

        // weighted composite score, 0-100
        double loadScore = (taskDensity * 40) + (delayRatio * 20) + (missRatio * 25) + ((avgEffort / 5.0) * 15);
        loadScore = Math.min(100.0, Math.max(0.0, loadScore));

        boolean overloaded = loadScore > profile.getCurrentThreshold();

        MentalLoadLog log = mentalLoadLogRepository.findByUserAndDate(user, today)
                .orElseGet(() -> MentalLoadLog.builder().user(user).date(today).build());
        log.setTaskDensity(taskDensity);
        log.setDelayRatio(delayRatio);
        log.setMissRatio(missRatio);
        log.setAvgEffort(avgEffort);
        log.setLoadScore(loadScore);
        log.setThresholdAtTime(profile.getCurrentThreshold());
        log.setWasOverloaded(overloaded);
        mentalLoadLogRepository.save(log);

        return buildResponse(today, loadScore, profile.getCurrentThreshold(), overloaded);
    }

    /**
     * Self-calibration step: called after each completed/missed task. Nudges the
     * student's personal threshold toward their actual sustained load using an
     * exponential moving average, so tomorrow's cap reflects real capacity.
     */
    @Transactional
    public void recalibrateThreshold(User user) {
        UserCapacityProfile profile = getOrCreateProfile(user);
        List<MentalLoadLog> recent = mentalLoadLogRepository.findTop14ByUserOrderByDateDesc(user);
        if (recent.isEmpty()) return;

        double avgRecentLoad = recent.stream().mapToDouble(MentalLoadLog::getLoadScore).average().orElse(profile.getCurrentThreshold());
        double alpha = profile.getSmoothingFactor();

        // If the student is consistently comfortably under threshold, nudge threshold down slightly toward
        // their real average load (avoid over-scheduling); if consistently over, allow a small upward nudge
        // only within safe bounds -- capacity threshold never exceeds 90 to keep a safety margin.
        double newThreshold = (alpha * avgRecentLoad) + ((1 - alpha) * profile.getCurrentThreshold());
        newThreshold = Math.max(30.0, Math.min(90.0, newThreshold));

        profile.setCurrentThreshold(newThreshold);
        capacityProfileRepository.save(profile);
    }

    public LoadTrendResponse getTrend(User user, LocalDate start, LocalDate end) {
        List<LoadTrendPoint> points = new ArrayList<>();
        for (MentalLoadLog log : mentalLoadLogRepository.findByUserAndDateBetweenOrderByDateAsc(user, start, end)) {
            points.add(new LoadTrendPoint(log.getDate(), log.getLoadScore(), log.getThresholdAtTime()));
        }
        double currentThreshold = getOrCreateProfile(user).getCurrentThreshold();
        return new LoadTrendResponse(points, currentThreshold);
    }

    private UserCapacityProfile getOrCreateProfile(User user) {
        return capacityProfileRepository.findByUser(user)
                .orElseGet(() -> capacityProfileRepository.save(
                        UserCapacityProfile.builder().user(user).currentThreshold(65.0).smoothingFactor(0.2).build()));
    }

    private TodayLoadResponse buildResponse(LocalDate date, double score, double threshold, boolean overloaded) {
        String status;
        if (score < 40) status = "LIGHT";
        else if (score < 65) status = "MODERATE";
        else if (score <= threshold) status = "HIGH";
        else status = "OVERLOADED";

        return new TodayLoadResponse(date, Math.round(score * 100) / 100.0, threshold, overloaded, status);
    }
}
