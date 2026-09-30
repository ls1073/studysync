package com.studysync.controller;

import com.studysync.config.CurrentUserProvider;
import com.studysync.dto.CalendarDtos.*;
import com.studysync.service.CalendarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping("/entry")
    public ResponseEntity<EntryResponse> setSingleEntry(@Valid @RequestBody SingleEntryRequest request) {
        return ResponseEntity.ok(calendarService.setSingleEntry(currentUserProvider.getCurrentUser(), request));
    }

    @PostMapping("/bulk-range")
    public ResponseEntity<List<EntryResponse>> setBulkRange(@Valid @RequestBody BulkRangeRequest request) {
        return ResponseEntity.ok(calendarService.setBulkRange(currentUserProvider.getCurrentUser(), request));
    }

    @GetMapping
    public ResponseEntity<List<EntryResponse>> getRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(calendarService.getRange(currentUserProvider.getCurrentUser(), start, end));
    }
}
