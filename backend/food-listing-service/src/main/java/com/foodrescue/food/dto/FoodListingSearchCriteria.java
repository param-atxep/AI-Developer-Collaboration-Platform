package com.foodrescue.food.dto;

import com.foodrescue.food.entity.FoodCategory;
import com.foodrescue.food.entity.FoodStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodListingSearchCriteria {

    private FoodCategory category;
    private FoodStatus status;
    private Double lat;
    private Double lng;
    private Double radiusKm;
    private Boolean vegetarian;
    private Boolean vegan;
    private Boolean halal;
}
