package com.foodrescue.food.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class FoodEventProducer {

    public static final String TOPIC_FOOD_LISTED = "food.listed";
    public static final String TOPIC_FOOD_CLAIMED = "food.claimed";
    public static final String TOPIC_FOOD_EXPIRED = "food.expired";

    private final KafkaTemplate<String, FoodEvent> kafkaTemplate;

    /**
     * Publish a FOOD_LISTED event when a new food listing is created.
     */
    public void publishFoodListed(FoodEvent event) {
        sendEvent(TOPIC_FOOD_LISTED, event);
    }

    /**
     * Publish a FOOD_CLAIMED event when a food listing is claimed by a recipient.
     */
    public void publishFoodClaimed(FoodEvent event) {
        sendEvent(TOPIC_FOOD_CLAIMED, event);
    }

    /**
     * Publish a FOOD_EXPIRED event when a food listing has expired.
     */
    public void publishFoodExpired(FoodEvent event) {
        sendEvent(TOPIC_FOOD_EXPIRED, event);
    }

    private void sendEvent(String topic, FoodEvent event) {
        String key = event.getFoodListingId().toString();

        log.info("Publishing event to topic [{}]: eventType={}, foodListingId={}",
                topic, event.getEventType(), event.getFoodListingId());

        CompletableFuture<SendResult<String, FoodEvent>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event to topic [{}]: eventType={}, foodListingId={}, error={}",
                        topic, event.getEventType(), event.getFoodListingId(), ex.getMessage(), ex);
            } else {
                log.info("Successfully published event to topic [{}]: eventType={}, foodListingId={}, offset={}",
                        topic, event.getEventType(), event.getFoodListingId(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
