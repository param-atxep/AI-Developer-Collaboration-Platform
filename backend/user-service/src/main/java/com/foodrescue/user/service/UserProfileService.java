package com.foodrescue.user.service;

import com.foodrescue.user.dto.RestaurantDto;
import com.foodrescue.user.dto.UpdateProfileRequest;
import com.foodrescue.user.dto.UserProfileDto;

import java.util.List;
import java.util.UUID;

public interface UserProfileService {

    /**
     * Get a user profile by its primary key ID.
     */
    UserProfileDto getProfileById(UUID id);

    /**
     * Get a user profile by the authentication user ID.
     */
    UserProfileDto getProfileByUserId(UUID userId);

    /**
     * Create a new user profile.
     */
    UserProfileDto createProfile(UserProfileDto profileDto);

    /**
     * Update an existing user profile.
     */
    UserProfileDto updateProfile(UUID id, UpdateProfileRequest request);

    /**
     * Deactivate a user profile (soft delete).
     */
    void deactivateProfile(UUID id);

    /**
     * Find nearby restaurants within the given radius (in kilometers).
     */
    List<RestaurantDto> getNearbyRestaurants(double latitude, double longitude, double radiusKm);

    /**
     * Find nearby NGOs within the given radius (in kilometers).
     */
    List<UserProfileDto> getNearbyNgos(double latitude, double longitude, double radiusKm);

    /**
     * Increment the total donations counter for a user profile.
     */
    void incrementDonations(UUID profileId);

    /**
     * Increment the total pickups counter for a user profile.
     */
    void incrementPickups(UUID profileId);

    /**
     * Update the rating for a user profile.
     */
    void updateRating(UUID profileId, double newRating);
}
