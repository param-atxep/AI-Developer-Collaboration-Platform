package com.foodrescue.user.controller;

import com.foodrescue.user.dto.RestaurantDto;
import com.foodrescue.user.dto.UpdateProfileRequest;
import com.foodrescue.user.dto.UserProfileDto;
import com.foodrescue.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * GET /api/users/me
     * Retrieves the profile of the currently authenticated user.
     * The user ID is expected to be passed via the X-User-Id header
     * (typically set by the API Gateway after JWT validation).
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUserProfile(
            @RequestHeader("X-User-Id") String userId) {
        log.info("GET /api/users/me for userId={}", userId);
        UserProfileDto profile = userProfileService.getProfileByUserId(UUID.fromString(userId));
        return ResponseEntity.ok(profile);
    }

    /**
     * GET /api/users/{id}
     * Retrieves a user profile by its profile ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDto> getProfileById(@PathVariable UUID id) {
        log.info("GET /api/users/{}", id);
        UserProfileDto profile = userProfileService.getProfileById(id);
        return ResponseEntity.ok(profile);
    }

    /**
     * POST /api/users
     * Creates a new user profile.
     */
    @PostMapping
    public ResponseEntity<UserProfileDto> createProfile(@Valid @RequestBody UserProfileDto profileDto) {
        log.info("POST /api/users - creating profile for userId={}", profileDto.getUserId());
        UserProfileDto created = userProfileService.createProfile(profileDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/users/{id}
     * Updates an existing user profile. Only non-null fields in the request body
     * will be applied (partial update).
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserProfileDto> updateProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("PUT /api/users/{}", id);
        UserProfileDto updated = userProfileService.updateProfile(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/users/{id}
     * Soft-deletes (deactivates) a user profile.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateProfile(@PathVariable UUID id) {
        log.info("DELETE /api/users/{}", id);
        userProfileService.deactivateProfile(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/users/restaurants/nearby?lat=&lng=&radius=
     * Finds restaurants near the given coordinates within the specified radius (km).
     * Defaults to 10 km radius if not specified.
     */
    @GetMapping("/restaurants/nearby")
    public ResponseEntity<List<RestaurantDto>> getNearbyRestaurants(
            @RequestParam("lat") double latitude,
            @RequestParam("lng") double longitude,
            @RequestParam(value = "radius", defaultValue = "10.0") double radiusKm) {
        log.info("GET /api/users/restaurants/nearby lat={}, lng={}, radius={}km", latitude, longitude, radiusKm);
        List<RestaurantDto> restaurants = userProfileService.getNearbyRestaurants(latitude, longitude, radiusKm);
        return ResponseEntity.ok(restaurants);
    }

    /**
     * GET /api/users/ngos/nearby?lat=&lng=&radius=
     * Finds NGOs near the given coordinates within the specified radius (km).
     * Defaults to 10 km radius if not specified.
     */
    @GetMapping("/ngos/nearby")
    public ResponseEntity<List<UserProfileDto>> getNearbyNgos(
            @RequestParam("lat") double latitude,
            @RequestParam("lng") double longitude,
            @RequestParam(value = "radius", defaultValue = "10.0") double radiusKm) {
        log.info("GET /api/users/ngos/nearby lat={}, lng={}, radius={}km", latitude, longitude, radiusKm);
        List<UserProfileDto> ngos = userProfileService.getNearbyNgos(latitude, longitude, radiusKm);
        return ResponseEntity.ok(ngos);
    }

    /**
     * PUT /api/users/{id}/stats/donations
     * Increments the total donations counter for a user profile.
     * Intended to be called by other microservices (e.g., donation-service).
     */
    @PutMapping("/{id}/stats/donations")
    public ResponseEntity<Void> incrementDonations(@PathVariable UUID id) {
        log.info("PUT /api/users/{}/stats/donations", id);
        userProfileService.incrementDonations(id);
        return ResponseEntity.ok().build();
    }

    /**
     * PUT /api/users/{id}/stats/pickups
     * Increments the total pickups counter for a user profile.
     * Intended to be called by other microservices (e.g., pickup-service).
     */
    @PutMapping("/{id}/stats/pickups")
    public ResponseEntity<Void> incrementPickups(@PathVariable UUID id) {
        log.info("PUT /api/users/{}/stats/pickups", id);
        userProfileService.incrementPickups(id);
        return ResponseEntity.ok().build();
    }

    /**
     * PUT /api/users/{id}/rating?value=
     * Updates the rating for a user profile.
     */
    @PutMapping("/{id}/rating")
    public ResponseEntity<Void> updateRating(
            @PathVariable UUID id,
            @RequestParam("value") double rating) {
        log.info("PUT /api/users/{}/rating value={}", id, rating);
        userProfileService.updateRating(id, rating);
        return ResponseEntity.ok().build();
    }
}
