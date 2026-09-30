package com.studysync.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class CalendarDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SingleEntryRequest {
        @NotNull
        private LocalDate date;
        private Integer dayOrderNumber; // null if holiday
        private Boolean isHoliday;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkRangeRequest {
        @NotNull
        private LocalDate startDate;
        @NotNull
        private LocalDate endDate;
        // repeating cycle of day-order numbers, e.g. [1,2,3,4,5] applied Mon-Fri style across the range,
        // skipping any dates the client separately marks as holiday afterward
        @NotNull
        private java.util.List<Integer> cyclePattern;
        private java.util.List<LocalDate> excludeDates; // holidays within the range
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntryResponse {
        private Long id;
        private LocalDate date;
        private Integer dayOrderNumber;
        private Boolean isHoliday;
    }
}
