package com.foodrescue.user.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @Size(max = 255, message = "Organization name must not exceed 255 characters")
    private String organizationName;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;

    @Size(max = 512, message = "Profile image URL must not exceed 512 characters")
    private String profileImageUrl;

    // Restaurant-specific fields (optional, only applied if user is a restaurant)
    @Size(max = 100, message = "Cuisine type must not exceed 100 characters")
    private String cuisineType;

    @Size(max = 255, message = "Operating hours must not exceed 255 characters")
    private String operatingHours;

    private Double foodSafetyRating;

    @Size(max = 100, message = "License number must not exceed 100 characters")
    private String licenseNumber;

    private Double averageWastePerDay;

    // NGO-specific fields (optional, only applied if user is an NGO)
    @Size(max = 100, message = "Registration number must not exceed 100 characters")
    private String registrationNumber;

    @Size(max = 500, message = "Service area must not exceed 500 characters")
    private String serviceArea;

    private Integer capacity;
}
