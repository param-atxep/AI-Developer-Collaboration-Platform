package com.foodrescue.ai.service;

import com.foodrescue.ai.dto.PredictionResponse;
import com.foodrescue.ai.dto.WasteTrendResponse;
import com.foodrescue.ai.entity.WasteHistory;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PredictionService {

    /**
     * Predict waste for a specific restaurant on a given date.
     * Uses TensorFlow model if available, otherwise falls back to a statistical model.
     */
    PredictionResponse predictWaste(UUID restaurantId, LocalDate date, boolean includeBreakdown);

    /**
     * Analyze waste trends for a restaurant over a number of past days.
     */
    WasteTrendResponse getWasteTrend(UUID restaurantId, int days);

    /**
     * Generate actionable recommendations based on historical waste patterns.
     */
    List<String> generateRecommendations(UUID restaurantId);

    /**
     * Retrain the prediction model with the latest historical data.
     */
    void updateModel();

    /**
     * Retrieve waste history entries for a restaurant.
     */
    List<WasteHistory> getWasteHistory(UUID restaurantId);
}
