package com.foodrescue.pickup.repository;

import com.foodrescue.pickup.entity.Pickup;
import com.foodrescue.pickup.entity.PickupStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PickupRepository extends JpaRepository<Pickup, UUID> {

    List<Pickup> findByClaimerIdOrderByCreatedAtDesc(UUID claimerId);

    List<Pickup> findByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId);

    List<Pickup> findByStatusOrderByScheduledPickupTimeAsc(PickupStatus status);

    Optional<Pickup> findByFoodListingId(UUID foodListingId);

    List<Pickup> findByClaimerIdAndStatus(UUID claimerId, PickupStatus status);

    List<Pickup> findByRestaurantIdAndStatus(UUID restaurantId, PickupStatus status);

    boolean existsByFoodListingIdAndStatusNot(UUID foodListingId, PickupStatus status);
}
