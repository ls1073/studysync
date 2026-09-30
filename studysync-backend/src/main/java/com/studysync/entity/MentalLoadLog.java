package com.studysync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "mental_load_logs", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "log_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentalLoadLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "log_date", nullable = false)
    private LocalDate date;

    @Column(name = "task_density", nullable = false)
    private Double taskDensity;      // hours allocated / hours free that day

    @Column(name = "delay_ratio", nullable = false)
    private Double delayRatio;       // proportion of tasks started/finished late

    @Column(name = "miss_ratio", nullable = false)
    private Double missRatio;        // proportion of recent tasks missed

    @Column(name = "avg_effort", nullable = false)
    private Double avgEffort;        // average self-reported 1-5 effort

    @Column(name = "load_score", nullable = false)
    private Double loadScore;        // 0-100 computed score

    @Column(name = "threshold_at_time", nullable = false)
    private Double thresholdAtTime;  // student's personal threshold when this was computed

    @Column(name = "was_overloaded", nullable = false)
    private Boolean wasOverloaded;
}
