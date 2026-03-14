package com.foodrescue.food.service;

import com.foodrescue.food.dto.CreateFoodListingRequest;
import com.foodrescue.food.dto.FoodListingResponse;
import com.foodrescue.food.dto.FoodListingSearchCriteria;
import com.foodrescue.food.entity.FoodListing;
import com.foodrescue.food.entity.FoodStatus;
import com.foodrescue.food.exception.FoodListingNotFoundException;
import com.foodrescue.food.exception.InvalidFoodListingStateException;
import com.foodrescue.food.kafka.FoodEvent;
import com.foodrescue.food.kafka.FoodEventProducer;
import com.foodrescue.food.repository.FoodListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FoodListingServiceImpl implements FoodListingService {

    private final FoodListingRepository foodListingRepository;
    private final FoodEventProducer foodEventProducer;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "nearbyListings", allEntries = true),
            @CacheEvict(value = "restaurantListings", allEntries = true)
    })
    public FoodListingResponse createListing(CreateFoodListingRequest request) {
        log.info("Creating food listing: title={}, restaurantId={}", request.getTitle(), request.getRestaurantId());

        FoodListing listing = FoodListing.builder()
                .restaurantId(request.getRestaurantId())
                .title(request.getTitle())
                .description(request.getDescription())
                .foodCategory(request.getFoodCategory())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .originalPrice(request.getOriginalPrice())
                .expiresAt(request.getExpiresAt())
                .status(FoodStatus.AVAILABLE)
                .imageUrl(request.getImageUrl())
                .allergens(request.getAllergens())
                .isVegetarian(request.getIsVegetarian() != null ? request.getIsVegetarian() : false)
                .isVegan(request.getIsVegan() != null ? request.getIsVegan() : false)
                .isHalal(request.getIsHalal() != null ? request.getIsHalal() : false)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(request.getAddress())
                .build();

        FoodListing savedListing = foodListingRepository.save(listing);

        log.info("Food listing created: id={}", savedListing.getId());

        // Publish FOOD_LISTED Kafka event
        FoodEvent event = FoodEvent.of(
                "FOOD_LISTED",
                savedListing.getId(),
                savedListing.getRestaurantId(),
                savedListing.getTitle(),
                savedListing.getLatitude(),
                savedListing.getLongitude()
        );
        foodEventProducer.publishFoodListed(event);

        return toResponse(savedListing);
    }

    @Override
    @Cacheable(value = "foodListings", key = "#id")
    public FoodListingResponse getListingById(UUID id) {
        log.debug("Fetching food listing: id={}", id);

        FoodListing listing = foodListingRepository.findById(id)
                .orElseThrow(() -> new FoodListingNotFoundException(
                        "Food listing not found with id: " + id));

        return toResponse(listing);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "foodListings", key = "#id"),
            @CacheEvict(value = "nearbyListings", allEntries = true),
            @CacheEvict(value = "restaurantListings", allEntries = true)
    })
    public FoodListingResponse updateListing(UUID id, CreateFoodListingRequest request) {
        log.info("Updating food listing: id={}", id);

        FoodListing listing = foodListingRepository.findById(id)
                .orElseThrow(() -> new FoodListingNotFoundException(
                        "Food listing not found with id: " + id));

        if (listing.getStatus() != FoodStatus.AVAILABLE) {
            throw new InvalidFoodListingStateException(
                    "Cannot update listing in state: " + listing.getStatus());
        }

        listing.setTitle(request.getTitle());
        listing.setDescription(request.getDescription());
        listing.setFoodCategory(request.getFoodCategory());
        listing.setQuantity(request.getQuantity());
        listing.setUnit(request.getUnit());
        listing.setOriginalPrice(request.getOriginalPrice());
        listing.setExpiresAt(request.getExpiresAt());
        listing.setImageUrl(request.getImageUrl());
        listing.setAllergens(request.getAllergens());
        listing.setIsVegetarian(request.getIsVegetarian() != null ? request.getIsVegetarian() : false);
        listing.setIsVegan(request.getIsVegan() != null ? request.getIsVegan() : false);
        listing.setIsHalal(request.getIsHalal() != null ? request.getIsHalal() : false);
        listing.setLatitude(request.getLatitude());
        listing.setLongitude(request.getLongitude());
        listing.setAddress(request.getAddress());

        FoodListing updatedListing = foodListingRepository.save(listing);

        log.info("Food listing updated: id={}", updatedListing.getId());

        return toResponse(updatedListing);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "foodListings", key = "#id"),
            @CacheEvict(value = "nearbyListings", allEntries = true),
            @CacheEvict(value = "restaurantListings", allEntries = true)
    })
    public FoodListingResponse claimListing(UUID id) {
        log.info("Claiming food listing: id={}", id);

        FoodListing listing = foodListingRepository.findById(id)
                .orElseThrow(() -> new FoodListingNotFoundException(
                        "Food listing not found with id: " + id));

        if (listing.getStatus() != FoodStatus.AVAILABLE) {
            throw new InvalidFoodListingStateException(
                    "Cannot claim listing in state: " + listing.getStatus()
                            + ". Only AVAILABLE listings can be claimed.");
        }

        if (listing.getExpiresAt().isBefore(LocalDateTime.now())) {
            listing.setStatus(FoodStatus.EXPIRED);
            foodListingRepository.save(listing);
            throw new InvalidFoodListingStateException(
                    "Cannot claim listing that has already expired.");
        }

        listing.setStatus(FoodStatus.CLAIMED);
        FoodListing claimedListing = foodListingRepository.save(listing);

        log.info("Food listing claimed: id={}", claimedListing.getId());

        // Publish FOOD_CLAIMED Kafka event
        FoodEvent event = FoodEvent.of(
                "FOOD_CLAIMED",
                claimedListing.getId(),
                claimedListing.getRestaurantId(),
                claimedListing.getTitle(),
                claimedListing.getLatitude(),
                claimedListing.getLongitude()
        );
        foodEventProducer.publishFoodClaimed(event);

        return toResponse(claimedListing);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "foodListings", key = "#id"),
            @CacheEvict(value = "nearbyListings", allEntries = true),
            @CacheEvict(value = "restaurantListings", allEntries = true)
    })
    public void cancelListing(UUID id) {
        log.info("Cancelling food listing: id={}", id);

        FoodListing listing = foodListingRepository.findById(id)
                .orElseThrow(() -> new FoodListingNotFoundException(
                        "Food listing not found with id: " + id));

        if (listing.getStatus() != FoodStatus.AVAILABLE) {
            throw new InvalidFoodListingStateException(
                    "Cannot cancel listing in state: " + listing.getStatus()
                            + ". Only AVAILABLE listings can be cancelled.");
        }

        listing.setStatus(FoodStatus.CANCELLED);
        foodListingRepository.save(listing);

        log.info("Food listing cancelled: id={}", id);
    }

    @Override
    @Cacheable(value = "nearbyListings",
            key = "T(String).format('%s_%s_%s_%s_%s', #latitude, #longitude, #radiusKm, #pageable.pageNumber, #pageable.pageSize)")
    public Page<FoodListingResponse> findNearbyListings(double latitude, double longitude,
                                                         double radiusKm, Pageable pageable) {
        log.debug("Finding nearby listings: lat={}, lng={}, radius={}km", latitude, longitude, radiusKm);

        Page<FoodListing> listings = foodListingRepository.findNearbyListings(
                latitude, longitude, radiusKm, FoodStatus.AVAILABLE.name(), pageable);

        return listings.map(listing -> {
            FoodListingResponse response = toResponse(listing);
            response.setDistanceKm(calculateDistance(latitude, longitude,
                    listing.getLatitude(), listing.getLongitude()));
            return response;
        });
    }

    @Override
    @Cacheable(value = "restaurantListings",
            key = "T(String).format('%s_%s_%s', #restaurantId, #pageable.pageNumber, #pageable.pageSize)")
    public Page<FoodListingResponse> getListingsByRestaurant(UUID restaurantId, Pageable pageable) {
        log.debug("Fetching listings for restaurant: restaurantId={}", restaurantId);

        Page<FoodListing> listings = foodListingRepository.findByRestaurantId(restaurantId, pageable);
        return listings.map(this::toResponse);
    }

    @Override
    public Page<FoodListingResponse> searchListings(FoodListingSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching listings with criteria: {}", criteria);

        String status = (criteria.getStatus() != null)
                ? criteria.getStatus().name()
                : FoodStatus.AVAILABLE.name();

        String category = (criteria.getCategory() != null)
                ? criteria.getCategory().name()
                : null;

        Page<FoodListing> listings = foodListingRepository.searchListings(
                criteria.getLat(),
                criteria.getLng(),
                criteria.getRadiusKm(),
                status,
                category,
                criteria.getVegetarian(),
                criteria.getVegan(),
                criteria.getHalal(),
                pageable
        );

        return listings.map(listing -> {
            FoodListingResponse response = toResponse(listing);
            if (criteria.getLat() != null && criteria.getLng() != null) {
                response.setDistanceKm(calculateDistance(
                        criteria.getLat(), criteria.getLng(),
                        listing.getLatitude(), listing.getLongitude()));
            }
            return response;
        });
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    @Caching(evict = {
            @CacheEvict(value = "foodListings", allEntries = true),
            @CacheEvict(value = "nearbyListings", allEntries = true),
            @CacheEvict(value = "restaurantListings", allEntries = true)
    })
    public void autoExpireListings() {
        log.info("Running auto-expire task for food listings");
        LocalDateTime now = LocalDateTime.now();

        // Fetch expired listings before bulk update so we can publish events
        List<FoodListing> expiredListings = foodListingRepository.findExpiredListings(now);

        if (expiredListings.isEmpty()) {
            log.debug("No expired listings found");
            return;
        }

        int expiredCount = foodListingRepository.markExpiredListings(now);
        log.info("Marked {} listings as expired", expiredCount);

        // Publish FOOD_EXPIRED Kafka events for each expired listing
        for (FoodListing listing : expiredListings) {
            try {
                FoodEvent event = FoodEvent.of(
                        "FOOD_EXPIRED",
                        listing.getId(),
                        listing.getRestaurantId(),
                        listing.getTitle(),
                        listing.getLatitude(),
                        listing.getLongitude()
                );
                foodEventProducer.publishFoodExpired(event);
            } catch (Exception e) {
                log.error("Failed to publish FOOD_EXPIRED event for listing: id={}", listing.getId(), e);
            }
        }
    }

    /**
     * Convert a FoodListing entity to a FoodListingResponse DTO.
     */
    private FoodListingResponse toResponse(FoodListing listing) {
        return FoodListingResponse.builder()
                .id(listing.getId())
                .restaurantId(listing.getRestaurantId())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .foodCategory(listing.getFoodCategory())
                .quantity(listing.getQuantity())
                .unit(listing.getUnit())
                .originalPrice(listing.getOriginalPrice())
                .expiresAt(listing.getExpiresAt())
                .status(listing.getStatus())
                .imageUrl(listing.getImageUrl())
                .allergens(listing.getAllergens())
                .isVegetarian(listing.getIsVegetarian())
                .isVegan(listing.getIsVegan())
                .isHalal(listing.getIsHalal())
                .latitude(listing.getLatitude())
                .longitude(listing.getLongitude())
                .address(listing.getAddress())
                .createdAt(listing.getCreatedAt())
                .updatedAt(listing.getUpdatedAt())
                .build();
    }

    /**
     * Calculate the distance between two geo-coordinates using the Haversine formula.
     *
     * @return distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Earth's radius in kilometers

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.round(R * c * 100.0) / 100.0; // Round to 2 decimal places
    }
}
