package com.studysync.repository;

import com.studysync.entity.User;
import com.studysync.entity.UserCapacityProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCapacityProfileRepository extends JpaRepository<UserCapacityProfile, Long> {
    Optional<UserCapacityProfile> findByUser(User user);
}
