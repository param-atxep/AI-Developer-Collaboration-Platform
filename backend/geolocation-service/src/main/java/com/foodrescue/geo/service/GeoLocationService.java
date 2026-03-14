package com.foodrescue.geo.service;

import com.foodrescue.geo.dto.GeoSearchRequest;
import com.foodrescue.geo.dto.GeoSearchResult;
import com.foodrescue.geo.entity.GeoLocation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface GeoLocationService {

    /**
     * Find nearby locations based on search criteria.
     */
    List<GeoSearchResult> findNearby(GeoSearchRequest request);

    /**
     * Calculate distance in kilometers between two geographic points.
     */
    double calculateDistance(double lat1, double lng1, double lat2, double lng2);

    /**
     * Geocode an address string into latitude/longitude coordinates.
     */
    Map<String, Object> geocodeAddress(String address);

    /**
     * Reverse geocode latitude/longitude into a human-readable address.
     */
    Map<String, Object> reverseGeocode(double latitude, double longitude);

    /**
     * Update or create a geo location for an entity.
     */
    GeoLocation updateLocation(UUID entityId, GeoLocation.EntityType entityType,
                                double latitude, double longitude,
                                String address, String city, String state,
                                String country, String postalCode);
}
