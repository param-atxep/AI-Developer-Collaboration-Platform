package com.foodrescue.user.repository;

import com.foodrescue.user.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    Optional<Restaurant> findByUserProfileId(UUID userProfileId);

    boolean existsByUserProfileId(UUID userProfileId);

    boolean existsByLicenseNumber(String licenseNumber);

    List<Restaurant> findByCuisineType(String cuisineType);

    List<Restaurant> findByUserProfileIdIn(List<UUID> userProfileIds);
}
