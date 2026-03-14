package com.foodrescue.pickup.dto;

import com.foodrescue.pickup.entity.ClaimerType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
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
public class CreatePickupRequest {

    @NotNull(message = "Food listing ID is required")
    private UUID foodListingId;

    @NotNull(message = "Restaurant ID is required")
    private UUID restaurantId;

    @NotNull(message = "Claimer ID is required")
    private UUID claimerId;

    @NotNull(message = "Claimer type is required")
    private ClaimerType claimerType;

    @NotNull(message = "Scheduled pickup time is required")
    @Future(message = "Scheduled pickup time must be in the future")
    private LocalDateTime scheduledPickupTime;

    private String notes;
}
