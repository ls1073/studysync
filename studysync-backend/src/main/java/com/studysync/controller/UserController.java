package com.studysync.controller;

import com.studysync.config.CurrentUserProvider;
import com.studysync.dto.UserDtos.*;
import com.studysync.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile() {
        return ResponseEntity.ok(userService.getProfile(currentUserProvider.getCurrentUser()));
    }

    @PutMapping("/me/commute-distance")
    public ResponseEntity<UserProfileResponse> updateCommuteDistance(@Valid @RequestBody UpdateCommuteRequest request) {
        return ResponseEntity.ok(userService.updateCommuteDistance(currentUserProvider.getCurrentUser(), request));
    }
}
