package com.studysync.controller;

import com.studysync.config.CurrentUserProvider;
import com.studysync.dto.MentalLoadDtos.*;
import com.studysync.service.MentalLoadService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/mental-load")
@RequiredArgsConstructor
public class MentalLoadController {

    private final MentalLoadService mentalLoadService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/today")
    public ResponseEntity<TodayLoadResponse> getToday() {
        return ResponseEntity.ok(mentalLoadService.computeAndLogTodayLoad(currentUserProvider.getCurrentUser()));
    }

    @GetMapping("/trend")
    public ResponseEntity<LoadTrendResponse> getTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(mentalLoadService.getTrend(currentUserProvider.getCurrentUser(), start, end));
    }
}
