package com.foodrescue.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Persistent entity storing per-user notification preferences.
 * Controls which channels and alert types a user has opted into.
 */
@Entity
@Table(name = "notification_preferences", indexes = {
        @Index(name = "idx_preference_user_id", columnList = "userId", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    /**
     * Whether the user wants to receive notifications via email.
     */
    @Column(name = "email_enabled", nullable = false)
    @Builder.Default
    private boolean emailEnabled = true;

    /**
     * Whether the user wants to receive push notifications.
     */
    @Column(name = "push_enabled", nullable = false)
    @Builder.Default
    private boolean pushEnabled = true;

    /**
     * Whether the user wants to receive real-time WebSocket notifications.
     */
    @Column(name = "websocket_enabled", nullable = false)
    @Builder.Default
    private boolean websocketEnabled = true;

    /**
     * Whether the user wants alerts when new food becomes available nearby.
     */
    @Column(name = "food_available_alert", nullable = false)
    @Builder.Default
    private boolean foodAvailableAlert = true;

    /**
     * Whether the user wants reminders about scheduled pickups.
     */
    @Column(name = "pickup_reminder", nullable = false)
    @Builder.Default
    private boolean pickupReminder = true;

    /**
     * Whether the user wants to receive system-level alerts.
     */
    @Column(name = "system_alerts", nullable = false)
    @Builder.Default
    private boolean systemAlerts = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
