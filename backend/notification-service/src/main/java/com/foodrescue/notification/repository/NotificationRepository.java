package com.foodrescue.notification.repository;

import com.foodrescue.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Notification} entities.
 * Provides paginated queries, unread counts, and bulk update operations.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Retrieve all notifications for a user, ordered by creation date descending.
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Retrieve notifications for a user filtered by read status,
     * ordered by creation date descending.
     */
    Page<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(UUID userId, boolean isRead, Pageable pageable);

    /**
     * Count unread notifications for a specific user.
     */
    long countByUserIdAndIsRead(UUID userId, boolean isRead);

    /**
     * Mark all unread notifications as read for a specific user.
     *
     * @param userId the target user
     * @return the number of rows updated
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") UUID userId);
}
