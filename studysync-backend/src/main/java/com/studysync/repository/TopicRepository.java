package com.studysync.repository;

import com.studysync.entity.Subject;
import com.studysync.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findBySubject(Subject subject);
    List<Topic> findBySubjectGoalIdAndCompletedFalseOrderByIdAsc(Long goalId);
    List<Topic> findBySubjectGoalIdAndCompletedFalseOrderByWeightDescIdAsc(Long goalId);
    long countBySubjectGoalId(Long goalId);
    long countBySubjectGoalIdAndCompletedTrue(Long goalId);
}
