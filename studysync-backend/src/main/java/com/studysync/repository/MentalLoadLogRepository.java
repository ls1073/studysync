package com.studysync.repository;

import com.studysync.entity.MentalLoadLog;
import com.studysync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MentalLoadLogRepository extends JpaRepository<MentalLoadLog, Long> {
    Optional<MentalLoadLog> findByUserAndDate(User user, LocalDate date);
    List<MentalLoadLog> findByUserAndDateBetweenOrderByDateAsc(User user, LocalDate start, LocalDate end);
    List<MentalLoadLog> findTop14ByUserOrderByDateDesc(User user);
}
