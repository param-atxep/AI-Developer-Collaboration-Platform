package com.foodrescue.notification.websocket;

import com.foodrescue.notification.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Handler responsible for delivering notifications to specific users
 * via the STOMP WebSocket message broker.
 * <p>
 * Messages are sent to the user-specific queue:
 * /user/{userId}/queue/notifications
 * <p>
 * Clients subscribe to this destination after establishing a STOMP connection
 * to receive real-time notification updates.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Destination suffix for user notification queues.
     */
    private static final String USER_NOTIFICATION_QUEUE = "/queue/notifications";

    /**
     * Send a notification message to a specific user.
     * The message is delivered to /user/{userId}/queue/notifications.
     *
     * @param userId  the target user's identifier (UUID string)
     * @param message the WebSocket message payload
     */
    public void sendToUser(String userId, WebSocketMessage message) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId,
                    USER_NOTIFICATION_QUEUE,
                    message
            );
            log.debug("WebSocket message sent to user {} at {}", userId, USER_NOTIFICATION_QUEUE);
        } catch (Exception e) {
            log.error("Failed to send WebSocket message to user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Broadcast a notification to all connected clients via a topic.
     * Used for system-wide announcements.
     *
     * @param message the WebSocket message payload
     */
    public void broadcastToAll(WebSocketMessage message) {
        try {
            messagingTemplate.convertAndSend("/topic/notifications", message);
            log.debug("Broadcast WebSocket message sent to /topic/notifications");
        } catch (Exception e) {
            log.error("Failed to broadcast WebSocket message: {}", e.getMessage(), e);
        }
    }

    /**
     * Send a notification to all subscribers of a specific topic.
     * Useful for geo-region or category-based broadcasts.
     *
     * @param topic   the topic path (e.g., "/topic/region/downtown")
     * @param message the WebSocket message payload
     */
    public void sendToTopic(String topic, WebSocketMessage message) {
        try {
            messagingTemplate.convertAndSend(topic, message);
            log.debug("WebSocket message sent to topic {}", topic);
        } catch (Exception e) {
            log.error("Failed to send WebSocket message to topic {}: {}", topic, e.getMessage(), e);
        }
    }
}
