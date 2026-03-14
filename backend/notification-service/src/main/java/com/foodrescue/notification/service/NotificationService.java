package com.foodrescue.notification.service;

import com.foodrescue.notification.dto.NotificationDto;
import com.foodrescue.notification.entity.NotificationPreference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Core notification service interface defining all notification operations.
 * Implementations handle persistence, preference-checking, and multi-channel delivery.
 */
public interface NotificationService {

    /**
     * Create, persist, and deliver a notification to the target user
     * across all applicable channels (WebSocket, email, push) based on user preferences.
     *
     * @param notificationDto the notification data to send
     * @return the persisted notification DTO with generated ID and timestamp
     */
    NotificationDto sendNotification(NotificationDto notificationDto);

    /**
     * Mark a single notification as read.
     *
     * @param notificationId the notification's unique identifier
     * @param userId         the requesting user's ID (for ownership verification)
     * @return the updated notification DTO
     */
    NotificationDto markAsRead(UUID notificationId, UUID userId);

    /**
     * Mark all unread notifications for a user as read.
     *
     * @param userId the target user's unique identifier
     * @return the number of notifications marked as read
     */
    int markAllAsRead(UUID userId);

    /**
     * Retrieve a paginated list of notifications for a user.
     *
     * @param userId   the target user's unique identifier
     * @param pageable pagination and sorting parameters
     * @return a page of notification DTOs
     */
    Page<NotificationDto> getUserNotifications(UUID userId, Pageable pageable);

    /**
     * Get the count of unread notifications for a user.
     *
     * @param userId the target user's unique identifier
     * @return the number of unread notifications
     */
    long getUnreadCount(UUID userId);

    /**
     * Retrieve the notification preferences for a user.
     * If no preferences exist, returns a default-initialized preference object.
     *
     * @param userId the target user's unique identifier
     * @return the user's notification preferences
     */
    NotificationPreference getPreferences(UUID userId);

    /**
     * Create or update notification preferences for a user.
     *
     * @param userId     the target user's unique identifier
     * @param preference the preference settings to apply
     * @return the persisted preferences
     */
    NotificationPreference updatePreferences(UUID userId, NotificationPreference preference);
}
