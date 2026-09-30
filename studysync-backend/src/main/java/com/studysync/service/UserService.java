package com.studysync.service;

import com.studysync.dto.UserDtos.*;
import com.studysync.entity.User;
import com.studysync.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserProfileResponse getProfile(User user) {
        return new UserProfileResponse(user.getId(), user.getUsername(), user.getFullName(), user.getCommuteDistanceKm());
    }

    @Transactional
    public UserProfileResponse updateCommuteDistance(User user, UpdateCommuteRequest request) {
        user.setCommuteDistanceKm(request.getCommuteDistanceKm());
        User saved = userRepository.save(user);
        return getProfile(saved);
    }
}
