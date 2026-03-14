package com.foodrescue.notification.dto;

import com.foodrescue.notification.entity.NotificationChannel;
import com.foodrescue.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for notification data exchanged between
 * the service layer, Kafka consumers, and REST controllers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {

    private UUID id;

    private UUID userId;

    private String title;

    private String message;

    private NotificationType type;

    private NotificationChannel channel;

    private boolean isRead;

    /**
     * Optional JSON string carrying additional context such as
     * food listing ID, pickup coordinates, or donor information.
     */
    private String metadata;

    private LocalDateTime createdAt;

    /**
     * Optional email address for email-channel notifications.
     * Populated by the Kafka consumer from the event payload.
     */
    private String recipientEmail;
}
