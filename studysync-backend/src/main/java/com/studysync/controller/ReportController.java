package com.studysync.controller;

import com.studysync.config.CurrentUserProvider;
import com.studysync.dto.GoalReportDtos.GoalReportResponse;
import com.studysync.dto.ReportDtos.WeeklyReportResponse;
import com.studysync.service.GoalReportService;
import com.studysync.service.WeeklyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final WeeklyReportService weeklyReportService;
    private final GoalReportService goalReportService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/weekly")
    public ResponseEntity<WeeklyReportResponse> getWeeklyReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEnd,
            @RequestParam(required = false) Long goalId) {
        LocalDate end = weekEnd != null ? weekEnd : LocalDate.now();
        LocalDate start = weekStart != null ? weekStart : end.minusDays(6);
        return ResponseEntity.ok(weeklyReportService.generateReport(currentUserProvider.getCurrentUser(), start, end, goalId));
    }

    @GetMapping("/goal/{goalId}")
    public ResponseEntity<GoalReportResponse> getGoalReport(@PathVariable Long goalId) {
        return ResponseEntity.ok(goalReportService.generateReport(currentUserProvider.getCurrentUser(), goalId));
    }
}
