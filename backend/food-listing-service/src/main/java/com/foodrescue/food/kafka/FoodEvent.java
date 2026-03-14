package com.foodrescue.food.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventType;
    private UUID foodListingId;
    private UUID restaurantId;
    private String title;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;

    public static FoodEvent of(String eventType, UUID foodListingId, UUID restaurantId,
                                String title, Double latitude, Double longitude) {
        return FoodEvent.builder()
                .eventType(eventType)
                .foodListingId(foodListingId)
                .restaurantId(restaurantId)
                .title(title)
                .latitude(latitude)
                .longitude(longitude)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
