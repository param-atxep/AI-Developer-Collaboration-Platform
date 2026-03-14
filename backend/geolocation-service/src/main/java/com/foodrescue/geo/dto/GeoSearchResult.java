package com.foodrescue.geo.dto;

import com.foodrescue.geo.entity.GeoLocation;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeoSearchResult implements Serializable {

    private UUID id;
    private UUID entityId;
    private GeoLocation.EntityType entityType;
    private double latitude;
    private double longitude;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private double distanceKm;
    private LocalDateTime lastUpdated;

    public static GeoSearchResult fromEntity(GeoLocation entity, double distanceKm) {
        return GeoSearchResult.builder()
                .id(entity.getId())
                .entityId(entity.getEntityId())
                .entityType(entity.getEntityType())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .address(entity.getAddress())
                .city(entity.getCity())
                .state(entity.getState())
                .country(entity.getCountry())
                .postalCode(entity.getPostalCode())
                .distanceKm(distanceKm)
                .lastUpdated(entity.getLastUpdated())
                .build();
    }
}
