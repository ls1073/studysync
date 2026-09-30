package com.studysync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "day_order_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayOrderTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "day_order_number", nullable = false)
    private Integer dayOrderNumber;

    @Column(length = 50)
    private String label;

    @OneToMany(mappedBy = "dayOrderTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TimetableSlot> slots = new ArrayList<>();
}
