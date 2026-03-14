package com.foodrescue.notification.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration using STOMP sub-protocol over SockJS.
 * <p>
 * - Endpoint: /ws (with SockJS fallback)
 * - Application destination prefix: /app
 * - Simple broker destinations: /topic (broadcast), /queue (point-to-point)
 * - User destination prefix: /user (for user-targeted messages)
 * <p>
 * Clients connect to ws://host:8086/ws and subscribe to
 * /user/{userId}/queue/notifications to receive personal notifications.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable a simple in-memory message broker for /topic and /queue destinations
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix for messages bound for @MessageMapping-annotated methods
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific destinations (e.g., /user/{userId}/queue/notifications)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the STOMP WebSocket endpoint with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                        "http://localhost:3000",
                        "http://localhost:3001",
                        "http://localhost:8080"
                )
                .withSockJS();

        // Also register without SockJS for native WebSocket clients
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                        "http://localhost:3000",
                        "http://localhost:3001",
                        "http://localhost:8080"
                );
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Register the JWT authentication interceptor for inbound STOMP messages
        registration.interceptors(webSocketAuthInterceptor);
    }
}
