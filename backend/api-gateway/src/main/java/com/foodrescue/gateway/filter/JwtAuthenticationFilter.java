package com.foodrescue.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * Global gateway filter that validates JWT tokens on every incoming request.
 *
 * For each request that is not on the exclusion list, this filter:
 * 1. Extracts the Bearer token from the Authorization header
 * 2. Validates the token signature, expiration, and structure
 * 3. Extracts user claims (userId, roles) from the token
 * 4. Passes those claims as headers (X-User-Id, X-User-Roles) to downstream services
 *
 * Requests to authentication endpoints (login, register) are excluded
 * from JWT validation to allow unauthenticated access.
 *
 * Order is set to -100 to ensure this filter runs before routing filters.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String HEADER_USER_EMAIL = "X-User-Email";

    /**
     * Paths that do not require JWT authentication.
     */
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/forgot-password",
            "/actuator/health",
            "/actuator/info"
    );

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.issuer:foodrescue}")
    private String jwtIssuer;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT authentication filter initialized");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip authentication for excluded paths
        if (isExcludedPath(path)) {
            log.debug("Skipping JWT validation for excluded path: {}", path);
            return chain.filter(exchange);
        }

        // Extract Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Missing or malformed Authorization header for path: {}", path);
            return onUnauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            // Parse and validate the JWT token
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(jwtIssuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.getSubject();
            String roles = claims.get("roles", String.class);
            String email = claims.get("email", String.class);

            if (userId == null || userId.isBlank()) {
                log.warn("JWT token missing subject (userId) for path: {}", path);
                return onUnauthorized(exchange, "Invalid token: missing user identity");
            }

            // Mutate the request to add user identity headers for downstream services
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(HEADER_USER_ID, userId)
                    .header(HEADER_USER_ROLES, roles != null ? roles : "")
                    .header(HEADER_USER_EMAIL, email != null ? email : "")
                    .build();

            // Remove the Authorization header before forwarding to prevent
            // downstream services from needing to re-validate
            mutatedRequest = mutatedRequest.mutate()
                    .headers(headers -> headers.remove(HttpHeaders.AUTHORIZATION))
                    .build();

            log.debug("JWT validated for user={} roles={} path={}", userId, roles, path);

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT token for path: {}", path);
            return onUnauthorized(exchange, "Token has expired");
        } catch (JwtException ex) {
            log.warn("Invalid JWT token for path: {} - {}", path, ex.getMessage());
            return onUnauthorized(exchange, "Invalid token");
        } catch (Exception ex) {
            log.error("Unexpected error during JWT validation for path: {}", path, ex);
            return onError(exchange, "Internal authentication error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Determines whether the given request path is excluded from JWT validation.
     * Uses prefix matching so sub-paths under excluded roots are also allowed.
     */
    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Writes a 401 Unauthorized JSON response.
     */
    private Mono<Void> onUnauthorized(ServerWebExchange exchange, String message) {
        return onError(exchange, message, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Writes an error JSON response with the given status code and message.
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getURI().getPath()
        );

        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    /**
     * Ensures this filter runs early in the chain, before routing decisions.
     * Negative order values run before default (0) filters.
     */
    @Override
    public int getOrder() {
        return -100;
    }
}
