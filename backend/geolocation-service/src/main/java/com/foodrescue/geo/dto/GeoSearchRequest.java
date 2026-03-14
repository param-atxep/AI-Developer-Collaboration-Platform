package com.foodrescue.geo.dto;

import com.foodrescue.geo.entity.GeoLocation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeoSearchRequest {

    @NotNull(message = "Latitude is required")
    @Min(value = -90, message = "Latitude must be between -90 and 90")
    @Max(value = 90, message = "Latitude must be between -90 and 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Min(value = -180, message = "Longitude must be between -180 and 180")
    @Max(value = 180, message = "Longitude must be between -180 and 180")
    private Double longitude;

    @Builder.Default
    private Double radiusKm = 10.0;

    private GeoLocation.EntityType entityType;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;
}
