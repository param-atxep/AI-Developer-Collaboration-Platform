package com.foodrescue.notification.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodrescue.notification.dto.NotificationDto;
import com.foodrescue.notification.entity.NotificationType;
import com.foodrescue.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Kafka consumer that listens to domain events from other microservices
 * and translates them into user notifications.
 * <p>
 * Consumed topics:
 * <ul>
 *   <li><b>notification.geo-matched</b> - When food is matched to a nearby user via geo-matching</li>
 *   <li><b>food.claimed</b> - When a food listing is claimed by a recipient</li>
 *   <li><b>pickup.scheduled</b> - When a pickup is scheduled for a food donation</li>
 *   <li><b>pickup.completed</b> - When a pickup has been successfully completed</li>
 *   <li><b>pickup.cancelled</b> - When a scheduled pickup is cancelled</li>
 * </ul>
 * <p>
 * Each event is expected to be a JSON object with at least:
 * <pre>
 * {
 *   "userId": "uuid",
 *   "title": "string" (optional),
 *   "message": "string" (optional),
 *   "email": "string" (optional),
 *   "metadata": { ... } (optional)
 * }
 * </pre>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    /**
     * Handle geo-matched food availability events.
     * Sent when the matching engine finds food near a user's location.
     */
    @KafkaListener(
            topics = "notification.geo-matched",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleGeoMatchedEvent(String payload) {
        log.info("Received geo-matched event: {}", truncateForLog(payload));
        processEvent(payload, NotificationType.FOOD_AVAILABLE,
                "Food Available Nearby",
                "New food has been listed near your location and is available for pickup.");
    }

    /**
     * Handle food claimed events.
     * Sent when a recipient claims a food listing.
     */
    @KafkaListener(
            topics = "food.claimed",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleFoodClaimedEvent(String payload) {
        log.info("Received food claimed event: {}", truncateForLog(payload));
        processEvent(payload, NotificationType.FOOD_CLAIMED,
                "Food Listing Claimed",
                "Your food listing has been claimed by a recipient.");
    }

    /**
     * Handle pickup scheduled events.
     * Sent when a pickup is scheduled for a claimed food donation.
     */
    @KafkaListener(
            topics = "pickup.scheduled",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePickupScheduledEvent(String payload) {
        log.info("Received pickup scheduled event: {}", truncateForLog(payload));
        processEvent(payload, NotificationType.PICKUP_SCHEDULED,
                "Pickup Scheduled",
                "A pickup has been scheduled for your food donation.");
    }

    /**
     * Handle pickup completed events.
     * Sent when a pickup has been successfully completed.
     */
    @KafkaListener(
            topics = "pickup.completed",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePickupCompletedEvent(String payload) {
        log.info("Received pickup completed event: {}", truncateForLog(payload));
        processEvent(payload, NotificationType.PICKUP_COMPLETED,
                "Pickup Completed",
                "The food pickup has been successfully completed. Thank you for your contribution!");
    }

    /**
     * Handle pickup cancelled events.
     * Sent when a scheduled pickup is cancelled.
     */
    @KafkaListener(
            topics = "pickup.cancelled",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePickupCancelledEvent(String payload) {
        log.info("Received pickup cancelled event: {}", truncateForLog(payload));
        processEvent(payload, NotificationType.PICKUP_CANCELLED,
                "Pickup Cancelled",
                "A scheduled pickup has been cancelled. The food listing is now available again.");
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Parse the event payload and delegate to the notification service.
     *
     * @param payload        raw JSON string from Kafka
     * @param type           the notification type to assign
     * @param defaultTitle   fallback title if none in the payload
     * @param defaultMessage fallback message if none in the payload
     */
    private void processEvent(String payload, NotificationType type,
                              String defaultTitle, String defaultMessage) {
        try {
            JsonNode event = objectMapper.readTree(payload);

            // Extract userId (required)
            String userIdStr = getTextValue(event, "userId");
            if (userIdStr == null || userIdStr.isBlank()) {
                log.error("Event missing required 'userId' field, skipping: {}", truncateForLog(payload));
                return;
            }

            UUID userId;
            try {
                userId = UUID.fromString(userIdStr);
            } catch (IllegalArgumentException e) {
                log.error("Invalid UUID format for userId '{}', skipping event", userIdStr);
                return;
            }

            // Extract optional fields with defaults
            String title = getTextValueOrDefault(event, "title", defaultTitle);
            String message = getTextValueOrDefault(event, "message", defaultMessage);
            String email = getTextValue(event, "email");

            // Extract metadata as a JSON string
            String metadata = null;
            JsonNode metadataNode = event.get("metadata");
            if (metadataNode != null && !metadataNode.isNull()) {
                metadata = objectMapper.writeValueAsString(metadataNode);
            } else {
                // Store the entire event as metadata if no dedicated metadata field
                metadata = payload;
            }

            // Build and send the notification
            NotificationDto notification = NotificationDto.builder()
                    .userId(userId)
                    .title(title)
                    .message(message)
                    .type(type)
                    .metadata(metadata)
                    .recipientEmail(email)
                    .build();

            notificationService.sendNotification(notification);
            log.info("Notification sent for event type {} to user {}", type, userId);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse Kafka event payload: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error processing Kafka event: {}", e.getMessage(), e);
        }
    }

    /**
     * Safely extract a text value from a JsonNode.
     */
    private String getTextValue(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode != null && !fieldNode.isNull() && fieldNode.isTextual()) {
            return fieldNode.asText();
        }
        return null;
    }

    /**
     * Safely extract a text value from a JsonNode with a default fallback.
     */
    private String getTextValueOrDefault(JsonNode node, String field, String defaultValue) {
        String value = getTextValue(node, field);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }

    /**
     * Truncate a string for safe logging (prevent log injection and excessive output).
     */
    private String truncateForLog(String value) {
        if (value == null) return "null";
        return value.length() > 500 ? value.substring(0, 500) + "..." : value;
    }
}
