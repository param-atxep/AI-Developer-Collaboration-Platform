package com.foodrescue.notification.dto;

import com.foodrescue.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Message payload delivered to clients over the STOMP WebSocket connection.
 * Serialized as JSON before being sent to /user/{userId}/queue/notifications.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketMessage {

    /**
     * Unique identifier of the persisted notification.
     */
    private UUID id;

    /**
     * Target user who should receive this real-time message.
     */
    private UUID userId;

    /**
     * Short summary displayed as the notification headline.
     */
    private String title;

    /**
     * Full notification body text.
     */
    private String message;

    /**
     * The type of event that triggered this notification.
     */
    private NotificationType type;

    /**
     * Optional JSON metadata with contextual details
     * (e.g., food listing ID, location data, pickup info).
     */
    private String metadata;

    /**
     * Timestamp when the notification was created.
     */
    private LocalDateTime timestamp;

    /**
     * Current count of unread notifications for the target user.
     * Allows the client to update badge counts without a separate API call.
     */
    private long unreadCount;
}
