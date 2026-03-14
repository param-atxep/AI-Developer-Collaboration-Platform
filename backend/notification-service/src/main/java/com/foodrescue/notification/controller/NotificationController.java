package com.foodrescue.notification.controller;

import com.foodrescue.notification.dto.NotificationDto;
import com.foodrescue.notification.entity.NotificationPreference;
import com.foodrescue.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller exposing notification management endpoints.
 * <p>
 * All endpoints require a valid user ID passed via the X-User-Id header,
 * which is typically set by the API gateway after JWT validation.
 * <p>
 * Endpoints:
 * <ul>
 *   <li>GET    /api/notifications              - paginated list of user notifications</li>
 *   <li>PUT    /api/notifications/{id}/read     - mark a single notification as read</li>
 *   <li>PUT    /api/notifications/read-all      - mark all notifications as read</li>
 *   <li>GET    /api/notifications/unread-count  - count of unread notifications</li>
 *   <li>GET    /api/notifications/preferences   - get notification preferences</li>
 *   <li>PUT    /api/notifications/preferences   - update notification preferences</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Retrieve a paginated list of notifications for the authenticated user.
     *
     * @param userId the authenticated user's ID (from gateway header)
     * @param page   zero-based page index (default: 0)
     * @param size   page size (default: 20, max: 100)
     * @return a page of notification DTOs sorted by creation date descending
     */
    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getUserNotifications(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // Clamp page size to prevent abuse
        int clampedSize = Math.min(Math.max(size, 1), 100);

        Pageable pageable = PageRequest.of(page, clampedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NotificationDto> notifications = notificationService.getUserNotifications(userId, pageable);

        log.debug("Retrieved {} notifications (page {}) for user {}",
                notifications.getNumberOfElements(), page, userId);

        return ResponseEntity.ok(notifications);
    }

    /**
     * Mark a single notification as read.
     *
     * @param userId         the authenticated user's ID (from gateway header)
     * @param notificationId the notification's unique identifier
     * @return the updated notification DTO
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationDto> markAsRead(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable("id") UUID notificationId) {

        NotificationDto updated = notificationService.markAsRead(notificationId, userId);
        log.debug("Notification {} marked as read for user {}", notificationId, userId);

        return ResponseEntity.ok(updated);
    }

    /**
     * Mark all unread notifications as read for the authenticated user.
     *
     * @param userId the authenticated user's ID (from gateway header)
     * @return a map containing the count of notifications marked as read
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(
            @RequestHeader("X-User-Id") UUID userId) {

        int count = notificationService.markAllAsRead(userId);
        log.info("Marked {} notifications as read for user {}", count, userId);

        return ResponseEntity.ok(Map.of(
                "markedAsRead", count,
                "message", count + " notification(s) marked as read"
        ));
    }

    /**
     * Get the count of unread notifications for the authenticated user.
     *
     * @param userId the authenticated user's ID (from gateway header)
     * @return a map containing the unread count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader("X-User-Id") UUID userId) {

        long count = notificationService.getUnreadCount(userId);

        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * Retrieve the notification preferences for the authenticated user.
     *
     * @param userId the authenticated user's ID (from gateway header)
     * @return the user's notification preferences
     */
    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreference> getPreferences(
            @RequestHeader("X-User-Id") UUID userId) {

        NotificationPreference preferences = notificationService.getPreferences(userId);
        return ResponseEntity.ok(preferences);
    }

    /**
     * Create or update notification preferences for the authenticated user.
     *
     * @param userId     the authenticated user's ID (from gateway header)
     * @param preference the preference settings to apply
     * @return the persisted preferences
     */
    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreference> updatePreferences(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody NotificationPreference preference) {

        NotificationPreference updated = notificationService.updatePreferences(userId, preference);
        log.info("Updated notification preferences for user {}", userId);

        return ResponseEntity.ok(updated);
    }
}
