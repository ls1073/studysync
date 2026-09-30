package com.studysync.repository;

import com.studysync.entity.Goal;
import com.studysync.entity.GoalStatus;
import com.studysync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUserOrderByCreatedAtDesc(User user);
    List<Goal> findByUserAndStatus(User user, GoalStatus status);
    Optional<Goal> findByIdAndUser(Long id, User user);
    List<Goal> findByStatusAndTargetDateBefore(GoalStatus status, java.time.LocalDate date);
    List<Goal> findByStatus(GoalStatus status);
}
