package com.foodrescue.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {

    private UUID id;
    private UUID userId;
    private String organizationName;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private String profileImageUrl;
    private Double rating;
    private Integer totalDonations;
    private Integer totalPickups;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Optional nested details
    private RestaurantDto restaurantDetails;
    private NgoProfileDto ngoDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NgoProfileDto {
        private UUID id;
        private String registrationNumber;
        private String serviceArea;
        private Integer capacity;
        private Integer peopleServed;
    }
}
