package com.foodrescue.food.controller;

import com.foodrescue.food.dto.CreateFoodListingRequest;
import com.foodrescue.food.dto.FoodListingResponse;
import com.foodrescue.food.dto.FoodListingSearchCriteria;
import com.foodrescue.food.entity.FoodCategory;
import com.foodrescue.food.entity.FoodStatus;
import com.foodrescue.food.service.FoodListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/food")
@RequiredArgsConstructor
@Slf4j
public class FoodListingController {

    private final FoodListingService foodListingService;

    /**
     * POST /api/food - Create a new food listing.
     */
    @PostMapping
    public ResponseEntity<FoodListingResponse> createListing(
            @Valid @RequestBody CreateFoodListingRequest request) {
        log.info("REST request to create food listing: title={}", request.getTitle());
        FoodListingResponse response = foodListingService.createListing(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/food/{id} - Get a food listing by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FoodListingResponse> getListingById(@PathVariable UUID id) {
        log.info("REST request to get food listing: id={}", id);
        FoodListingResponse response = foodListingService.getListingById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/food/nearby - Find nearby food listings within a specified radius.
     */
    @GetMapping("/nearby")
    public ResponseEntity<Page<FoodListingResponse>> getNearbyListings(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5.0") double radius,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to find nearby listings: lat={}, lng={}, radius={}km", lat, lng, radius);

        Pageable pageable = PageRequest.of(page, size);
        Page<FoodListingResponse> response = foodListingService.findNearbyListings(lat, lng, radius, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/food/restaurant/{restaurantId} - Get all listings for a restaurant.
     */
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<Page<FoodListingResponse>> getListingsByRestaurant(
            @PathVariable UUID restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to get listings for restaurant: restaurantId={}", restaurantId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<FoodListingResponse> response = foodListingService.getListingsByRestaurant(restaurantId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/food/{id} - Update an existing food listing.
     */
    @PutMapping("/{id}")
    public ResponseEntity<FoodListingResponse> updateListing(
            @PathVariable UUID id,
            @Valid @RequestBody CreateFoodListingRequest request) {
        log.info("REST request to update food listing: id={}", id);
        FoodListingResponse response = foodListingService.updateListing(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/food/{id}/claim - Claim a food listing.
     */
    @PostMapping("/{id}/claim")
    public ResponseEntity<FoodListingResponse> claimListing(@PathVariable UUID id) {
        log.info("REST request to claim food listing: id={}", id);
        FoodListingResponse response = foodListingService.claimListing(id);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/food/{id} - Cancel a food listing.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelListing(@PathVariable UUID id) {
        log.info("REST request to cancel food listing: id={}", id);
        foodListingService.cancelListing(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/food/search - Search food listings with advanced filtering criteria.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<FoodListingResponse>> searchListings(
            @RequestParam(required = false) FoodCategory category,
            @RequestParam(required = false) FoodStatus status,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(required = false) Boolean vegetarian,
            @RequestParam(required = false) Boolean vegan,
            @RequestParam(required = false) Boolean halal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to search food listings with criteria");

        FoodListingSearchCriteria criteria = FoodListingSearchCriteria.builder()
                .category(category)
                .status(status)
                .lat(lat)
                .lng(lng)
                .radiusKm(radiusKm)
                .vegetarian(vegetarian)
                .vegan(vegan)
                .halal(halal)
                .build();

        Pageable pageable = PageRequest.of(page, size);
        Page<FoodListingResponse> response = foodListingService.searchListings(criteria, pageable);
        return ResponseEntity.ok(response);
    }
}
