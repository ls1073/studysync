package com.studysync.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;
import java.time.Duration;

@Data
@AllArgsConstructor
public class FreeSlot {
    private LocalTime start;
    private LocalTime end;

    public double hours() {
        return Duration.between(start, end).toMinutes() / 60.0;
    }
}
