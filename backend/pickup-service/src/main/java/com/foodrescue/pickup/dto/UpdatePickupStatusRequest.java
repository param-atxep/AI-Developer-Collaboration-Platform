package com.foodrescue.pickup.dto;

import com.foodrescue.pickup.entity.PickupStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePickupStatusRequest {

    @NotNull(message = "Status is required")
    private PickupStatus status;

    private String notes;
}
