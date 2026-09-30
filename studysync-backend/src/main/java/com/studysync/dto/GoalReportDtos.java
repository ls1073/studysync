package com.studysync.dto;

import com.studysync.entity.GoalStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class GoalReportDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectTimeBreakdown {
        private String subjectName;
        private double hoursCompleted;
        private int topicsCompleted;
        private int topicsTotal;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalReportResponse {
        private String goalTitle;
        private LocalDate startDate;
        private LocalDate targetDate;
        private GoalStatus status;
        private int totalTasks;
        private int completedTasks;
        private int missedTasks;
        private double completionRate;
        private double totalHoursStudied;
        private double avgMentalLoad;
        private List<SubjectTimeBreakdown> subjectBreakdown;
        private List<String> suggestions;
    }
}
