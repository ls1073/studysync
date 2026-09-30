package com.studysync.repository;

import com.studysync.entity.GoalTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalTemplateRepository extends JpaRepository<GoalTemplate, Long> {
    Optional<GoalTemplate> findByNameIgnoreCase(String name);
    List<GoalTemplate> findByNameContainingIgnoreCase(String query);
}
