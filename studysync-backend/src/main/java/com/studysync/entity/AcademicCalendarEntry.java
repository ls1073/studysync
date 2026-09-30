package com.studysync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "academic_calendar_entries", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "entry_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicCalendarEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "entry_date", nullable = false)
    private LocalDate date;

    @Column(name = "day_order_number")
    private Integer dayOrderNumber; // nullable if holiday

    @Column(name = "is_holiday", nullable = false)
    @Builder.Default
    private Boolean isHoliday = false;
}
