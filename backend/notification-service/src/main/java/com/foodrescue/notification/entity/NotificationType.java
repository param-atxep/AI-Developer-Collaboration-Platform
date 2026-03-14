package com.foodrescue.notification.entity;

/**
 * Enumeration of all notification types supported by the platform.
 * Each type corresponds to a specific event in the food rescue workflow.
 */
public enum NotificationType {

    /**
     * Sent when new food becomes available near a user's location.
     */
    FOOD_AVAILABLE,

    /**
     * Sent when a food listing has been claimed by a recipient.
     */
    FOOD_CLAIMED,

    /**
     * Sent when a pickup has been scheduled for a food donation.
     */
    PICKUP_SCHEDULED,

    /**
     * Sent when a pickup has been successfully completed.
     */
    PICKUP_COMPLETED,

    /**
     * Sent when a scheduled pickup has been cancelled.
     */
    PICKUP_CANCELLED,

    /**
     * System-level alerts such as maintenance windows, policy changes, etc.
     */
    SYSTEM_ALERT
}
