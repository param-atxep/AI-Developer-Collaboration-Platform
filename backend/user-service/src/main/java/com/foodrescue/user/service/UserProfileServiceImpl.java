package com.foodrescue.user.service;

import com.foodrescue.user.dto.RestaurantDto;
import com.foodrescue.user.dto.UpdateProfileRequest;
import com.foodrescue.user.dto.UserProfileDto;
import com.foodrescue.user.entity.NgoProfile;
import com.foodrescue.user.entity.Restaurant;
import com.foodrescue.user.entity.UserProfile;
import com.foodrescue.user.exception.ResourceNotFoundException;
import com.foodrescue.user.repository.NgoProfileRepository;
import com.foodrescue.user.repository.RestaurantRepository;
import com.foodrescue.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserProfileServiceImpl implements UserProfileService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final UserProfileRepository userProfileRepository;
    private final RestaurantRepository restaurantRepository;
    private final NgoProfileRepository ngoProfileRepository;

    @Override
    public UserProfileDto getProfileById(UUID id) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "id", id));
        return mapToDto(profile);
    }

    @Override
    public UserProfileDto getProfileByUserId(UUID userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));
        return mapToDto(profile);
    }

    @Override
    @Transactional
    public UserProfileDto createProfile(UserProfileDto profileDto) {
        if (profileDto.getUserId() == null) {
            throw new IllegalArgumentException("userId is required to create a profile");
        }

        if (userProfileRepository.existsByUserId(profileDto.getUserId())) {
            throw new IllegalArgumentException(
                    "A profile already exists for userId: " + profileDto.getUserId());
        }

        UserProfile profile = UserProfile.builder()
                .userId(profileDto.getUserId())
                .organizationName(profileDto.getOrganizationName())
                .description(profileDto.getDescription())
                .address(profileDto.getAddress())
                .latitude(profileDto.getLatitude())
                .longitude(profileDto.getLongitude())
                .profileImageUrl(profileDto.getProfileImageUrl())
                .build();

        UserProfile saved = userProfileRepository.save(profile);
        log.info("Created user profile with id={} for userId={}", saved.getId(), saved.getUserId());

        // Create restaurant sub-profile if restaurant details are provided
        if (profileDto.getRestaurantDetails() != null) {
            RestaurantDto rd = profileDto.getRestaurantDetails();
            Restaurant restaurant = Restaurant.builder()
                    .userProfileId(saved.getId())
                    .cuisineType(rd.getCuisineType())
                    .operatingHours(rd.getOperatingHours())
                    .foodSafetyRating(rd.getFoodSafetyRating())
                    .licenseNumber(rd.getLicenseNumber())
                    .averageWastePerDay(rd.getAverageWastePerDay())
                    .build();
            restaurantRepository.save(restaurant);
            log.info("Created restaurant profile for userProfileId={}", saved.getId());
        }

        // Create NGO sub-profile if NGO details are provided
        if (profileDto.getNgoDetails() != null) {
            UserProfileDto.NgoProfileDto nd = profileDto.getNgoDetails();
            NgoProfile ngoProfile = NgoProfile.builder()
                    .userProfileId(saved.getId())
                    .registrationNumber(nd.getRegistrationNumber())
                    .serviceArea(nd.getServiceArea())
                    .capacity(nd.getCapacity())
                    .peopleServed(nd.getPeopleServed())
                    .build();
            ngoProfileRepository.save(ngoProfile);
            log.info("Created NGO profile for userProfileId={}", saved.getId());
        }

        return mapToDto(saved);
    }

    @Override
    @Transactional
    public UserProfileDto updateProfile(UUID id, UpdateProfileRequest request) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "id", id));

        // Update base profile fields (only non-null values)
        if (request.getOrganizationName() != null) {
            profile.setOrganizationName(request.getOrganizationName());
        }
        if (request.getDescription() != null) {
            profile.setDescription(request.getDescription());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        if (request.getLatitude() != null) {
            profile.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            profile.setLongitude(request.getLongitude());
        }
        if (request.getProfileImageUrl() != null) {
            profile.setProfileImageUrl(request.getProfileImageUrl());
        }

        UserProfile updated = userProfileRepository.save(profile);

        // Update restaurant sub-profile if restaurant-specific fields are provided
        updateRestaurantIfPresent(updated.getId(), request);

        // Update NGO sub-profile if NGO-specific fields are provided
        updateNgoIfPresent(updated.getId(), request);

        log.info("Updated user profile id={}", id);
        return mapToDto(updated);
    }

    @Override
    @Transactional
    public void deactivateProfile(UUID id) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "id", id));
        profile.setIsActive(false);
        userProfileRepository.save(profile);
        log.info("Deactivated user profile id={}", id);
    }

    @Override
    public List<RestaurantDto> getNearbyRestaurants(double latitude, double longitude, double radiusKm) {
        List<UserProfile> nearbyProfiles = userProfileRepository.findNearbyProfiles(latitude, longitude, radiusKm);

        if (nearbyProfiles.isEmpty()) {
            return List.of();
        }

        // Get profile IDs and fetch associated restaurant records
        List<UUID> profileIds = nearbyProfiles.stream()
                .map(UserProfile::getId)
                .collect(Collectors.toList());

        List<Restaurant> restaurants = restaurantRepository.findByUserProfileIdIn(profileIds);

        // Build a map for quick lookup: userProfileId -> Restaurant
        Map<UUID, Restaurant> restaurantMap = restaurants.stream()
                .collect(Collectors.toMap(Restaurant::getUserProfileId, Function.identity()));

        // Only return profiles that have a corresponding restaurant record
        List<RestaurantDto> result = new ArrayList<>();
        for (UserProfile profile : nearbyProfiles) {
            Restaurant restaurant = restaurantMap.get(profile.getId());
            if (restaurant != null) {
                double distance = calculateHaversineDistance(latitude, longitude,
                        profile.getLatitude(), profile.getLongitude());
                result.add(mapToRestaurantDto(profile, restaurant, distance));
            }
        }

        return result;
    }

    @Override
    public List<UserProfileDto> getNearbyNgos(double latitude, double longitude, double radiusKm) {
        List<UserProfile> nearbyProfiles = userProfileRepository.findNearbyProfiles(latitude, longitude, radiusKm);

        if (nearbyProfiles.isEmpty()) {
            return List.of();
        }

        List<UUID> profileIds = nearbyProfiles.stream()
                .map(UserProfile::getId)
                .collect(Collectors.toList());

        List<NgoProfile> ngos = ngoProfileRepository.findByUserProfileIdIn(profileIds);

        // Build a set of profile IDs that are NGOs
        Map<UUID, NgoProfile> ngoMap = ngos.stream()
                .collect(Collectors.toMap(NgoProfile::getUserProfileId, Function.identity()));

        // Only return profiles that have a corresponding NGO record
        List<UserProfileDto> result = new ArrayList<>();
        for (UserProfile profile : nearbyProfiles) {
            NgoProfile ngo = ngoMap.get(profile.getId());
            if (ngo != null) {
                UserProfileDto dto = mapToDto(profile);
                dto.setNgoDetails(mapToNgoDto(ngo));
                result.add(dto);
            }
        }

        return result;
    }

    @Override
    @Transactional
    public void incrementDonations(UUID profileId) {
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "id", profileId));
        profile.setTotalDonations(profile.getTotalDonations() + 1);
        userProfileRepository.save(profile);
        log.info("Incremented donations for profile id={}, new total={}", profileId, profile.getTotalDonations());
    }

    @Override
    @Transactional
    public void incrementPickups(UUID profileId) {
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "id", profileId));
        profile.setTotalPickups(profile.getTotalPickups() + 1);
        userProfileRepository.save(profile);
        log.info("Incremented pickups for profile id={}, new total={}", profileId, profile.getTotalPickups());
    }

    @Override
    @Transactional
    public void updateRating(UUID profileId, double newRating) {
        if (newRating < 0.0 || newRating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 0.0 and 5.0");
        }
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "id", profileId));
        profile.setRating(newRating);
        userProfileRepository.save(profile);
        log.info("Updated rating for profile id={} to {}", profileId, newRating);
    }

    // ---- Private helper methods ----

    private void updateRestaurantIfPresent(UUID userProfileId, UpdateProfileRequest request) {
        boolean hasRestaurantFields = request.getCuisineType() != null
                || request.getOperatingHours() != null
                || request.getFoodSafetyRating() != null
                || request.getLicenseNumber() != null
                || request.getAverageWastePerDay() != null;

        if (!hasRestaurantFields) {
            return;
        }

        Optional<Restaurant> optRestaurant = restaurantRepository.findByUserProfileId(userProfileId);
        Restaurant restaurant = optRestaurant.orElseGet(() -> Restaurant.builder()
                .userProfileId(userProfileId)
                .build());

        if (request.getCuisineType() != null) {
            restaurant.setCuisineType(request.getCuisineType());
        }
        if (request.getOperatingHours() != null) {
            restaurant.setOperatingHours(request.getOperatingHours());
        }
        if (request.getFoodSafetyRating() != null) {
            restaurant.setFoodSafetyRating(request.getFoodSafetyRating());
        }
        if (request.getLicenseNumber() != null) {
            restaurant.setLicenseNumber(request.getLicenseNumber());
        }
        if (request.getAverageWastePerDay() != null) {
            restaurant.setAverageWastePerDay(request.getAverageWastePerDay());
        }

        restaurantRepository.save(restaurant);
    }

    private void updateNgoIfPresent(UUID userProfileId, UpdateProfileRequest request) {
        boolean hasNgoFields = request.getRegistrationNumber() != null
                || request.getServiceArea() != null
                || request.getCapacity() != null;

        if (!hasNgoFields) {
            return;
        }

        Optional<NgoProfile> optNgo = ngoProfileRepository.findByUserProfileId(userProfileId);
        NgoProfile ngo = optNgo.orElseGet(() -> NgoProfile.builder()
                .userProfileId(userProfileId)
                .build());

        if (request.getRegistrationNumber() != null) {
            ngo.setRegistrationNumber(request.getRegistrationNumber());
        }
        if (request.getServiceArea() != null) {
            ngo.setServiceArea(request.getServiceArea());
        }
        if (request.getCapacity() != null) {
            ngo.setCapacity(request.getCapacity());
        }

        ngoProfileRepository.save(ngo);
    }

    private UserProfileDto mapToDto(UserProfile profile) {
        UserProfileDto dto = UserProfileDto.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .organizationName(profile.getOrganizationName())
                .description(profile.getDescription())
                .address(profile.getAddress())
                .latitude(profile.getLatitude())
                .longitude(profile.getLongitude())
                .profileImageUrl(profile.getProfileImageUrl())
                .rating(profile.getRating())
                .totalDonations(profile.getTotalDonations())
                .totalPickups(profile.getTotalPickups())
                .isActive(profile.getIsActive())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();

        // Eagerly load restaurant details if present
        restaurantRepository.findByUserProfileId(profile.getId()).ifPresent(restaurant -> {
            dto.setRestaurantDetails(RestaurantDto.builder()
                    .id(restaurant.getId())
                    .userProfileId(restaurant.getUserProfileId())
                    .cuisineType(restaurant.getCuisineType())
                    .operatingHours(restaurant.getOperatingHours())
                    .foodSafetyRating(restaurant.getFoodSafetyRating())
                    .licenseNumber(restaurant.getLicenseNumber())
                    .averageWastePerDay(restaurant.getAverageWastePerDay())
                    .build());
        });

        // Eagerly load NGO details if present
        ngoProfileRepository.findByUserProfileId(profile.getId()).ifPresent(ngo -> {
            dto.setNgoDetails(mapToNgoDto(ngo));
        });

        return dto;
    }

    private RestaurantDto mapToRestaurantDto(UserProfile profile, Restaurant restaurant, double distanceKm) {
        return RestaurantDto.builder()
                .id(restaurant.getId())
                .userProfileId(restaurant.getUserProfileId())
                .cuisineType(restaurant.getCuisineType())
                .operatingHours(restaurant.getOperatingHours())
                .foodSafetyRating(restaurant.getFoodSafetyRating())
                .licenseNumber(restaurant.getLicenseNumber())
                .averageWastePerDay(restaurant.getAverageWastePerDay())
                .organizationName(profile.getOrganizationName())
                .address(profile.getAddress())
                .latitude(profile.getLatitude())
                .longitude(profile.getLongitude())
                .rating(profile.getRating())
                .profileImageUrl(profile.getProfileImageUrl())
                .distanceKm(Math.round(distanceKm * 100.0) / 100.0)
                .build();
    }

    private UserProfileDto.NgoProfileDto mapToNgoDto(NgoProfile ngo) {
        return UserProfileDto.NgoProfileDto.builder()
                .id(ngo.getId())
                .registrationNumber(ngo.getRegistrationNumber())
                .serviceArea(ngo.getServiceArea())
                .capacity(ngo.getCapacity())
                .peopleServed(ngo.getPeopleServed())
                .build();
    }

    /**
     * Calculates the distance between two geographic coordinates using the Haversine formula.
     *
     * @return distance in kilometers
     */
    private double calculateHaversineDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
