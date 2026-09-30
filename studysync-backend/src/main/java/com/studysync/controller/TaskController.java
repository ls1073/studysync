package com.studysync.controller;

import com.studysync.config.CurrentUserProvider;
import com.studysync.dto.TaskDtos.*;
import com.studysync.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/today")
    public ResponseEntity<List<TaskResponse>> getToday(@RequestParam(required = false) Long goalId) {
        return ResponseEntity.ok(taskService.getTasksForDate(currentUserProvider.getCurrentUser(), LocalDate.now(), goalId));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<TaskResponse>> getForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long goalId) {
        return ResponseEntity.ok(taskService.getTasksForDate(currentUserProvider.getCurrentUser(), date, goalId));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<TaskResponse>> getUpcoming(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long goalId) {
        return ResponseEntity.ok(taskService.getUpcomingTasks(currentUserProvider.getCurrentUser(), from, to, goalId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<TaskResponse>> getHistory(@RequestParam(required = false) Long goalId) {
        return ResponseEntity.ok(taskService.getHistory(currentUserProvider.getCurrentUser(), goalId));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<TaskResponse> completeTask(@PathVariable Long id,
                                                       @Valid @RequestBody CompleteTaskRequest request) {
        return ResponseEntity.ok(taskService.completeTask(currentUserProvider.getCurrentUser(), id, request));
    }

    @PostMapping("/{id}/miss")
    public ResponseEntity<MissedTaskImpactResponse> markMissed(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.markMissed(currentUserProvider.getCurrentUser(), id));
    }

    @PostMapping("/miss-day")
    public ResponseEntity<MissedDayImpactResponse> markDayMissed(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(taskService.markDayMissed(currentUserProvider.getCurrentUser(), date));
    }
}
