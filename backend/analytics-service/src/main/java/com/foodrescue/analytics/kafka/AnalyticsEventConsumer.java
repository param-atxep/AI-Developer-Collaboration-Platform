package com.foodrescue.analytics.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodrescue.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsEventConsumer {

    private final AnalyticsService analyticsService;
    private final ObjectMapper objectMapper;

    /**
     * Consumes events when food is listed by a restaurant.
     * Expected payload:
     * {
     *   "restaurantId": "uuid",
     *   "listingId": "uuid",
     *   "foodKg": 5.0,
     *   "monetaryValue": 22.50,
     *   "category": "prepared_meals",
     *   "timestamp": "2026-03-14T10:30:00"
     * }
     */
    @KafkaListener(
            topics = "${app.kafka.topics.food-listed:food.listed}",
            groupId = "${spring.kafka.consumer.group-id:analytics-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeFoodListedEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received food.listed event: topic={}, partition={}, offset={}",
                topic, partition, offset);

        try {
            JsonNode event = objectMapper.readTree(message);

            UUID restaurantId = UUID.fromString(event.get("restaurantId").asText());
            double foodKg = getDoubleOrDefault(event, "foodKg", 0.0);
            double monetaryValue = getDoubleOrDefault(event, "monetaryValue", 0.0);

            analyticsService.recordFoodListed(restaurantId, foodKg, monetaryValue);

            log.info("Successfully processed food.listed event for restaurant: {}",
                    restaurantId);

        } catch (Exception e) {
            log.error("Failed to process food.listed event: message={}, error={}",
                    message, e.getMessage(), e);
        }
    }

    /**
     * Consumes events when food is claimed by an NGO or individual.
     * Expected payload:
     * {
     *   "restaurantId": "uuid",
     *   "listingId": "uuid",
     *   "claimantId": "uuid",
     *   "claimantType": "NGO",
     *   "foodKg": 5.0,
     *   "timestamp": "2026-03-14T11:00:00"
     * }
     */
    @KafkaListener(
            topics = "${app.kafka.topics.food-claimed:food.claimed}",
            groupId = "${spring.kafka.consumer.group-id:analytics-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeFoodClaimedEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received food.claimed event: topic={}, partition={}, offset={}",
                topic, partition, offset);

        try {
            JsonNode event = objectMapper.readTree(message);

            UUID restaurantId = UUID.fromString(event.get("restaurantId").asText());
            double foodKg = getDoubleOrDefault(event, "foodKg", 0.0);

            analyticsService.recordFoodClaimed(restaurantId, foodKg);

            log.info("Successfully processed food.claimed event for restaurant: {}",
                    restaurantId);

        } catch (Exception e) {
            log.error("Failed to process food.claimed event: message={}, error={}",
                    message, e.getMessage(), e);
        }
    }

    /**
     * Consumes events when a food pickup is completed.
     * Expected payload:
     * {
     *   "restaurantId": "uuid",
     *   "listingId": "uuid",
     *   "pickupId": "uuid",
     *   "ngoId": "uuid",
     *   "foodKg": 5.0,
     *   "co2SavedKg": 12.5,
     *   "mealsProvided": 10,
     *   "monetaryValue": 22.50,
     *   "timestamp": "2026-03-14T12:00:00"
     * }
     */
    @KafkaListener(
            topics = "${app.kafka.topics.pickup-completed:pickup.completed}",
            groupId = "${spring.kafka.consumer.group-id:analytics-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePickupCompletedEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received pickup.completed event: topic={}, partition={}, offset={}",
                topic, partition, offset);

        try {
            JsonNode event = objectMapper.readTree(message);

            UUID restaurantId = UUID.fromString(event.get("restaurantId").asText());
            double foodKg = getDoubleOrDefault(event, "foodKg", 0.0);
            double co2SavedKg = getDoubleOrDefault(event, "co2SavedKg", 0.0);
            int mealsProvided = getIntOrDefault(event, "mealsProvided", 0);
            double monetaryValue = getDoubleOrDefault(event, "monetaryValue", 0.0);

            analyticsService.recordPickupCompleted(
                    restaurantId, foodKg, co2SavedKg, mealsProvided, monetaryValue);

            log.info("Successfully processed pickup.completed event for restaurant: {}",
                    restaurantId);

        } catch (Exception e) {
            log.error("Failed to process pickup.completed event: message={}, error={}",
                    message, e.getMessage(), e);
        }
    }

    // ---- Private Helpers ----

    private double getDoubleOrDefault(JsonNode node, String field, double defaultValue) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode != null && !fieldNode.isNull()) {
            return fieldNode.asDouble(defaultValue);
        }
        return defaultValue;
    }

    private int getIntOrDefault(JsonNode node, String field, int defaultValue) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode != null && !fieldNode.isNull()) {
            return fieldNode.asInt(defaultValue);
        }
        return defaultValue;
    }
}
