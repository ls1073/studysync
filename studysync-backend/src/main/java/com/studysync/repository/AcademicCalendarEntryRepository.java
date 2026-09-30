package com.studysync.repository;

import com.studysync.entity.AcademicCalendarEntry;
import com.studysync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AcademicCalendarEntryRepository extends JpaRepository<AcademicCalendarEntry, Long> {
    Optional<AcademicCalendarEntry> findByUserAndDate(User user, LocalDate date);
    List<AcademicCalendarEntry> findByUserAndDateBetweenOrderByDateAsc(User user, LocalDate start, LocalDate end);
}
