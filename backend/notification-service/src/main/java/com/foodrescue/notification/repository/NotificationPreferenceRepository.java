package com.foodrescue.notification.repository;

import com.foodrescue.notification.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link NotificationPreference} entities.
 */
@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {

    /**
     * Find the notification preferences for a specific user.
     *
     * @param userId the user's unique identifier
     * @return the user's preferences, or empty if not yet configured
     */
    Optional<NotificationPreference> findByUserId(UUID userId);

    /**
     * Check whether a preference record exists for the given user.
     */
    boolean existsByUserId(UUID userId);
}
