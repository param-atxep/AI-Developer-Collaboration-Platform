package com.foodrescue.geo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GoogleMapsService {

    private final WebClient webClient;
    private final String apiKey;

    private static final String GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    public GoogleMapsService(WebClient.Builder webClientBuilder,
                             @Value("${google.maps.api-key}") String apiKey) {
        this.webClient = webClientBuilder
                .baseUrl("https://maps.googleapis.com")
                .build();
        this.apiKey = apiKey;
    }

    /**
     * Geocode an address to latitude/longitude using Google Maps Geocoding API.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> geocode(String address) {
        log.info("Calling Google Maps Geocode API for address: {}", address);

        try {
            Map<String, Object> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/maps/api/geocode/json")
                            .queryParam("address", address)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && "OK".equals(response.get("status"))) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                if (results != null && !results.isEmpty()) {
                    Map<String, Object> firstResult = results.get(0);
                    Map<String, Object> geometry = (Map<String, Object>) firstResult.get("geometry");
                    Map<String, Object> location = (Map<String, Object>) geometry.get("location");

                    Map<String, Object> result = new HashMap<>();
                    result.put("latitude", ((Number) location.get("lat")).doubleValue());
                    result.put("longitude", ((Number) location.get("lng")).doubleValue());
                    result.put("formattedAddress", firstResult.get("formatted_address"));
                    result.put("placeId", firstResult.get("place_id"));
                    return result;
                }
            }

            log.warn("Geocode API returned no results for address: {}", address);
            return Map.of("error", "No results found for the given address");
        } catch (Exception e) {
            log.error("Error calling Google Maps Geocode API: {}", e.getMessage(), e);
            return Map.of("error", "Failed to geocode address: " + e.getMessage());
        }
    }

    /**
     * Reverse geocode latitude/longitude to an address using Google Maps Geocoding API.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> reverseGeocode(double latitude, double longitude) {
        log.info("Calling Google Maps Reverse Geocode API for lat={}, lng={}", latitude, longitude);

        try {
            Map<String, Object> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/maps/api/geocode/json")
                            .queryParam("latlng", latitude + "," + longitude)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && "OK".equals(response.get("status"))) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                if (results != null && !results.isEmpty()) {
                    Map<String, Object> firstResult = results.get(0);

                    Map<String, Object> result = new HashMap<>();
                    result.put("formattedAddress", firstResult.get("formatted_address"));
                    result.put("placeId", firstResult.get("place_id"));
                    result.put("latitude", latitude);
                    result.put("longitude", longitude);

                    // Extract address components
                    List<Map<String, Object>> addressComponents =
                            (List<Map<String, Object>>) firstResult.get("address_components");
                    if (addressComponents != null) {
                        for (Map<String, Object> component : addressComponents) {
                            List<String> types = (List<String>) component.get("types");
                            String longName = (String) component.get("long_name");

                            if (types.contains("locality")) {
                                result.put("city", longName);
                            } else if (types.contains("administrative_area_level_1")) {
                                result.put("state", longName);
                            } else if (types.contains("country")) {
                                result.put("country", longName);
                            } else if (types.contains("postal_code")) {
                                result.put("postalCode", longName);
                            }
                        }
                    }

                    return result;
                }
            }

            log.warn("Reverse geocode API returned no results for lat={}, lng={}", latitude, longitude);
            return Map.of("error", "No results found for the given coordinates");
        } catch (Exception e) {
            log.error("Error calling Google Maps Reverse Geocode API: {}", e.getMessage(), e);
            return Map.of("error", "Failed to reverse geocode: " + e.getMessage());
        }
    }
}
