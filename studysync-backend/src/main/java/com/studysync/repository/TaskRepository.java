package com.studysync.repository;

import com.studysync.entity.Goal;
import com.studysync.entity.Task;
import com.studysync.entity.TaskStatus;
import com.studysync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByGoalOrderByDateAscStartTimeAsc(Goal goal);

    List<Task> findByGoalUserAndDateOrderByStartTimeAsc(User user, LocalDate date);

    List<Task> findByGoalUserAndDateBetweenOrderByDateAscStartTimeAsc(User user, LocalDate start, LocalDate end);

    List<Task> findByGoalUserAndStatusAndDateBefore(User user, TaskStatus status, LocalDate date);

    List<Task> findByGoalAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(Goal goal, LocalDate fromDate);

    List<Task> findByGoalUserAndDateOrderByStartTimeAscStatusAsc(User user, LocalDate date);

    long countByGoalAndStatus(Goal goal, TaskStatus status);

    long countByGoal(Goal goal);

    long countByGoalAndStatusAndIsRestBlockFalse(Goal goal, TaskStatus status);

    long countByGoalAndIsRestBlockFalse(Goal goal);

    List<Task> findByGoalUserAndStatusAndDateBeforeAndIsRestBlockFalse(User user, TaskStatus status, LocalDate date);

    // ---- Goal-scoped variants (for the goal switcher) ----

    List<Task> findByGoalIdAndDateOrderByStartTimeAsc(Long goalId, LocalDate date);

    List<Task> findByGoalIdAndDateBetweenOrderByDateAscStartTimeAsc(Long goalId, LocalDate start, LocalDate end);

    List<Task> findTop30ByGoalIdAndDateLessThanEqualOrderByDateDescStartTimeDesc(Long goalId, LocalDate date);

    // ---- History (fixed: only PAST tasks, not future) ----

    List<Task> findTop30ByGoalUserAndDateLessThanEqualOrderByDateDescStartTimeDesc(User user, LocalDate date);

    // ---- Cross-goal conflict prevention: other ACTIVE goals' tasks already
    // occupying real time on a given date, so a new/regenerated goal's schedule
    // never double-books the same slot. MISSED tasks don't count as occupying
    // time (that time is genuinely free again).
    @Query("SELECT t FROM Task t WHERE t.goal.user = :user AND t.date = :date " +
           "AND t.goal.id <> :excludeGoalId AND t.status <> com.studysync.entity.TaskStatus.MISSED " +
           "ORDER BY t.startTime ASC")
    List<Task> findOtherGoalsTasksForDate(@Param("user") User user, @Param("date") LocalDate date,
                                           @Param("excludeGoalId") Long excludeGoalId);

    void deleteByGoal(Goal goal);

    List<Task> findTop20ByGoalUserOrderByDateDesc(User user);
}
