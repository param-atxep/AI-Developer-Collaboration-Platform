package com.foodrescue.food.service;

import com.foodrescue.food.dto.CreateFoodListingRequest;
import com.foodrescue.food.dto.FoodListingResponse;
import com.foodrescue.food.dto.FoodListingSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface FoodListingService {

    /**
     * Create a new food listing and publish a FOOD_LISTED Kafka event.
     */
    FoodListingResponse createListing(CreateFoodListingRequest request);

    /**
     * Retrieve a food listing by its ID.
     */
    FoodListingResponse getListingById(UUID id);

    /**
     * Update an existing food listing.
     */
    FoodListingResponse updateListing(UUID id, CreateFoodListingRequest request);

    /**
     * Claim a food listing (mark as CLAIMED) and publish a FOOD_CLAIMED Kafka event.
     */
    FoodListingResponse claimListing(UUID id);

    /**
     * Cancel a food listing (mark as CANCELLED).
     */
    void cancelListing(UUID id);

    /**
     * Find nearby food listings within a radius using geo-search.
     */
    Page<FoodListingResponse> findNearbyListings(double latitude, double longitude, double radiusKm, Pageable pageable);

    /**
     * Find all listings for a specific restaurant.
     */
    Page<FoodListingResponse> getListingsByRestaurant(UUID restaurantId, Pageable pageable);

    /**
     * Search listings with advanced filtering criteria.
     */
    Page<FoodListingResponse> searchListings(FoodListingSearchCriteria criteria, Pageable pageable);

    /**
     * Automatically expire listings that have passed their expiration time.
     * Publishes FOOD_EXPIRED Kafka events for each expired listing.
     */
    void autoExpireListings();
}
