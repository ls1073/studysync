package com.studysync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReportDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectBreakdown {
        private String subjectName;
        private double hoursCompleted;
        private int tasksCompleted;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyReportResponse {
        private LocalDate weekStart;
        private LocalDate weekEnd;
        private int totalTasks;
        private int completedTasks;
        private int missedTasks;
        private double completionRate;
        private double totalHoursStudied;
        private double avgMentalLoad;
        private double restHoursTaken;
        private List<SubjectBreakdown> subjectBreakdown;
        private List<String> tips;
    }
}
