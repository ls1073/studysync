package com.studysync.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "topics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "estimated_hours", nullable = false)
    @Builder.Default
    private Double estimatedHours = 2.0;

    @Column(nullable = false)
    @Builder.Default
    private Double weight = 2.0; // 1=Low, 2=Medium, 3=High importance -- drives scheduling priority

    @Column(name = "hours_remaining", nullable = false)
    private Double hoursRemaining;

    @Column(nullable = false)
    @Builder.Default
    private Boolean completed = false;

    @PrePersist
    protected void initRemaining() {
        if (this.hoursRemaining == null) {
            this.hoursRemaining = this.estimatedHours;
        }
    }
}
