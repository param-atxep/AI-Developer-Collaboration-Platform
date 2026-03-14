package com.foodrescue.geo.repository;

import com.foodrescue.geo.entity.GeoLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GeoLocationRepository extends JpaRepository<GeoLocation, UUID> {

    Optional<GeoLocation> findByEntityIdAndEntityType(UUID entityId, GeoLocation.EntityType entityType);

    List<GeoLocation> findByEntityType(GeoLocation.EntityType entityType);

    /**
     * Find nearby locations using the Haversine formula.
     * Returns locations within the specified radius (in kilometers)
     * ordered by distance ascending.
     */
    @Query(value = """
            SELECT g.*,
                   (6371 * acos(
                       cos(radians(:lat)) * cos(radians(g.latitude)) *
                       cos(radians(g.longitude) - radians(:lng)) +
                       sin(radians(:lat)) * sin(radians(g.latitude))
                   )) AS distance_km
            FROM geo_locations g
            WHERE (6371 * acos(
                       cos(radians(:lat)) * cos(radians(g.latitude)) *
                       cos(radians(g.longitude) - radians(:lng)) +
                       sin(radians(:lat)) * sin(radians(g.latitude))
                   )) <= :radius
            ORDER BY distance_km ASC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Object[]> findNearbyLocations(
            @Param("lat") double latitude,
            @Param("lng") double longitude,
            @Param("radius") double radiusKm,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    /**
     * Find nearby locations filtered by entity type using the Haversine formula.
     */
    @Query(value = """
            SELECT g.*,
                   (6371 * acos(
                       cos(radians(:lat)) * cos(radians(g.latitude)) *
                       cos(radians(g.longitude) - radians(:lng)) +
                       sin(radians(:lat)) * sin(radians(g.latitude))
                   )) AS distance_km
            FROM geo_locations g
            WHERE g.entity_type = :entityType
              AND (6371 * acos(
                       cos(radians(:lat)) * cos(radians(g.latitude)) *
                       cos(radians(g.longitude) - radians(:lng)) +
                       sin(radians(:lat)) * sin(radians(g.latitude))
                   )) <= :radius
            ORDER BY distance_km ASC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Object[]> findNearbyLocationsByType(
            @Param("lat") double latitude,
            @Param("lng") double longitude,
            @Param("radius") double radiusKm,
            @Param("entityType") String entityType,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
}
