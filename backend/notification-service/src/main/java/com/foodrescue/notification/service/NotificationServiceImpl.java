package com.foodrescue.notification.service;

import com.foodrescue.notification.dto.NotificationDto;
import com.foodrescue.notification.dto.WebSocketMessage;
import com.foodrescue.notification.entity.Notification;
import com.foodrescue.notification.entity.NotificationChannel;
import com.foodrescue.notification.entity.NotificationPreference;
import com.foodrescue.notification.entity.NotificationType;
import com.foodrescue.notification.repository.NotificationPreferenceRepository;
import com.foodrescue.notification.repository.NotificationRepository;
import com.foodrescue.notification.websocket.WebSocketHandler;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Production implementation of {@link NotificationService}.
 * Handles notification persistence, preference checking, and multi-channel delivery
 * via WebSocket, email, and push notification channels.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final WebSocketHandler webSocketHandler;
    private final EmailService emailService;

    @Override
    public NotificationDto sendNotification(NotificationDto notificationDto) {
        log.info("Sending notification to user {} - type: {}, title: {}",
                notificationDto.getUserId(), notificationDto.getType(), notificationDto.getTitle());

        // Retrieve user preferences (or use defaults)
        NotificationPreference preferences = getOrCreateDefaultPreferences(notificationDto.getUserId());

        // Check if this notification type is enabled for the user
        if (!isNotificationTypeEnabled(notificationDto.getType(), preferences)) {
            log.info("Notification type {} is disabled for user {}, skipping",
                    notificationDto.getType(), notificationDto.getUserId());
            return notificationDto;
        }

        // Persist the notification for WebSocket channel (always persisted for history)
        Notification notification = buildNotificationEntity(notificationDto, NotificationChannel.WEBSOCKET);
        Notification saved = notificationRepository.save(notification);
        log.debug("Notification persisted with id: {}", saved.getId());

        // Deliver via WebSocket if enabled
        if (preferences.isWebsocketEnabled()) {
            deliverViaWebSocket(saved);
        }

        // Deliver via email if enabled and email address is available
        if (preferences.isEmailEnabled() && notificationDto.getRecipientEmail() != null
                && !notificationDto.getRecipientEmail().isBlank()) {
            deliverViaEmail(notificationDto);
        }

        // Deliver via push if enabled (placeholder for future implementation)
        if (preferences.isPushEnabled()) {
            log.debug("Push notification delivery not yet implemented for user {}",
                    notificationDto.getUserId());
        }

        return mapToDto(saved);
    }

    @Override
    public NotificationDto markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Notification not found with id: " + notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw new SecurityException(
                    "User " + userId + " is not authorized to modify notification " + notificationId);
        }

        notification.setRead(true);
        Notification updated = notificationRepository.save(notification);
        log.debug("Notification {} marked as read for user {}", notificationId, userId);

        return mapToDto(updated);
    }

    @Override
    public int markAllAsRead(UUID userId) {
        int count = notificationRepository.markAllAsReadByUserId(userId);
        log.info("Marked {} notifications as read for user {}", count, userId);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotifications(UUID userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPreference getPreferences(UUID userId) {
        return getOrCreateDefaultPreferences(userId);
    }

    @Override
    public NotificationPreference updatePreferences(UUID userId, NotificationPreference preference) {
        NotificationPreference existing = preferenceRepository.findByUserId(userId)
                .orElse(NotificationPreference.builder().userId(userId).build());

        existing.setEmailEnabled(preference.isEmailEnabled());
        existing.setPushEnabled(preference.isPushEnabled());
        existing.setWebsocketEnabled(preference.isWebsocketEnabled());
        existing.setFoodAvailableAlert(preference.isFoodAvailableAlert());
        existing.setPickupReminder(preference.isPickupReminder());
        existing.setSystemAlerts(preference.isSystemAlerts());

        NotificationPreference saved = preferenceRepository.save(existing);
        log.info("Updated notification preferences for user {}", userId);
        return saved;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Get existing preferences or create a default set for the user.
     */
    private NotificationPreference getOrCreateDefaultPreferences(UUID userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    NotificationPreference defaults = NotificationPreference.builder()
                            .userId(userId)
                            .emailEnabled(true)
                            .pushEnabled(true)
                            .websocketEnabled(true)
                            .foodAvailableAlert(true)
                            .pickupReminder(true)
                            .systemAlerts(true)
                            .build();
                    return preferenceRepository.save(defaults);
                });
    }

    /**
     * Check whether a specific notification type is enabled in user preferences.
     */
    private boolean isNotificationTypeEnabled(NotificationType type, NotificationPreference prefs) {
        return switch (type) {
            case FOOD_AVAILABLE -> prefs.isFoodAvailableAlert();
            case FOOD_CLAIMED, PICKUP_SCHEDULED, PICKUP_COMPLETED, PICKUP_CANCELLED -> prefs.isPickupReminder();
            case SYSTEM_ALERT -> prefs.isSystemAlerts();
        };
    }

    /**
     * Build a JPA entity from the DTO.
     */
    private Notification buildNotificationEntity(NotificationDto dto, NotificationChannel channel) {
        return Notification.builder()
                .userId(dto.getUserId())
                .title(dto.getTitle())
                .message(dto.getMessage())
                .type(dto.getType())
                .channel(channel)
                .isRead(false)
                .metadata(dto.getMetadata())
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Deliver a notification to the user via WebSocket.
     */
    private void deliverViaWebSocket(Notification notification) {
        try {
            long unreadCount = notificationRepository.countByUserIdAndIsRead(
                    notification.getUserId(), false);

            WebSocketMessage wsMessage = WebSocketMessage.builder()
                    .id(notification.getId())
                    .userId(notification.getUserId())
                    .title(notification.getTitle())
                    .message(notification.getMessage())
                    .type(notification.getType())
                    .metadata(notification.getMetadata())
                    .timestamp(notification.getCreatedAt())
                    .unreadCount(unreadCount)
                    .build();

            webSocketHandler.sendToUser(notification.getUserId().toString(), wsMessage);
            log.debug("WebSocket notification delivered to user {}", notification.getUserId());
        } catch (Exception e) {
            log.error("Failed to deliver WebSocket notification to user {}: {}",
                    notification.getUserId(), e.getMessage(), e);
        }
    }

    /**
     * Deliver a notification to the user via email.
     */
    private void deliverViaEmail(NotificationDto dto) {
        try {
            emailService.sendNotificationEmail(
                    dto.getRecipientEmail(),
                    dto.getTitle(),
                    dto.getMessage(),
                    dto.getType()
            );
            log.debug("Email notification delivered to {}", dto.getRecipientEmail());
        } catch (Exception e) {
            log.error("Failed to deliver email notification to {}: {}",
                    dto.getRecipientEmail(), e.getMessage(), e);
        }
    }

    /**
     * Map a JPA entity to a DTO.
     */
    private NotificationDto mapToDto(Notification entity) {
        return NotificationDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .type(entity.getType())
                .channel(entity.getChannel())
                .isRead(entity.isRead())
                .metadata(entity.getMetadata())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
