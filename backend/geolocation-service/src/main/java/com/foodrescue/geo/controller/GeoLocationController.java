package com.foodrescue.geo.controller;

import com.foodrescue.geo.dto.GeoSearchRequest;
import com.foodrescue.geo.dto.GeoSearchResult;
import com.foodrescue.geo.entity.GeoLocation;
import com.foodrescue.geo.service.GeoLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
public class GeoLocationController {

    private final GeoLocationService geoLocationService;

    /**
     * GET /api/geo/nearby - Find nearby geo locations.
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<GeoSearchResult>> findNearby(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radiusKm,
            @RequestParam(required = false) GeoLocation.EntityType entityType,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        GeoSearchRequest request = GeoSearchRequest.builder()
                .latitude(latitude)
                .longitude(longitude)
                .radiusKm(radiusKm)
                .entityType(entityType)
                .page(page)
                .size(size)
                .build();

        List<GeoSearchResult> results = geoLocationService.findNearby(request);
        return ResponseEntity.ok(results);
    }

    /**
     * POST /api/geo/geocode - Convert address to coordinates.
     */
    @PostMapping("/geocode")
    public ResponseEntity<Map<String, Object>> geocode(@RequestBody Map<String, String> body) {
        String address = body.get("address");
        if (address == null || address.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Address is required"));
        }
        Map<String, Object> result = geoLocationService.geocodeAddress(address);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/geo/reverse-geocode - Convert coordinates to address.
     */
    @PostMapping("/reverse-geocode")
    public ResponseEntity<Map<String, Object>> reverseGeocode(@RequestBody Map<String, Double> body) {
        Double latitude = body.get("latitude");
        Double longitude = body.get("longitude");
        if (latitude == null || longitude == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Latitude and longitude are required"));
        }
        Map<String, Object> result = geoLocationService.reverseGeocode(latitude, longitude);
        return ResponseEntity.ok(result);
    }

    /**
     * PUT /api/geo/location - Update or create a geo location for an entity.
     */
    @PutMapping("/location")
    public ResponseEntity<GeoLocation> updateLocation(@Valid @RequestBody LocationUpdateRequest request) {
        GeoLocation updated = geoLocationService.updateLocation(
                request.entityId(),
                request.entityType(),
                request.latitude(),
                request.longitude(),
                request.address(),
                request.city(),
                request.state(),
                request.country(),
                request.postalCode()
        );
        return ResponseEntity.ok(updated);
    }

    /**
     * GET /api/geo/distance - Calculate distance between two points.
     */
    @GetMapping("/distance")
    public ResponseEntity<Map<String, Object>> calculateDistance(
            @RequestParam double lat1,
            @RequestParam double lng1,
            @RequestParam double lat2,
            @RequestParam double lng2) {

        double distance = geoLocationService.calculateDistance(lat1, lng1, lat2, lng2);
        return ResponseEntity.ok(Map.of(
                "from", Map.of("latitude", lat1, "longitude", lng1),
                "to", Map.of("latitude", lat2, "longitude", lng2),
                "distanceKm", distance
        ));
    }

    /**
     * Request body record for location updates.
     */
    record LocationUpdateRequest(
            UUID entityId,
            GeoLocation.EntityType entityType,
            double latitude,
            double longitude,
            String address,
            String city,
            String state,
            String country,
            String postalCode
    ) {}
}
