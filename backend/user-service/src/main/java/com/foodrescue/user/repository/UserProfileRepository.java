package com.foodrescue.user.repository;

import com.foodrescue.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    List<UserProfile> findByIsActiveTrue();

    /**
     * Find active user profiles within a bounding box defined by lat/lng boundaries.
     * This is used as a pre-filter before applying the Haversine distance calculation
     * in the service layer for accurate radius-based queries.
     */
    @Query("SELECT u FROM UserProfile u WHERE u.isActive = true " +
           "AND u.latitude BETWEEN :minLat AND :maxLat " +
           "AND u.longitude BETWEEN :minLng AND :maxLng")
    List<UserProfile> findActiveProfilesWithinBounds(
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng);

    /**
     * Find nearby profiles using the Haversine formula directly in PostgreSQL.
     * Returns profiles within the given radius (in kilometers).
     * Uses the Haversine formula: 6371 * acos(cos(radians(lat1)) * cos(radians(lat2)) *
     * cos(radians(lng2) - radians(lng1)) + sin(radians(lat1)) * sin(radians(lat2)))
     */
    @Query(value = "SELECT * FROM user_profiles u WHERE u.is_active = true " +
           "AND u.latitude IS NOT NULL AND u.longitude IS NOT NULL " +
           "AND (6371 * acos(LEAST(1.0, cos(radians(:lat)) * cos(radians(u.latitude)) * " +
           "cos(radians(u.longitude) - radians(:lng)) + " +
           "sin(radians(:lat)) * sin(radians(u.latitude))))) <= :radiusKm " +
           "ORDER BY (6371 * acos(LEAST(1.0, cos(radians(:lat)) * cos(radians(u.latitude)) * " +
           "cos(radians(u.longitude) - radians(:lng)) + " +
           "sin(radians(:lat)) * sin(radians(u.latitude)))))",
           nativeQuery = true)
    List<UserProfile> findNearbyProfiles(
            @Param("lat") double latitude,
            @Param("lng") double longitude,
            @Param("radiusKm") double radiusKm);

    @Query("SELECT u FROM UserProfile u WHERE u.organizationName LIKE %:name% AND u.isActive = true")
    List<UserProfile> searchByOrganizationName(@Param("name") String name);
}
