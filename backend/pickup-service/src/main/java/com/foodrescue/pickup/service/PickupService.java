package com.foodrescue.pickup.service;

import com.foodrescue.pickup.dto.CreatePickupRequest;
import com.foodrescue.pickup.dto.PickupResponse;
import com.foodrescue.pickup.dto.UpdatePickupStatusRequest;
import com.foodrescue.pickup.entity.PickupStatus;

import java.util.List;
import java.util.UUID;

public interface PickupService {

    PickupResponse schedulePickup(CreatePickupRequest request);

    PickupResponse getPickupById(UUID pickupId);

    PickupResponse updateStatus(UUID pickupId, UpdatePickupStatusRequest request);

    PickupResponse completePickup(UUID pickupId);

    PickupResponse cancelPickup(UUID pickupId, String reason);

    PickupResponse ratePickup(UUID pickupId, Integer rating, String feedback);

    List<PickupResponse> getByClaimerId(UUID claimerId);

    List<PickupResponse> getByRestaurantId(UUID restaurantId);

    List<PickupResponse> getByStatus(PickupStatus status);

    PickupResponse getByFoodListingId(UUID foodListingId);
}
