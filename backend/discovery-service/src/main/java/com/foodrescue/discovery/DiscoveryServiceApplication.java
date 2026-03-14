package com.foodrescue.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Discovery Service for the AI Food Waste Redistribution Platform.
 *
 * <p>This service acts as the central service registry, enabling all platform
 * microservices (food-listing, pickup, notification, geolocation, ai-prediction,
 * analytics, auth, and api-gateway) to discover and communicate with one another
 * without hard-coded host/port configurations.</p>
 *
 * <p>In production this server should be deployed as a cluster of peer-aware
 * replicas for high availability. See the {@code peer1} / {@code peer2} Spring
 * profiles defined in {@code application.yml}.</p>
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServiceApplication.class, args);
    }
}
