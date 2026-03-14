package com.foodrescue.food.dto;

import com.foodrescue.food.entity.FoodCategory;
import com.foodrescue.food.entity.FoodStatus;
import com.foodrescue.food.entity.QuantityUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodListingResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID restaurantId;
    private String title;
    private String description;
    private FoodCategory foodCategory;
    private Double quantity;
    private QuantityUnit unit;
    private BigDecimal originalPrice;
    private LocalDateTime expiresAt;
    private FoodStatus status;
    private String imageUrl;
    private String allergens;
    private Boolean isVegetarian;
    private Boolean isVegan;
    private Boolean isHalal;
    private Double latitude;
    private Double longitude;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Double distanceKm;
}
