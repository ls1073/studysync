package com.studysync.controller;

import com.studysync.config.CurrentUserProvider;
import com.studysync.dto.DashboardDtos.DashboardResponse;
import com.studysync.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(@RequestParam(required = false) Long goalId) {
        return ResponseEntity.ok(dashboardService.getDashboard(currentUserProvider.getCurrentUser(), goalId));
    }
}
