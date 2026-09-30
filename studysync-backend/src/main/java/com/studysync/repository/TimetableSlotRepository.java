package com.studysync.repository;

import com.studysync.entity.DayOrderTemplate;
import com.studysync.entity.TimetableSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, Long> {
    List<TimetableSlot> findByDayOrderTemplateOrderByStartTimeAsc(DayOrderTemplate dayOrderTemplate);
    void deleteByDayOrderTemplate(DayOrderTemplate dayOrderTemplate);
}
