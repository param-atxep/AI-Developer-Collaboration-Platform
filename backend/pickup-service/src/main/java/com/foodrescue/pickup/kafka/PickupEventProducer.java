package com.foodrescue.pickup.kafka;

import com.foodrescue.pickup.config.KafkaConfig;
import com.foodrescue.pickup.entity.Pickup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class PickupEventProducer {

    private final KafkaTemplate<String, PickupEvent> kafkaTemplate;

    public void publishPickupScheduled(Pickup pickup) {
        PickupEvent event = buildEvent(pickup, "PICKUP_SCHEDULED");
        sendEvent(KafkaConfig.TOPIC_PICKUP_SCHEDULED, pickup.getId().toString(), event);
    }

    public void publishPickupCompleted(Pickup pickup) {
        PickupEvent event = buildEvent(pickup, "PICKUP_COMPLETED");
        sendEvent(KafkaConfig.TOPIC_PICKUP_COMPLETED, pickup.getId().toString(), event);
    }

    public void publishPickupCancelled(Pickup pickup) {
        PickupEvent event = buildEvent(pickup, "PICKUP_CANCELLED");
        sendEvent(KafkaConfig.TOPIC_PICKUP_CANCELLED, pickup.getId().toString(), event);
    }

    private PickupEvent buildEvent(Pickup pickup, String eventType) {
        return PickupEvent.builder()
                .pickupId(pickup.getId())
                .foodListingId(pickup.getFoodListingId())
                .restaurantId(pickup.getRestaurantId())
                .claimerId(pickup.getClaimerId())
                .claimerType(pickup.getClaimerType())
                .status(pickup.getStatus())
                .scheduledPickupTime(pickup.getScheduledPickupTime())
                .actualPickupTime(pickup.getActualPickupTime())
                .qrCode(pickup.getQrCode())
                .rating(pickup.getRating())
                .feedback(pickup.getFeedback())
                .eventType(eventType)
                .eventTimestamp(LocalDateTime.now())
                .build();
    }

    private void sendEvent(String topic, String key, PickupEvent event) {
        CompletableFuture<SendResult<String, PickupEvent>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Published event [{}] to topic [{}] with key [{}], partition [{}], offset [{}]",
                        event.getEventType(),
                        topic,
                        key,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish event [{}] to topic [{}] with key [{}]: {}",
                        event.getEventType(),
                        topic,
                        key,
                        ex.getMessage(),
                        ex);
            }
        });
    }
}
