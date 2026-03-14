package com.foodrescue.pickup.kafka;

import com.foodrescue.pickup.config.KafkaConfig;
import com.foodrescue.pickup.dto.CreatePickupRequest;
import com.foodrescue.pickup.service.PickupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PickupEventConsumer {

    private final PickupService pickupService;

    @KafkaListener(
            topics = KafkaConfig.TOPIC_FOOD_CLAIMED,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleFoodClaimedEvent(PickupEvent event) {
        log.info("Received food.claimed event for food listing [{}] claimed by [{}] (type: {})",
                event.getFoodListingId(),
                event.getClaimerId(),
                event.getClaimerType());

        try {
            CreatePickupRequest request = CreatePickupRequest.builder()
                    .foodListingId(event.getFoodListingId())
                    .restaurantId(event.getRestaurantId())
                    .claimerId(event.getClaimerId())
                    .claimerType(event.getClaimerType())
                    .scheduledPickupTime(event.getScheduledPickupTime())
                    .notes("Auto-scheduled from food claim event")
                    .build();

            pickupService.schedulePickup(request);

            log.info("Successfully scheduled pickup for food listing [{}]", event.getFoodListingId());
        } catch (Exception e) {
            log.error("Failed to process food.claimed event for food listing [{}]: {}",
                    event.getFoodListingId(),
                    e.getMessage(),
                    e);
        }
    }
}
