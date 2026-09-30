package com.studysync.service;

import com.studysync.entity.Goal;
import com.studysync.entity.GoalStatus;
import com.studysync.entity.NotificationType;
import com.studysync.entity.User;
import com.studysync.entity.UserCapacityProfile;
import com.studysync.repository.GoalRepository;
import com.studysync.repository.UserCapacityProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Weekly Threshold-Drift Recalibration.
 *
 * This runs alongside (not instead of) the miss-triggered regeneration engine.
 * The two together cover both cases a student actually experiences:
 *  - Miss-triggered regeneration: reactive, immediate, happens the moment a
 *    task/day is missed.
 *  - This weekly job: proactive, happens even if nothing was ever missed, so a
 *    quietly-struggling (or quietly-coasting) student's capacity drift still
 *    gets reflected in their plan without waiting for a failure.
 *
 * Deliberately NOT continuous/real-time -- only checked once a week, and only
 * acts if the student's self-calibrated threshold has moved more than a
 * meaningful amount (15% by default) since the goal's schedule was last built.
 * Only future, not-yet-started tasks are touched; today and anything already
 * completed/missed are never rewritten.
 */
@Service
@RequiredArgsConstructor
public class WeeklyRecalibrationService {

    private final GoalRepository goalRepository;
    private final UserCapacityProfileRepository capacityProfileRepository;
    private final SchedulerService schedulerService;
    private final NotificationService notificationService;

    @Value("${app.scheduling.recalibration-drift-threshold-percent}")
    private double driftThresholdPercent;

    @Scheduled(cron = "${app.scheduling.recalibration-cron}")
    @Transactional
    public void weeklyRecalibrationCheck() {
        List<Goal> activeGoals = goalRepository.findByStatus(GoalStatus.ACTIVE);
        for (Goal goal : activeGoals) {
            recalibrateIfDrifted(goal);
        }
    }

    private void recalibrateIfDrifted(Goal goal) {
        User user = goal.getUser();
        double currentThreshold = capacityProfileRepository.findByUser(user)
                .map(UserCapacityProfile::getCurrentThreshold)
                .orElse(65.0);

        Double lastThreshold = goal.getThresholdAtLastGeneration();
        if (lastThreshold == null || lastThreshold <= 0) {
            goal.setThresholdAtLastGeneration(currentThreshold);
            goalRepository.save(goal);
            return;
        }

        double driftPercent = Math.abs(currentThreshold - lastThreshold) / lastThreshold * 100.0;
        if (driftPercent < driftThresholdPercent) {
            return; // not enough drift to bother -- stability over noise
        }

        boolean gotEasier = currentThreshold > lastThreshold;
        int tasksRegenerated = schedulerService.regenerateSchedule(goal, LocalDate.now().plusDays(1));

        goal.setThresholdAtLastGeneration(currentThreshold);
        goalRepository.save(goal);

        if (tasksRegenerated > 0) {
            String message = String.format(
                    "Your schedule for \"%s\" was gently adjusted based on your recent pace -- your capacity has " +
                    "%s over the past week, so upcoming (not-yet-started) tasks were replanned to match.",
                    goal.getTitle(), gotEasier ? "increased" : "decreased"
            );
            notificationService.create(user, NotificationType.PLAN_REGENERATED, message, null, goal.getId());
        }
    }
}
