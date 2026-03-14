package com.foodrescue.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantDto {

    private UUID id;
    private UUID userProfileId;
    private String cuisineType;
    private String operatingHours;
    private Double foodSafetyRating;
    private String licenseNumber;
    private Double averageWastePerDay;

    // Denormalized fields from UserProfile for convenience
    private String organizationName;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double rating;
    private String profileImageUrl;
    private Double distanceKm;
}
