package com.studysync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class DashboardDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardResponse {
        private List<TaskDtos.TaskResponse> todayTasks;
        private List<GoalDtos.GoalSummaryResponse> activeGoals;
        private MentalLoadDtos.TodayLoadResponse mentalLoad;
        private long unreadNotifications;
        private double overallCompletionRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationResponse {
        private Long id;
        private String type;
        private String message;
        private Boolean isRead;
        private String createdAt;
    }
}
