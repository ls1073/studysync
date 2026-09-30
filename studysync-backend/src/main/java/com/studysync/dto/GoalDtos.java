package com.studysync.dto;

import com.studysync.entity.GoalMode;
import com.studysync.entity.GoalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class GoalDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ManualTopicRequest {
        @NotBlank
        private String name;
        private Double estimatedHours;
        private Double weight; // 1=Low, 2=Medium, 3=High importance -- per-topic now
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ManualSubjectRequest {
        @NotBlank
        private String name;
        private List<ManualTopicRequest> topics;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateGoalRequest {
        @NotBlank
        private String title;
        private String description;
        private LocalDate startDate;
        @NotNull
        private LocalDate targetDate;
        @NotNull
        private GoalMode mode;

        // AUTO mode
        private String templateName;

        // MANUAL mode
        private List<ManualSubjectRequest> subjects;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicResponse {
        private Long id;
        private String name;
        private Double estimatedHours;
        private Double hoursRemaining;
        private Double weight;
        private Boolean completed;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectResponse {
        private Long id;
        private String name;
        private List<TopicResponse> topics;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalResponse {
        private Long id;
        private String title;
        private String description;
        private LocalDate startDate;
        private LocalDate targetDate;
        private GoalMode mode;
        private GoalStatus status;
        private Integer daysRemaining;
        private Double percentComplete;
        private List<SubjectResponse> subjects;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalSummaryResponse {
        private Long id;
        private String title;
        private LocalDate startDate;
        private LocalDate targetDate;
        private GoalStatus status;
        private Integer daysRemaining;
        private Double percentComplete;
    }
}
