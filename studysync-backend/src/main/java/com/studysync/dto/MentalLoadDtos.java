package com.studysync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class MentalLoadDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodayLoadResponse {
        private LocalDate date;
        private Double loadScore;      // 0-100
        private Double threshold;
        private Boolean overloaded;
        private String status;         // "LIGHT", "MODERATE", "HIGH", "OVERLOADED"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoadTrendPoint {
        private LocalDate date;
        private Double loadScore;
        private Double threshold;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoadTrendResponse {
        private List<LoadTrendPoint> points;
        private Double currentThreshold;
    }
}
