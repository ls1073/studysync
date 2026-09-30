package com.studysync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "template_subjects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_template_id", nullable = false)
    private GoalTemplate goalTemplate;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    @Builder.Default
    private Double weight = 1.0;

    @OneToMany(mappedBy = "templateSubject", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TemplateTopic> topics = new ArrayList<>();
}
