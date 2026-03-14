package com.foodrescue.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway entry point for the AI Food Waste Redistribution Platform.
 *
 * Acts as the single entry point for all client requests, providing:
 * - Dynamic routing to downstream microservices via Eureka discovery
 * - JWT-based authentication and authorization
 * - Redis-backed rate limiting
 * - Circuit breaker resilience with Resilience4j
 * - Request/response logging with correlation IDs
 * - CORS configuration for frontend clients
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
