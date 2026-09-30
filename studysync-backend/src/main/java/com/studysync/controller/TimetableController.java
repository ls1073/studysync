package com.studysync.controller;

import com.studysync.config.CurrentUserProvider;
import com.studysync.dto.TimetableDtos.*;
import com.studysync.service.TimetableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping("/day-order")
    public ResponseEntity<DayOrderResponse> createOrUpdateDayOrder(@Valid @RequestBody DayOrderRequest request) {
        return ResponseEntity.ok(timetableService.createOrUpdateDayOrder(currentUserProvider.getCurrentUser(), request));
    }

    @GetMapping("/day-order")
    public ResponseEntity<List<DayOrderResponse>> getAllDayOrders() {
        return ResponseEntity.ok(timetableService.getAllDayOrders(currentUserProvider.getCurrentUser()));
    }

    @DeleteMapping("/day-order/{id}")
    public ResponseEntity<Void> deleteDayOrder(@PathVariable Long id) {
        timetableService.deleteDayOrder(currentUserProvider.getCurrentUser(), id);
        return ResponseEntity.noContent().build();
    }
}
