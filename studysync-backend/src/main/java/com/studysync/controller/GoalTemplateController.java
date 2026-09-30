package com.studysync.controller;

import com.studysync.dto.GoalTemplateDtos.*;
import com.studysync.service.GoalTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goal-templates")
@RequiredArgsConstructor
public class GoalTemplateController {

    private final GoalTemplateService goalTemplateService;

    @GetMapping
    public ResponseEntity<List<TemplateSummaryResponse>> search(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(goalTemplateService.searchTemplates(query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemplateDetailResponse> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(goalTemplateService.getTemplateDetail(id));
    }
}
