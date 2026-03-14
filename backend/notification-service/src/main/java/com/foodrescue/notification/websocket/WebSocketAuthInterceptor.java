package com.foodrescue.notification.websocket;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

/**
 * STOMP channel interceptor that validates JWT tokens during the WebSocket handshake.
 * <p>
 * Clients must include an "Authorization" header with a Bearer token in the STOMP CONNECT frame.
 * If the token is valid, the user principal is set on the session so that user-targeted
 * message routing (e.g., /user/{userId}/queue/notifications) works correctly.
 * <p>
 * If no token is provided or the token is invalid, the connection is rejected.
 */
@Component
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Value("${app.jwt.secret:foodrescue-default-secret-key-change-in-production-env-please}")
    private String jwtSecret;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || authHeader.isBlank()) {
                // Also check query parameters for token (SockJS fallback support)
                authHeader = accessor.getFirstNativeHeader("token");
            }

            if (authHeader != null && !authHeader.isBlank()) {
                String token = authHeader.startsWith("Bearer ")
                        ? authHeader.substring(7)
                        : authHeader;

                try {
                    Claims claims = validateToken(token);
                    String userId = claims.getSubject();

                    if (userId != null && !userId.isBlank()) {
                        // Set the principal so Spring can route user-targeted messages
                        accessor.setUser(new StompPrincipal(userId));
                        log.info("WebSocket connection authenticated for user: {}", userId);
                    } else {
                        log.warn("JWT token has no subject claim, rejecting connection");
                        throw new JwtException("JWT token has no subject claim");
                    }
                } catch (JwtException e) {
                    log.warn("Invalid JWT token during WebSocket handshake: {}", e.getMessage());
                    throw new org.springframework.messaging.MessageDeliveryException(
                            message, "Invalid authentication token");
                }
            } else {
                log.warn("No Authorization header in STOMP CONNECT frame, rejecting connection");
                throw new org.springframework.messaging.MessageDeliveryException(
                        message, "Missing authentication token");
            }
        }

        return message;
    }

    /**
     * Validate the JWT token and extract claims.
     *
     * @param token the raw JWT string
     * @return the parsed claims
     * @throws JwtException if the token is invalid, expired, or malformed
     */
    private Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Simple Principal implementation for STOMP session user identification.
     */
    private record StompPrincipal(String name) implements Principal {

        @Override
        public String getName() {
            return name;
        }
    }
}
