package com.foodrescue.user.repository;

import com.foodrescue.user.entity.NgoProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NgoProfileRepository extends JpaRepository<NgoProfile, UUID> {

    Optional<NgoProfile> findByUserProfileId(UUID userProfileId);

    boolean existsByUserProfileId(UUID userProfileId);

    boolean existsByRegistrationNumber(String registrationNumber);

    List<NgoProfile> findByUserProfileIdIn(List<UUID> userProfileIds);

    List<NgoProfile> findByServiceAreaContainingIgnoreCase(String area);
}
