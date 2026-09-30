package com.studysync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "goal_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name; // e.g. "UPSC CSE"

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "goalTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TemplateSubject> subjects = new ArrayList<>();
}
