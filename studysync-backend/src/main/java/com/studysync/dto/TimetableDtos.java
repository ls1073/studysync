package com.studysync.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

public class TimetableDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotRequest {
        @NotBlank
        private String subjectName;
        @NotNull
        private LocalTime startTime;
        @NotNull
        private LocalTime endTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayOrderRequest {
        @NotNull
        private Integer dayOrderNumber;
        private String label;
        @Valid
        private List<SlotRequest> slots;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotResponse {
        private Long id;
        private String subjectName;
        private LocalTime startTime;
        private LocalTime endTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayOrderResponse {
        private Long id;
        private Integer dayOrderNumber;
        private String label;
        private List<SlotResponse> slots;
    }
}
