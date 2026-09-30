package com.studysync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UserDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateCommuteRequest {
        private Double commuteDistanceKm;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfileResponse {
        private Long id;
        private String username;
        private String fullName;
        private Double commuteDistanceKm;
    }
}
