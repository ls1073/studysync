package com.studysync.service;

import com.studysync.dto.GoalTemplateDtos.*;
import com.studysync.entity.GoalTemplate;
import com.studysync.entity.TemplateSubject;
import com.studysync.entity.TemplateTopic;
import com.studysync.exception.ResourceNotFoundException;
import com.studysync.repository.GoalTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalTemplateService {

    private final GoalTemplateRepository goalTemplateRepository;

    public List<TemplateSummaryResponse> searchTemplates(String query) {
        List<GoalTemplate> templates = (query == null || query.isBlank())
                ? goalTemplateRepository.findAll()
                : goalTemplateRepository.findByNameContainingIgnoreCase(query);

        List<TemplateSummaryResponse> result = new ArrayList<>();
        for (GoalTemplate t : templates) {
            result.add(new TemplateSummaryResponse(t.getId(), t.getName(), t.getDescription()));
        }
        return result;
    }

    public TemplateDetailResponse getTemplateDetail(Long id) {
        GoalTemplate template = goalTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal template not found"));
        return toDetail(template);
    }

    GoalTemplate findByNameOrThrow(String name) {
        return goalTemplateRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No template found for '" + name + "'. Please use MANUAL mode to define your own subjects."));
    }

    private TemplateDetailResponse toDetail(GoalTemplate t) {
        List<TemplateSubjectResponse> subjects = new ArrayList<>();
        for (TemplateSubject s : t.getSubjects()) {
            List<TemplateTopicResponse> topics = new ArrayList<>();
            for (TemplateTopic top : s.getTopics()) {
                topics.add(new TemplateTopicResponse(top.getName(), top.getEstimatedHours()));
            }
            subjects.add(new TemplateSubjectResponse(s.getName(), s.getWeight(), topics));
        }
        return new TemplateDetailResponse(t.getId(), t.getName(), t.getDescription(), subjects);
    }
}
