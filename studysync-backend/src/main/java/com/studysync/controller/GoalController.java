package com.studysync.controller;

import com.studysync.config.CurrentUserProvider;
import com.studysync.dto.GoalDtos.*;
import com.studysync.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody CreateGoalRequest request) {
        return ResponseEntity.ok(goalService.createGoal(currentUserProvider.getCurrentUser(), request));
    }

    @GetMapping
    public ResponseEntity<List<GoalSummaryResponse>> listGoals() {
        return ResponseEntity.ok(goalService.listGoals(currentUserProvider.getCurrentUser()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoal(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.getGoalDetail(currentUserProvider.getCurrentUser(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
        goalService.deleteGoal(currentUserProvider.getCurrentUser(), id);
        return ResponseEntity.noContent().build();
    }
}
