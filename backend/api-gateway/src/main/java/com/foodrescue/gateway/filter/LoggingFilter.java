package com.foodrescue.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Global gateway filter that logs request and response details with a
 * unique correlation ID for end-to-end traceability.
 *
 * For each request this filter:
 * 1. Generates or reuses a correlation ID (X-Correlation-Id header)
 * 2. Logs the incoming request method, path, and client IP
 * 3. Adds the correlation ID to the outgoing response headers
 * 4. Logs the response status code and request duration on completion
 *
 * The correlation ID is propagated to downstream services via the
 * X-Correlation-Id request header, enabling distributed tracing
 * across the entire microservice call chain.
 *
 * Order is set to -200 to ensure this runs before the JWT filter,
 * so every request (including failed auth) gets logged.
 */
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String REQUEST_START_TIME = "requestStartTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Reuse an existing correlation ID if provided by the caller;
        // otherwise generate a new one for this request.
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Record the start time for duration calculation
        Instant startTime = Instant.now();
        exchange.getAttributes().put(REQUEST_START_TIME, startTime);

        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String query = request.getURI().getQuery();
        String clientIp = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
        String userAgent = request.getHeaders().getFirst(HttpHeaders.USER_AGENT);

        // Log the incoming request
        log.info(">>> Incoming request: correlationId={} method={} path={} query={} clientIp={} userAgent={}",
                correlationId, method, path, query != null ? query : "", clientIp,
                userAgent != null ? userAgent : "unknown");

        // Mutate the request to propagate the correlation ID downstream
        final String finalCorrelationId = correlationId;
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(CORRELATION_ID_HEADER, finalCorrelationId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        return chain.filter(mutatedExchange)
                .then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = mutatedExchange.getResponse();

                    // Add correlation ID to the response so clients can reference it
                    response.getHeaders().add(CORRELATION_ID_HEADER, finalCorrelationId);

                    // Calculate request duration
                    Instant start = mutatedExchange.getAttribute(REQUEST_START_TIME);
                    long durationMs = start != null
                            ? Duration.between(start, Instant.now()).toMillis()
                            : -1;

                    int statusCode = response.getStatusCode() != null
                            ? response.getStatusCode().value()
                            : 0;

                    // Log the outgoing response
                    if (statusCode >= 500) {
                        log.error("<<< Response: correlationId={} method={} path={} status={} durationMs={}",
                                finalCorrelationId, method, path, statusCode, durationMs);
                    } else if (statusCode >= 400) {
                        log.warn("<<< Response: correlationId={} method={} path={} status={} durationMs={}",
                                finalCorrelationId, method, path, statusCode, durationMs);
                    } else {
                        log.info("<<< Response: correlationId={} method={} path={} status={} durationMs={}",
                                finalCorrelationId, method, path, statusCode, durationMs);
                    }
                }));
    }

    /**
     * Ensures this filter runs first among all global filters so that
     * every request -- including those rejected by authentication --
     * is logged with a correlation ID.
     */
    @Override
    public int getOrder() {
        return -200;
    }
}
