package com.foodrescue.geo.service;

import com.foodrescue.geo.dto.GeoSearchRequest;
import com.foodrescue.geo.dto.GeoSearchResult;
import com.foodrescue.geo.entity.GeoLocation;
import com.foodrescue.geo.repository.GeoLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoLocationServiceImpl implements GeoLocationService {

    private final GeoLocationRepository geoLocationRepository;
    private final GoogleMapsService googleMapsService;

    private static final double EARTH_RADIUS_KM = 6371.0;

    @Override
    @Cacheable(value = "nearby-locations", key = "#request.latitude + ':' + #request.longitude + ':' + #request.radiusKm + ':' + #request.entityType + ':' + #request.page + ':' + #request.size")
    public List<GeoSearchResult> findNearby(GeoSearchRequest request) {
        log.info("Searching for nearby locations: lat={}, lng={}, radius={}km, type={}",
                request.getLatitude(), request.getLongitude(),
                request.getRadiusKm(), request.getEntityType());

        int offset = request.getPage() * request.getSize();
        List<Object[]> results;

        if (request.getEntityType() != null) {
            results = geoLocationRepository.findNearbyLocationsByType(
                    request.getLatitude(),
                    request.getLongitude(),
                    request.getRadiusKm(),
                    request.getEntityType().name(),
                    request.getSize(),
                    offset
            );
        } else {
            results = geoLocationRepository.findNearbyLocations(
                    request.getLatitude(),
                    request.getLongitude(),
                    request.getRadiusKm(),
                    request.getSize(),
                    offset
            );
        }

        return results.stream()
                .map(this::mapToGeoSearchResult)
                .toList();
    }

    @Override
    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.round(EARTH_RADIUS_KM * c * 100.0) / 100.0;
    }

    @Override
    @Cacheable(value = "geocode", key = "#address")
    public Map<String, Object> geocodeAddress(String address) {
        log.info("Geocoding address: {}", address);
        return googleMapsService.geocode(address);
    }

    @Override
    @Cacheable(value = "reverse-geocode", key = "#latitude + ':' + #longitude")
    public Map<String, Object> reverseGeocode(double latitude, double longitude) {
        log.info("Reverse geocoding: lat={}, lng={}", latitude, longitude);
        return googleMapsService.reverseGeocode(latitude, longitude);
    }

    @Override
    @Transactional
    @CacheEvict(value = "nearby-locations", allEntries = true)
    public GeoLocation updateLocation(UUID entityId, GeoLocation.EntityType entityType,
                                       double latitude, double longitude,
                                       String address, String city, String state,
                                       String country, String postalCode) {
        log.info("Updating location for entity: id={}, type={}", entityId, entityType);

        GeoLocation geoLocation = geoLocationRepository
                .findByEntityIdAndEntityType(entityId, entityType)
                .orElse(GeoLocation.builder()
                        .entityId(entityId)
                        .entityType(entityType)
                        .build());

        geoLocation.setLatitude(latitude);
        geoLocation.setLongitude(longitude);
        geoLocation.setAddress(address);
        geoLocation.setCity(city);
        geoLocation.setState(state);
        geoLocation.setCountry(country);
        geoLocation.setPostalCode(postalCode);
        geoLocation.setLastUpdated(LocalDateTime.now());

        return geoLocationRepository.save(geoLocation);
    }

    private GeoSearchResult mapToGeoSearchResult(Object[] row) {
        return GeoSearchResult.builder()
                .id((UUID) row[0])
                .entityId((UUID) row[1])
                .entityType(GeoLocation.EntityType.valueOf((String) row[2]))
                .latitude(((Number) row[3]).doubleValue())
                .longitude(((Number) row[4]).doubleValue())
                .address((String) row[5])
                .city((String) row[6])
                .state((String) row[7])
                .country((String) row[8])
                .postalCode((String) row[9])
                .lastUpdated(row[10] instanceof Timestamp ts ? ts.toLocalDateTime() : (LocalDateTime) row[10])
                .distanceKm(((Number) row[11]).doubleValue())
                .build();
    }
}
