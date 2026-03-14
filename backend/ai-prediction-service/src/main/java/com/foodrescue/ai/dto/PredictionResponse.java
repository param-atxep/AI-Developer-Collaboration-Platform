package com.foodrescue.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {

    private UUID restaurantId;
    private LocalDate date;
    private double predictedWasteKg;
    private double confidence;
    private String modelVersion;

    /**
     * Breakdown by food category, e.g. {"bakery": 5.2, "produce": 3.8, "dairy": 2.1}
     */
    private Map<String, Double> categoryBreakdown;

    /**
     * Actionable recommendations based on pattern analysis.
     */
    private List<String> recommendations;

    /**
     * Contributing factors used in the prediction.
     */
    private Map<String, String> factors;
}
