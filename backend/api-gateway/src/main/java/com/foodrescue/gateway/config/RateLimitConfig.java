package com.foodrescue.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Redis-backed rate limiting configuration for the API Gateway.
 *
 * Enforces per-user rate limits to protect downstream services from
 * being overwhelmed. Authenticated users are identified by their
 * X-User-Id header (set by JwtAuthenticationFilter); unauthenticated
 * requests fall back to IP-based limiting.
 *
 * Default policy: 1000 requests per minute per user.
 *
 * The Redis Rate Limiter uses the token-bucket algorithm:
 * - replenishRate: how many tokens per second are added to the bucket
 * - burstCapacity: maximum number of tokens the bucket can hold
 * - requestedTokens: how many tokens each request consumes
 */
@Configuration
public class RateLimitConfig {

    @Value("${gateway.rate-limit.replenish-rate:16}")
    private int replenishRate;

    @Value("${gateway.rate-limit.burst-capacity:1000}")
    private int burstCapacity;

    @Value("${gateway.rate-limit.requested-tokens:1}")
    private int requestedTokens;

    /**
     * Creates a RedisRateLimiter using token-bucket algorithm parameters.
     *
     * With replenishRate=16 tokens/sec and burstCapacity=1000, a user
     * can make up to ~960 requests per minute sustained (16 * 60) with
     * burst allowance up to 1000 concurrent tokens, effectively enforcing
     * approximately 1000 requests per minute.
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(replenishRate, burstCapacity, requestedTokens);
    }

    /**
     * Resolves the rate-limit key for each incoming request.
     *
     * Priority:
     * 1. X-User-Id header (set by JWT filter for authenticated requests)
     * 2. Remote client IP address (fallback for unauthenticated requests)
     *
     * This ensures that authenticated users are rate-limited per account,
     * while anonymous requests are rate-limited per source IP.
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isBlank()) {
                return Mono.just("user:" + userId);
            }

            String clientIp = Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                    .getAddress()
                    .getHostAddress();
            return Mono.just("ip:" + clientIp);
        };
    }
}
