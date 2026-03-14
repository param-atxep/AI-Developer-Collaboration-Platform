package com.foodrescue.notification.entity;

/**
 * Enumeration of delivery channels for notifications.
 */
public enum NotificationChannel {

    /**
     * Real-time delivery via WebSocket (STOMP).
     */
    WEBSOCKET,

    /**
     * Delivery via email using Spring Mail.
     */
    EMAIL,

    /**
     * Delivery via push notification (e.g., Firebase Cloud Messaging).
     */
    PUSH
}
