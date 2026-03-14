package com.foodrescue.food.repository;

import com.foodrescue.food.entity.FoodCategory;
import com.foodrescue.food.entity.FoodListing;
import com.foodrescue.food.entity.FoodStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FoodListingRepository extends JpaRepository<FoodListing, UUID> {

    Page<FoodListing> findByStatus(FoodStatus status, Pageable pageable);

    Page<FoodListing> findByRestaurantId(UUID restaurantId, Pageable pageable);

    Page<FoodListing> findByRestaurantIdAndStatus(UUID restaurantId, FoodStatus status, Pageable pageable);

    Page<FoodListing> findByFoodCategoryAndStatus(FoodCategory category, FoodStatus status, Pageable pageable);

    /**
     * Find food listings within a given radius using the Haversine formula.
     * The formula calculates the great-circle distance between two points on a sphere
     * given their latitudes and longitudes.
     *
     * @param latitude  center point latitude in degrees
     * @param longitude center point longitude in degrees
     * @param radiusKm  search radius in kilometers
     * @param status    food listing status filter
     * @param pageable  pagination parameters
     * @return page of food listings within the specified radius, ordered by distance
     */
    @Query(value = """
            SELECT f.*, (
                6371 * acos(
                    cos(radians(:latitude)) * cos(radians(f.latitude))
                    * cos(radians(f.longitude) - radians(:longitude))
                    + sin(radians(:latitude)) * sin(radians(f.latitude))
                )
            ) AS distance
            FROM food_listings f
            WHERE f.status = :status
            AND (
                6371 * acos(
                    cos(radians(:latitude)) * cos(radians(f.latitude))
                    * cos(radians(f.longitude) - radians(:longitude))
                    + sin(radians(:latitude)) * sin(radians(f.latitude))
                )
            ) <= :radiusKm
            AND f.expires_at > NOW()
            ORDER BY distance ASC
            """,
            countQuery = """
            SELECT COUNT(*) FROM food_listings f
            WHERE f.status = :status
            AND (
                6371 * acos(
                    cos(radians(:latitude)) * cos(radians(f.latitude))
                    * cos(radians(f.longitude) - radians(:longitude))
                    + sin(radians(:latitude)) * sin(radians(f.latitude))
                )
            ) <= :radiusKm
            AND f.expires_at > NOW()
            """,
            nativeQuery = true)
    Page<FoodListing> findNearbyListings(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusKm") double radiusKm,
            @Param("status") String status,
            Pageable pageable);

    /**
     * Advanced search with dynamic filtering using the Haversine formula for geo-search.
     * Supports filtering by category, dietary preferences, and proximity.
     */
    @Query(value = """
            SELECT f.*, (
                6371 * acos(
                    cos(radians(:latitude)) * cos(radians(f.latitude))
                    * cos(radians(f.longitude) - radians(:longitude))
                    + sin(radians(:latitude)) * sin(radians(f.latitude))
                )
            ) AS distance
            FROM food_listings f
            WHERE f.status = :status
            AND f.expires_at > NOW()
            AND (:category IS NULL OR f.food_category = :category)
            AND (:vegetarian IS NULL OR f.is_vegetarian = :vegetarian)
            AND (:vegan IS NULL OR f.is_vegan = :vegan)
            AND (:halal IS NULL OR f.is_halal = :halal)
            AND (
                :latitude IS NULL OR :longitude IS NULL OR :radiusKm IS NULL
                OR (
                    6371 * acos(
                        cos(radians(:latitude)) * cos(radians(f.latitude))
                        * cos(radians(f.longitude) - radians(:longitude))
                        + sin(radians(:latitude)) * sin(radians(f.latitude))
                    )
                ) <= :radiusKm
            )
            ORDER BY distance ASC
            """,
            countQuery = """
            SELECT COUNT(*) FROM food_listings f
            WHERE f.status = :status
            AND f.expires_at > NOW()
            AND (:category IS NULL OR f.food_category = :category)
            AND (:vegetarian IS NULL OR f.is_vegetarian = :vegetarian)
            AND (:vegan IS NULL OR f.is_vegan = :vegan)
            AND (:halal IS NULL OR f.is_halal = :halal)
            AND (
                :latitude IS NULL OR :longitude IS NULL OR :radiusKm IS NULL
                OR (
                    6371 * acos(
                        cos(radians(:latitude)) * cos(radians(f.latitude))
                        * cos(radians(f.longitude) - radians(:longitude))
                        + sin(radians(:latitude)) * sin(radians(f.latitude))
                    )
                ) <= :radiusKm
            )
            """,
            nativeQuery = true)
    Page<FoodListing> searchListings(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusKm") Double radiusKm,
            @Param("status") String status,
            @Param("category") String category,
            @Param("vegetarian") Boolean vegetarian,
            @Param("vegan") Boolean vegan,
            @Param("halal") Boolean halal,
            Pageable pageable);

    /**
     * Find listings that are expiring within the given time window and still have AVAILABLE status.
     */
    @Query("SELECT f FROM FoodListing f WHERE f.status = :status AND f.expiresAt <= :expiryThreshold AND f.expiresAt > :now")
    List<FoodListing> findExpiringSoon(
            @Param("status") FoodStatus status,
            @Param("now") LocalDateTime now,
            @Param("expiryThreshold") LocalDateTime expiryThreshold);

    /**
     * Find all listings that have expired (expiresAt is past) but status is still AVAILABLE.
     */
    @Query("SELECT f FROM FoodListing f WHERE f.status = 'AVAILABLE' AND f.expiresAt <= :now")
    List<FoodListing> findExpiredListings(@Param("now") LocalDateTime now);

    /**
     * Bulk update expired listings to EXPIRED status.
     */
    @Modifying
    @Query("UPDATE FoodListing f SET f.status = 'EXPIRED', f.updatedAt = :now WHERE f.status = 'AVAILABLE' AND f.expiresAt <= :now")
    int markExpiredListings(@Param("now") LocalDateTime now);

    long countByRestaurantIdAndStatus(UUID restaurantId, FoodStatus status);
}
