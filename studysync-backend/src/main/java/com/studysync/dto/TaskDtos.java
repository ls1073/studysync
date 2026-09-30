package com.studysync.dto;

import com.studysync.entity.TaskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

public class TaskDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompleteTaskRequest {
        @Min(1)
        @Max(5)
        private Integer effortRating;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskResponse {
        private Long id;
        private Long goalId;
        private String goalTitle;
        private String title;
        private Boolean isRestBlock;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private Double allocatedHours;
        private TaskStatus status;
        private Integer effortRating;
        private Boolean isRegenerated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MissedTaskImpactResponse {
        private Long taskId;
        private String message;
        private Integer estimatedDelayDays;
        private Boolean planRegenerated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MissedDayImpactResponse {
        private LocalDate date;
        private Integer goalsAffected;
        private Integer tasksMarkedMissed;
        private String message;
    }
}
