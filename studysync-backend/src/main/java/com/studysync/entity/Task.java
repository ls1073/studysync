package com.studysync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic; // nullable - some tasks may be generic catch-up blocks

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "is_rest_block", nullable = false)
    @Builder.Default
    private Boolean isRestBlock = false;

    @Column(name = "task_date", nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "allocated_hours", nullable = false)
    private Double allocatedHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "effort_rating")
    private Integer effortRating; // 1-5, filled by student after completion

    @Column(name = "completed_at")
    private java.time.LocalDateTime completedAt;

    @Column(name = "is_regenerated", nullable = false)
    @Builder.Default
    private Boolean isRegenerated = false;
}
