package com.foodrescue.analytics.service;

import com.foodrescue.analytics.dto.DashboardResponse;
import com.foodrescue.analytics.dto.RestaurantAnalyticsResponse;
import com.foodrescue.analytics.dto.TimeSeriesDataPoint;
import com.foodrescue.analytics.entity.RestaurantAnalytics.Period;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AnalyticsService {

    /**
     * Get the platform-wide dashboard summary including total food saved,
     * total meals, CO2 saved, monetary value, active participants, and recent activity.
     */
    DashboardResponse getDashboardStats();

    /**
     * Get analytics for a specific restaurant, including summary stats,
     * waste analysis, and time series data.
     */
    RestaurantAnalyticsResponse getRestaurantAnalytics(UUID restaurantId);

    /**
     * Get time series data for food saved across the platform within a date range.
     */
    List<TimeSeriesDataPoint> getFoodSavedTimeSeries(LocalDate from, LocalDate to);

    /**
     * Get the leaderboard of top restaurants ranked by food saved.
     */
    List<Map<String, Object>> getLeaderboard(int limit);

    /**
     * Get environmental impact data: CO2 saved, meals provided, monetary value.
     */
    Map<String, Object> getEnvironmentalImpact();

    /**
     * Get trend data for a given period (DAILY, WEEKLY, MONTHLY) showing
     * waste reduction and food saved trends across the platform.
     */
    List<TimeSeriesDataPoint> getTrends(Period period);

    /**
     * Record a new food listing event for analytics tracking.
     */
    void recordFoodListed(UUID restaurantId, double foodKg, double monetaryValue);

    /**
     * Record a food claimed event for analytics tracking.
     */
    void recordFoodClaimed(UUID restaurantId, double foodKg);

    /**
     * Record a completed pickup event for analytics tracking.
     */
    void recordPickupCompleted(UUID restaurantId, double foodKg,
                                double co2SavedKg, int mealsProvided,
                                double monetaryValue);
}
