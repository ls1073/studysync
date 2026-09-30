package com.studysync.service;

import com.studysync.dto.GoalDtos.*;
import com.studysync.entity.*;
import com.studysync.exception.BadRequestException;
import com.studysync.exception.ResourceNotFoundException;
import com.studysync.repository.GoalRepository;
import com.studysync.repository.SubjectRepository;
import com.studysync.repository.TaskRepository;
import com.studysync.repository.TopicRepository;
import com.studysync.repository.UserCapacityProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final TaskRepository taskRepository;
    private final GoalTemplateService goalTemplateService;
    private final SchedulerService schedulerService;
    private final UserCapacityProfileRepository capacityProfileRepository;

    @Transactional
    public GoalResponse createGoal(User user, CreateGoalRequest request) {
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();

        if (request.getTargetDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Target date must be in the future");
        }
        if (startDate.isAfter(request.getTargetDate())) {
            throw new BadRequestException("Start date must be before the target date");
        }

        Goal goal = Goal.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(startDate)
                .targetDate(request.getTargetDate())
                .mode(request.getMode())
                .status(GoalStatus.ACTIVE)
                .build();

        if (request.getMode() == GoalMode.AUTO) {
            if (request.getTemplateName() == null || request.getTemplateName().isBlank()) {
                throw new BadRequestException("templateName is required for AUTO mode");
            }
            GoalTemplate template = goalTemplateService.findByNameOrThrow(request.getTemplateName());
            goal.setSourceTemplate(template);
            goal = goalRepository.save(goal);
            copyTemplateIntoGoal(template, goal);
        } else {
            if (request.getSubjects() == null || request.getSubjects().isEmpty()) {
                throw new BadRequestException("At least one subject is required for MANUAL mode");
            }
            goal = goalRepository.save(goal);
            int created = copyManualIntoGoal(request.getSubjects(), goal);
            if (created == 0) {
                throw new BadRequestException("Please fill in at least one subject with at least one topic before creating the goal.");
            }
        }

        goal.setThresholdAtLastGeneration(currentThreshold(user));
        goalRepository.save(goal);

        int tasksCreated = schedulerService.generateInitialSchedule(goal);

        if (tasksCreated == 0) {
            throw new BadRequestException(
                    "No tasks could be scheduled -- your Academic Calendar doesn't have any Day-Orders " +
                    "(or free hours) mapped between " + startDate + " and " + request.getTargetDate() +
                    ". Please go to Timetable Setup and fill in your calendar for this date range, then try again."
            );
        }

        return getGoalDetail(user, goal.getId());
    }

    public List<GoalSummaryResponse> listGoals(User user) {
        List<GoalSummaryResponse> result = new ArrayList<>();
        for (Goal g : goalRepository.findByUserOrderByCreatedAtDesc(user)) {
            result.add(toSummary(g));
        }
        return result;
    }

    public GoalResponse getGoalDetail(User user, Long goalId) {
        Goal goal = goalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        return toDetail(goal);
    }

    /**
     * Deletes a goal and everything created because of it: its tasks (deleted
     * explicitly first since Task->Goal isn't a JPA-cascaded collection), then
     * the goal itself (which cascades its Subjects and Topics automatically).
     * Nothing outside this goal -- timetable, calendar, other goals -- is touched.
     */
    @Transactional
    public void deleteGoal(User user, Long goalId) {
        Goal goal = goalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        taskRepository.deleteByGoal(goal);
        goalRepository.delete(goal);
    }

    private void copyTemplateIntoGoal(GoalTemplate template, Goal goal) {
        for (TemplateSubject ts : template.getSubjects()) {
            Subject subject = Subject.builder()
                    .goal(goal)
                    .name(ts.getName())
                    .build();
            subject = subjectRepository.save(subject);

            List<Topic> topics = new ArrayList<>();
            for (TemplateTopic tt : ts.getTopics()) {
                // Auto-generated goals don't have per-topic importance defined in the
                // curated templates, so each topic inherits its parent subject's weight.
                topics.add(Topic.builder()
                        .subject(subject)
                        .name(tt.getName())
                        .estimatedHours(tt.getEstimatedHours())
                        .hoursRemaining(tt.getEstimatedHours())
                        .weight(ts.getWeight())
                        .completed(false)
                        .build());
            }
            topicRepository.saveAll(topics);
        }
    }

    /** Returns the number of topics actually created (blank subjects/topics are skipped). */
    private int copyManualIntoGoal(List<ManualSubjectRequest> subjectRequests, Goal goal) {
        int totalTopicsCreated = 0;

        for (ManualSubjectRequest sr : subjectRequests) {
            if (sr.getName() == null || sr.getName().isBlank()) continue;
            if (sr.getTopics() == null || sr.getTopics().isEmpty()) continue;

            List<ManualTopicRequest> validTopics = sr.getTopics().stream()
                    .filter(t -> t.getName() != null && !t.getName().isBlank())
                    .toList();
            if (validTopics.isEmpty()) continue;

            Subject subject = Subject.builder()
                    .goal(goal)
                    .name(sr.getName().trim())
                    .build();
            subject = subjectRepository.save(subject);

            List<Topic> topics = new ArrayList<>();
            for (ManualTopicRequest tr : validTopics) {
                double hours = tr.getEstimatedHours() != null && tr.getEstimatedHours() > 0 ? tr.getEstimatedHours() : 2.0;
                double weight = tr.getWeight() != null ? tr.getWeight() : 2.0;
                topics.add(Topic.builder()
                        .subject(subject)
                        .name(tr.getName().trim())
                        .estimatedHours(hours)
                        .hoursRemaining(hours)
                        .weight(weight)
                        .completed(false)
                        .build());
            }
            topicRepository.saveAll(topics);
            totalTopicsCreated += topics.size();
        }

        return totalTopicsCreated;
    }

    private double currentThreshold(User user) {
        return capacityProfileRepository.findByUser(user)
                .map(UserCapacityProfile::getCurrentThreshold)
                .orElse(65.0);
    }

    private GoalSummaryResponse toSummary(Goal g) {
        int daysRemaining = (int) Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), g.getTargetDate()));
        double percent = computePercentComplete(g);
        return new GoalSummaryResponse(g.getId(), g.getTitle(), g.getStartDate(), g.getTargetDate(), g.getStatus(), daysRemaining, percent);
    }

    private GoalResponse toDetail(Goal g) {
        List<SubjectResponse> subjectResponses = new ArrayList<>();
        for (Subject s : subjectRepository.findByGoal(g)) {
            List<TopicResponse> topicResponses = new ArrayList<>();
            for (Topic t : topicRepository.findBySubject(s)) {
                topicResponses.add(new TopicResponse(t.getId(), t.getName(), t.getEstimatedHours(), t.getHoursRemaining(), t.getWeight(), t.getCompleted()));
            }
            subjectResponses.add(new SubjectResponse(s.getId(), s.getName(), topicResponses));
        }

        int daysRemaining = (int) Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), g.getTargetDate()));
        double percent = computePercentComplete(g);

        return new GoalResponse(g.getId(), g.getTitle(), g.getDescription(), g.getStartDate(), g.getTargetDate(),
                g.getMode(), g.getStatus(), daysRemaining, percent, subjectResponses);
    }

    private double computePercentComplete(Goal g) {
        long total = taskRepository.countByGoalAndIsRestBlockFalse(g);
        if (total == 0) return 0.0;
        long done = taskRepository.countByGoalAndStatusAndIsRestBlockFalse(g, TaskStatus.DONE);
        return Math.round((done * 10000.0 / total)) / 100.0;
    }

    /**
     * Called after a topic is completed. If every topic under this goal is now
     * done, the goal is marked COMPLETED -- no more tasks will be generated or
     * regenerated for it (the Scheduler checks goal status before doing anything).
     */
    @Transactional
    public void checkAndMarkCompletedIfAllTopicsDone(Goal goal) {
        if (goal.getStatus() != GoalStatus.ACTIVE) return;

        long totalTopics = topicRepository.countBySubjectGoalId(goal.getId());
        long completedTopics = topicRepository.countBySubjectGoalIdAndCompletedTrue(goal.getId());
        if (totalTopics > 0 && completedTopics >= totalTopics) {
            goal.setStatus(GoalStatus.COMPLETED);
            goalRepository.save(goal);
        }
    }

    /**
     * Runs daily: any ACTIVE goal whose target date has already passed is marked
     * COMPLETED regardless of topic progress -- once the deadline is gone there's
     * no point continuing to generate tasks for it.
     */
    @Scheduled(cron = "0 10 0 * * *")
    @Transactional
    public void autoCompleteExpiredGoals() {
        List<Goal> expired = goalRepository.findByStatusAndTargetDateBefore(GoalStatus.ACTIVE, LocalDate.now());
        for (Goal g : expired) {
            g.setStatus(GoalStatus.COMPLETED);
        }
        if (!expired.isEmpty()) {
            goalRepository.saveAll(expired);
        }
    }
}
