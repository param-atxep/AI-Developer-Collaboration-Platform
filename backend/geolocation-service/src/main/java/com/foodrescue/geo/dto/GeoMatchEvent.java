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
public class GeoMatchEvent implements Serializable {

    private UUID foodListingId;
    private UUID matchedEntityId;
    private GeoLocation.EntityType matchedEntityType;
    private double distanceKm;
    private double latitude;
    private double longitude;
    private String address;
    private String city;
    private LocalDateTime matchedAt;
}
