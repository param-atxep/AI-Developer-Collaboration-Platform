package com.foodrescue.analytics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestaurantAnalyticsResponse implements Serializable {

    private UUID restaurantId;
    private String restaurantName;

    private SummaryStats summary;
    private WasteAnalysis wasteAnalysis;
    private List<TimeSeriesDataPoint> foodSavedTimeSeries;
    private List<TimeSeriesDataPoint> wasteReductionTimeSeries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryStats implements Serializable {
        private int totalListings;
        private int totalClaimed;
        private int totalPickedUp;
        private int totalExpired;
        private double totalFoodSavedKg;
        private double totalCo2SavedKg;
        private int totalMealsProvided;
        private BigDecimal totalMonetaryValueSaved;
        private double claimRate;
        private double pickupRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WasteAnalysis implements Serializable {
        private double wasteGenerated;
        private double wasteRedirected;
        private double wasteReductionPercent;
        private List<String> topCategories;
        private Map<String, Double> peakWasteHours;
        private LocalDate periodStart;
        private LocalDate periodEnd;
    }
}
