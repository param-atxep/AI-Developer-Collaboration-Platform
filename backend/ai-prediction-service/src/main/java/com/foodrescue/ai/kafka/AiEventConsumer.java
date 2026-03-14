package com.foodrescue.ai.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodrescue.ai.entity.WasteHistory;
import com.foodrescue.ai.repository.WasteHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Kafka consumer that listens for food listing and pickup events to automatically
 * update waste history data for more accurate AI predictions.
 */
@Component
@RequiredArgsConstructor
public class AiEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AiEventConsumer.class);

    private final WasteHistoryRepository wasteHistoryRepository;
    private final ObjectMapper objectMapper;

    /**
     * Consume food.listed events.
     * When a restaurant lists surplus food, it signals potential waste. We record this
     * as a waste data point (the food would have been wasted if not listed).
     *
     * Expected payload:
     * {
     *   "restaurantId": "uuid",
     *   "category": "bakery",
     *   "weightKg": 5.2,
     *   "listedAt": "2025-12-20",
     *   "isHoliday": false,
     *   "weatherCondition": "clear",
     *   "specialEvent": null
     * }
     */
    @KafkaListener(topics = "${kafka.topics.food-listed:food.listed}",
            groupId = "${spring.kafka.consumer.group-id:ai-prediction-group}",
            containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void consumeFoodListed(String message) {
        log.info("Received food.listed event");
        try {
            JsonNode event = objectMapper.readTree(message);

            UUID restaurantId = UUID.fromString(event.get("restaurantId").asText());
            String category = event.has("category") ? event.get("category").asText() : "general";
            double weightKg = event.has("weightKg") ? event.get("weightKg").asDouble() : 0.0;
            LocalDate date = event.has("listedAt")
                    ? LocalDate.parse(event.get("listedAt").asText())
                    : LocalDate.now();

            boolean isHoliday = event.has("isHoliday") && event.get("isHoliday").asBoolean();
            String weatherCondition = event.has("weatherCondition")
                    ? event.get("weatherCondition").asText() : null;
            String specialEvent = event.has("specialEvent") && !event.get("specialEvent").isNull()
                    ? event.get("specialEvent").asText() : null;

            WasteHistory history = WasteHistory.builder()
                    .restaurantId(restaurantId)
                    .date(date)
                    .wasteKg(weightKg)
                    .category(category)
                    .dayOfWeek(date.getDayOfWeek().getValue())
                    .month(date.getMonthValue())
                    .isHoliday(isHoliday)
                    .weatherCondition(weatherCondition)
                    .specialEvent(specialEvent)
                    .build();

            wasteHistoryRepository.save(history);
            log.info("Recorded waste history from food.listed: restaurant={}, category={}, kg={}",
                    restaurantId, category, weightKg);

        } catch (Exception e) {
            log.error("Failed to process food.listed event: {}", e.getMessage(), e);
        }
    }

    /**
     * Consume pickup.completed events.
     * When a pickup is completed, we can refine our waste data -- the food was rescued
     * rather than wasted, but the initial listing still represents surplus production.
     * We also update existing predictions with actual waste figures where the pickup
     * only covered part of the listed food.
     *
     * Expected payload:
     * {
     *   "restaurantId": "uuid",
     *   "category": "produce",
     *   "listedWeightKg": 10.0,
     *   "pickedUpWeightKg": 8.5,
     *   "completedAt": "2025-12-20",
     *   "isHoliday": false,
     *   "weatherCondition": "rain",
     *   "specialEvent": null
     * }
     */
    @KafkaListener(topics = "${kafka.topics.pickup-completed:pickup.completed}",
            groupId = "${spring.kafka.consumer.group-id:ai-prediction-group}",
            containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void consumePickupCompleted(String message) {
        log.info("Received pickup.completed event");
        try {
            JsonNode event = objectMapper.readTree(message);

            UUID restaurantId = UUID.fromString(event.get("restaurantId").asText());
            String category = event.has("category") ? event.get("category").asText() : "general";
            double listedWeightKg = event.has("listedWeightKg")
                    ? event.get("listedWeightKg").asDouble() : 0.0;
            double pickedUpWeightKg = event.has("pickedUpWeightKg")
                    ? event.get("pickedUpWeightKg").asDouble() : 0.0;
            LocalDate date = event.has("completedAt")
                    ? LocalDate.parse(event.get("completedAt").asText())
                    : LocalDate.now();

            boolean isHoliday = event.has("isHoliday") && event.get("isHoliday").asBoolean();
            String weatherCondition = event.has("weatherCondition")
                    ? event.get("weatherCondition").asText() : null;
            String specialEvent = event.has("specialEvent") && !event.get("specialEvent").isNull()
                    ? event.get("specialEvent").asText() : null;

            // The actual waste is the surplus that was NOT picked up
            double actualWasteKg = Math.max(0, listedWeightKg - pickedUpWeightKg);

            // Record the actual waste (what was not rescued)
            WasteHistory history = WasteHistory.builder()
                    .restaurantId(restaurantId)
                    .date(date)
                    .wasteKg(actualWasteKg)
                    .category(category)
                    .dayOfWeek(date.getDayOfWeek().getValue())
                    .month(date.getMonthValue())
                    .isHoliday(isHoliday)
                    .weatherCondition(weatherCondition)
                    .specialEvent(specialEvent)
                    .build();

            wasteHistoryRepository.save(history);
            log.info("Recorded waste history from pickup.completed: restaurant={}, category={}, " +
                            "listedKg={}, pickedUpKg={}, actualWasteKg={}",
                    restaurantId, category, listedWeightKg, pickedUpWeightKg, actualWasteKg);

        } catch (Exception e) {
            log.error("Failed to process pickup.completed event: {}", e.getMessage(), e);
        }
    }
}
