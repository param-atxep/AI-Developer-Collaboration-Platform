package com.foodrescue.pickup.kafka;

import com.foodrescue.pickup.entity.ClaimerType;
import com.foodrescue.pickup.entity.PickupStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PickupEvent {

    private UUID pickupId;
    private UUID foodListingId;
    private UUID restaurantId;
    private UUID claimerId;
    private ClaimerType claimerType;
    private PickupStatus status;
    private LocalDateTime scheduledPickupTime;
    private LocalDateTime actualPickupTime;
    private String qrCode;
    private Integer rating;
    private String feedback;
    private String eventType;
    private LocalDateTime eventTimestamp;
}
