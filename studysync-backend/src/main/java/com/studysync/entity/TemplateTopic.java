package com.studysync.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "template_topics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_subject_id", nullable = false)
    private TemplateSubject templateSubject;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "estimated_hours", nullable = false)
    @Builder.Default
    private Double estimatedHours = 2.0;
}
