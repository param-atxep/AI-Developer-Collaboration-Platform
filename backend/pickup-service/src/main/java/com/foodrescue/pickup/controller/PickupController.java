package com.foodrescue.pickup.controller;

import com.foodrescue.pickup.dto.CreatePickupRequest;
import com.foodrescue.pickup.dto.PickupResponse;
import com.foodrescue.pickup.dto.UpdatePickupStatusRequest;
import com.foodrescue.pickup.entity.PickupStatus;
import com.foodrescue.pickup.service.PickupService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/pickups")
@RequiredArgsConstructor
@Slf4j
public class PickupController {

    private final PickupService pickupService;

    /**
     * Schedule a new pickup.
     * POST /api/pickups
     */
    @PostMapping
    public ResponseEntity<PickupResponse> schedulePickup(@Valid @RequestBody CreatePickupRequest request) {
        log.info("POST /api/pickups - Scheduling pickup for food listing [{}]", request.getFoodListingId());
        PickupResponse response = pickupService.schedulePickup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a pickup by its ID.
     * GET /api/pickups/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PickupResponse> getPickupById(@PathVariable UUID id) {
        log.info("GET /api/pickups/{}", id);
        PickupResponse response = pickupService.getPickupById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Update the status of a pickup.
     * PUT /api/pickups/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<PickupResponse> updatePickupStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePickupStatusRequest request) {
        log.info("PUT /api/pickups/{}/status - New status: [{}]", id, request.getStatus());
        PickupResponse response = pickupService.updateStatus(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all pickups for a specific claimer.
     * GET /api/pickups/claimer/{claimerId}
     */
    @GetMapping("/claimer/{claimerId}")
    public ResponseEntity<List<PickupResponse>> getPickupsByClaimerId(@PathVariable UUID claimerId) {
        log.info("GET /api/pickups/claimer/{}", claimerId);
        List<PickupResponse> responses = pickupService.getByClaimerId(claimerId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get all pickups for a specific restaurant.
     * GET /api/pickups/restaurant/{restaurantId}
     */
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<PickupResponse>> getPickupsByRestaurantId(@PathVariable UUID restaurantId) {
        log.info("GET /api/pickups/restaurant/{}", restaurantId);
        List<PickupResponse> responses = pickupService.getByRestaurantId(restaurantId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Complete a pickup.
     * POST /api/pickups/{id}/complete
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<PickupResponse> completePickup(@PathVariable UUID id) {
        log.info("POST /api/pickups/{}/complete", id);
        PickupResponse response = pickupService.completePickup(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel a pickup.
     * POST /api/pickups/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<PickupResponse> cancelPickup(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason) {
        log.info("POST /api/pickups/{}/cancel - Reason: {}", id, reason);
        PickupResponse response = pickupService.cancelPickup(id, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Rate a completed pickup.
     * POST /api/pickups/{id}/rate
     */
    @PostMapping("/{id}/rate")
    public ResponseEntity<PickupResponse> ratePickup(
            @PathVariable UUID id,
            @RequestParam Integer rating,
            @RequestParam(required = false) String feedback) {
        log.info("POST /api/pickups/{}/rate - Rating: [{}]", id, rating);
        PickupResponse response = pickupService.ratePickup(id, rating, feedback);
        return ResponseEntity.ok(response);
    }

    /**
     * Get pickups by status.
     * GET /api/pickups/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PickupResponse>> getPickupsByStatus(@PathVariable PickupStatus status) {
        log.info("GET /api/pickups/status/{}", status);
        List<PickupResponse> responses = pickupService.getByStatus(status);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get pickup by food listing ID.
     * GET /api/pickups/food-listing/{foodListingId}
     */
    @GetMapping("/food-listing/{foodListingId}")
    public ResponseEntity<PickupResponse> getPickupByFoodListingId(@PathVariable UUID foodListingId) {
        log.info("GET /api/pickups/food-listing/{}", foodListingId);
        PickupResponse response = pickupService.getByFoodListingId(foodListingId);
        return ResponseEntity.ok(response);
    }

    // ==================== Exception Handlers ====================

    @org.springframework.web.bind.annotation.ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
