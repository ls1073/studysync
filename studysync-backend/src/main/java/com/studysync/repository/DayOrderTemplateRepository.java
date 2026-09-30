package com.studysync.repository;

import com.studysync.entity.DayOrderTemplate;
import com.studysync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DayOrderTemplateRepository extends JpaRepository<DayOrderTemplate, Long> {
    List<DayOrderTemplate> findByUserOrderByDayOrderNumberAsc(User user);
    Optional<DayOrderTemplate> findByUserAndDayOrderNumber(User user, Integer dayOrderNumber);
}
