package com.studysync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class GoalTemplateDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateTopicResponse {
        private String name;
        private Double estimatedHours;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateSubjectResponse {
        private String name;
        private Double weight;
        private List<TemplateTopicResponse> topics;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateSummaryResponse {
        private Long id;
        private String name;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateDetailResponse {
        private Long id;
        private String name;
        private String description;
        private List<TemplateSubjectResponse> subjects;
    }
}
