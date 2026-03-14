package com.foodrescue.pickup.service;

import com.foodrescue.pickup.dto.CreatePickupRequest;
import com.foodrescue.pickup.dto.PickupResponse;
import com.foodrescue.pickup.dto.UpdatePickupStatusRequest;
import com.foodrescue.pickup.entity.Pickup;
import com.foodrescue.pickup.entity.PickupStatus;
import com.foodrescue.pickup.kafka.PickupEventProducer;
import com.foodrescue.pickup.repository.PickupRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PickupServiceImpl implements PickupService {

    private final PickupRepository pickupRepository;
    private final PickupEventProducer pickupEventProducer;

    @Override
    public PickupResponse schedulePickup(CreatePickupRequest request) {
        log.info("Scheduling pickup for food listing [{}] by claimer [{}]",
                request.getFoodListingId(), request.getClaimerId());

        // Check if a pickup already exists for this food listing (not cancelled)
        boolean exists = pickupRepository.existsByFoodListingIdAndStatusNot(
                request.getFoodListingId(), PickupStatus.CANCELLED);
        if (exists) {
            throw new IllegalStateException(
                    "An active pickup already exists for food listing: " + request.getFoodListingId());
        }

        // Generate unique QR code
        String qrCode = UUID.randomUUID().toString();

        Pickup pickup = Pickup.builder()
                .foodListingId(request.getFoodListingId())
                .restaurantId(request.getRestaurantId())
                .claimerId(request.getClaimerId())
                .claimerType(request.getClaimerType())
                .status(PickupStatus.SCHEDULED)
                .scheduledPickupTime(request.getScheduledPickupTime())
                .notes(request.getNotes())
                .qrCode(qrCode)
                .build();

        Pickup savedPickup = pickupRepository.save(pickup);
        log.info("Pickup scheduled with ID [{}] and QR code [{}]", savedPickup.getId(), qrCode);

        // Publish Kafka event
        try {
            pickupEventProducer.publishPickupScheduled(savedPickup);
        } catch (Exception e) {
            log.error("Failed to publish pickup.scheduled event for pickup [{}]: {}",
                    savedPickup.getId(), e.getMessage(), e);
        }

        return mapToResponse(savedPickup);
    }

    @Override
    @Transactional(readOnly = true)
    public PickupResponse getPickupById(UUID pickupId) {
        log.debug("Fetching pickup by ID [{}]", pickupId);
        Pickup pickup = findPickupOrThrow(pickupId);
        return mapToResponse(pickup);
    }

    @Override
    public PickupResponse updateStatus(UUID pickupId, UpdatePickupStatusRequest request) {
        log.info("Updating status of pickup [{}] to [{}]", pickupId, request.getStatus());

        Pickup pickup = findPickupOrThrow(pickupId);
        validateStatusTransition(pickup.getStatus(), request.getStatus());

        pickup.setStatus(request.getStatus());
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            String existingNotes = pickup.getNotes() != null ? pickup.getNotes() + "\n" : "";
            pickup.setNotes(existingNotes + request.getNotes());
        }

        Pickup updatedPickup = pickupRepository.save(pickup);
        log.info("Pickup [{}] status updated to [{}]", pickupId, request.getStatus());

        // Publish events based on new status
        publishStatusChangeEvent(updatedPickup);

        return mapToResponse(updatedPickup);
    }

    @Override
    public PickupResponse completePickup(UUID pickupId) {
        log.info("Completing pickup [{}]", pickupId);

        Pickup pickup = findPickupOrThrow(pickupId);
        validateStatusTransition(pickup.getStatus(), PickupStatus.COMPLETED);

        pickup.setStatus(PickupStatus.COMPLETED);
        pickup.setActualPickupTime(LocalDateTime.now());

        Pickup completedPickup = pickupRepository.save(pickup);
        log.info("Pickup [{}] completed at [{}]", pickupId, completedPickup.getActualPickupTime());

        try {
            pickupEventProducer.publishPickupCompleted(completedPickup);
        } catch (Exception e) {
            log.error("Failed to publish pickup.completed event for pickup [{}]: {}",
                    pickupId, e.getMessage(), e);
        }

        return mapToResponse(completedPickup);
    }

    @Override
    public PickupResponse cancelPickup(UUID pickupId, String reason) {
        log.info("Cancelling pickup [{}] with reason: {}", pickupId, reason);

        Pickup pickup = findPickupOrThrow(pickupId);

        if (pickup.getStatus() == PickupStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed pickup");
        }

        if (pickup.getStatus() == PickupStatus.CANCELLED) {
            throw new IllegalStateException("Pickup is already cancelled");
        }

        pickup.setStatus(PickupStatus.CANCELLED);
        if (reason != null && !reason.isBlank()) {
            String existingNotes = pickup.getNotes() != null ? pickup.getNotes() + "\n" : "";
            pickup.setNotes(existingNotes + "Cancellation reason: " + reason);
        }

        Pickup cancelledPickup = pickupRepository.save(pickup);
        log.info("Pickup [{}] cancelled successfully", pickupId);

        try {
            pickupEventProducer.publishPickupCancelled(cancelledPickup);
        } catch (Exception e) {
            log.error("Failed to publish pickup.cancelled event for pickup [{}]: {}",
                    pickupId, e.getMessage(), e);
        }

        return mapToResponse(cancelledPickup);
    }

    @Override
    public PickupResponse ratePickup(UUID pickupId, Integer rating, String feedback) {
        log.info("Rating pickup [{}] with score [{}]", pickupId, rating);

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Pickup pickup = findPickupOrThrow(pickupId);

        if (pickup.getStatus() != PickupStatus.COMPLETED) {
            throw new IllegalStateException("Can only rate a completed pickup");
        }

        if (pickup.getRating() != null) {
            throw new IllegalStateException("Pickup has already been rated");
        }

        pickup.setRating(rating);
        pickup.setFeedback(feedback);

        Pickup ratedPickup = pickupRepository.save(pickup);
        log.info("Pickup [{}] rated with score [{}]", pickupId, rating);

        return mapToResponse(ratedPickup);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PickupResponse> getByClaimerId(UUID claimerId) {
        log.debug("Fetching pickups for claimer [{}]", claimerId);
        return pickupRepository.findByClaimerIdOrderByCreatedAtDesc(claimerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PickupResponse> getByRestaurantId(UUID restaurantId) {
        log.debug("Fetching pickups for restaurant [{}]", restaurantId);
        return pickupRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PickupResponse> getByStatus(PickupStatus status) {
        log.debug("Fetching pickups with status [{}]", status);
        return pickupRepository.findByStatusOrderByScheduledPickupTimeAsc(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PickupResponse getByFoodListingId(UUID foodListingId) {
        log.debug("Fetching pickup for food listing [{}]", foodListingId);
        Pickup pickup = pickupRepository.findByFoodListingId(foodListingId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Pickup not found for food listing: " + foodListingId));
        return mapToResponse(pickup);
    }

    // ==================== Private Helper Methods ====================

    private Pickup findPickupOrThrow(UUID pickupId) {
        return pickupRepository.findById(pickupId)
                .orElseThrow(() -> new EntityNotFoundException("Pickup not found with ID: " + pickupId));
    }

    private void validateStatusTransition(PickupStatus currentStatus, PickupStatus newStatus) {
        boolean valid = switch (currentStatus) {
            case SCHEDULED -> newStatus == PickupStatus.IN_PROGRESS
                    || newStatus == PickupStatus.CANCELLED
                    || newStatus == PickupStatus.NO_SHOW;
            case IN_PROGRESS -> newStatus == PickupStatus.COMPLETED
                    || newStatus == PickupStatus.CANCELLED
                    || newStatus == PickupStatus.NO_SHOW;
            case COMPLETED, CANCELLED, NO_SHOW -> false;
        };

        if (!valid) {
            throw new IllegalStateException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
        }
    }

    private void publishStatusChangeEvent(Pickup pickup) {
        try {
            switch (pickup.getStatus()) {
                case COMPLETED -> pickupEventProducer.publishPickupCompleted(pickup);
                case CANCELLED -> pickupEventProducer.publishPickupCancelled(pickup);
                default -> {
                    // No event published for other status transitions via updateStatus
                }
            }
        } catch (Exception e) {
            log.error("Failed to publish status change event for pickup [{}]: {}",
                    pickup.getId(), e.getMessage(), e);
        }
    }

    private PickupResponse mapToResponse(Pickup pickup) {
        return PickupResponse.builder()
                .id(pickup.getId())
                .foodListingId(pickup.getFoodListingId())
                .restaurantId(pickup.getRestaurantId())
                .claimerId(pickup.getClaimerId())
                .claimerType(pickup.getClaimerType())
                .status(pickup.getStatus())
                .scheduledPickupTime(pickup.getScheduledPickupTime())
                .actualPickupTime(pickup.getActualPickupTime())
                .notes(pickup.getNotes())
                .rating(pickup.getRating())
                .feedback(pickup.getFeedback())
                .qrCode(pickup.getQrCode())
                .createdAt(pickup.getCreatedAt())
                .updatedAt(pickup.getUpdatedAt())
                .build();
    }
}
