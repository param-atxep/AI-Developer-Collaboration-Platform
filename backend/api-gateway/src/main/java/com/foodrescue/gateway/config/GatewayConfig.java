package com.foodrescue.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Centralized route configuration for the API Gateway.
 *
 * Defines routing rules that forward incoming requests to the appropriate
 * downstream microservice via Eureka service discovery (lb:// prefix).
 * Each route includes a circuit breaker filter for fault tolerance and
 * path rewriting to strip the gateway prefix before forwarding.
 */
@Configuration
public class GatewayConfig {

    /**
     * Defines all gateway routes mapping URI prefixes to downstream services.
     *
     * Each route is configured with:
     * - A path predicate matching incoming request paths
     * - A circuit breaker filter providing fault tolerance with a fallback
     * - A strip-prefix filter removing the /api/[service] segment before forwarding
     * - A load-balanced URI resolved through Eureka discovery
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // Auth Service: login, registration, token refresh, password reset
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .circuitBreaker(cb -> cb
                                        .setName("authServiceCB")
                                        .setFallbackUri("forward:/fallback/auth"))
                                .stripPrefix(2))
                        .uri("lb://auth-service"))

                // User Service: user profiles, preferences, account management
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .circuitBreaker(cb -> cb
                                        .setName("userServiceCB")
                                        .setFallbackUri("forward:/fallback/user"))
                                .stripPrefix(2))
                        .uri("lb://user-service"))

                // Food Listing Service: food surplus postings, search, availability
                .route("food-listing-service", r -> r
                        .path("/api/food/**")
                        .filters(f -> f
                                .circuitBreaker(cb -> cb
                                        .setName("foodListingServiceCB")
                                        .setFallbackUri("forward:/fallback/food"))
                                .stripPrefix(2))
                        .uri("lb://food-listing-service"))

                // Geolocation Service: proximity search, route optimization, mapping
                .route("geolocation-service", r -> r
                        .path("/api/geo/**")
                        .filters(f -> f
                                .circuitBreaker(cb -> cb
                                        .setName("geolocationServiceCB")
                                        .setFallbackUri("forward:/fallback/geo"))
                                .stripPrefix(2))
                        .uri("lb://geolocation-service"))

                // Pickup Service: scheduling, status tracking, confirmation
                .route("pickup-service", r -> r
                        .path("/api/pickups/**")
                        .filters(f -> f
                                .circuitBreaker(cb -> cb
                                        .setName("pickupServiceCB")
                                        .setFallbackUri("forward:/fallback/pickup"))
                                .stripPrefix(2))
                        .uri("lb://pickup-service"))

                // Notification Service: push notifications, email, SMS alerts
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f
                                .circuitBreaker(cb -> cb
                                        .setName("notificationServiceCB")
                                        .setFallbackUri("forward:/fallback/notification"))
                                .stripPrefix(2))
                        .uri("lb://notification-service"))

                // Analytics Service: waste reduction metrics, reports, dashboards
                .route("analytics-service", r -> r
                        .path("/api/analytics/**")
                        .filters(f -> f
                                .circuitBreaker(cb -> cb
                                        .setName("analyticsServiceCB")
                                        .setFallbackUri("forward:/fallback/analytics"))
                                .stripPrefix(2))
                        .uri("lb://analytics-service"))

                // AI Prediction Service: demand forecasting, waste prediction, matching
                .route("ai-prediction-service", r -> r
                        .path("/api/predictions/**")
                        .filters(f -> f
                                .circuitBreaker(cb -> cb
                                        .setName("aiPredictionServiceCB")
                                        .setFallbackUri("forward:/fallback/predictions"))
                                .stripPrefix(2))
                        .uri("lb://ai-prediction-service"))

                .build();
    }
}
