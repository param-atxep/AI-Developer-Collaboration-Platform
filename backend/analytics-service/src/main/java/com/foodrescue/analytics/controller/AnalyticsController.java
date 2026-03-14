package com.foodrescue.analytics.controller;

import com.foodrescue.analytics.dto.DashboardResponse;
import com.foodrescue.analytics.dto.RestaurantAnalyticsResponse;
import com.foodrescue.analytics.dto.TimeSeriesDataPoint;
import com.foodrescue.analytics.entity.RestaurantAnalytics.Period;
import com.foodrescue.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * GET /api/analytics/dashboard
     * Returns the platform-wide dashboard overview including total food saved,
     * meals provided, CO2 saved, monetary value, active participants, and recent activity.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        log.info("GET /api/analytics/dashboard");
        DashboardResponse dashboard = analyticsService.getDashboardStats();
        return ResponseEntity.ok(dashboard);
    }

    /**
     * GET /api/analytics/restaurant/{id}
     * Returns analytics specific to a restaurant, including summary stats,
     * waste analysis, and time series data for the last 30 days.
     */
    @GetMapping("/restaurant/{id}")
    public ResponseEntity<RestaurantAnalyticsResponse> getRestaurantAnalytics(
            @PathVariable("id") UUID restaurantId) {
        log.info("GET /api/analytics/restaurant/{}", restaurantId);
        RestaurantAnalyticsResponse response = analyticsService.getRestaurantAnalytics(restaurantId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/analytics/food-saved?from=YYYY-MM-DD&to=YYYY-MM-DD
     * Returns time series data for food saved across the platform within the specified date range.
     * Defaults to the last 30 days if no dates are provided.
     */
    @GetMapping("/food-saved")
    public ResponseEntity<List<TimeSeriesDataPoint>> getFoodSavedTimeSeries(
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        if (to == null) {
            to = LocalDate.now();
        }
        if (from == null) {
            from = to.minusDays(30);
        }

        log.info("GET /api/analytics/food-saved?from={}&to={}", from, to);
        List<TimeSeriesDataPoint> timeSeries = analyticsService.getFoodSavedTimeSeries(from, to);
        return ResponseEntity.ok(timeSeries);
    }

    /**
     * GET /api/analytics/leaderboard?limit=10
     * Returns a ranked list of top restaurants by total food saved in the last 30 days.
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        log.info("GET /api/analytics/leaderboard?limit={}", limit);

        if (limit < 1) {
            limit = 10;
        }
        if (limit > 100) {
            limit = 100;
        }

        List<Map<String, Object>> leaderboard = analyticsService.getLeaderboard(limit);
        return ResponseEntity.ok(leaderboard);
    }

    /**
     * GET /api/analytics/impact
     * Returns the global environmental impact data: total CO2 saved, meals provided,
     * monetary value saved, equivalents (trees planted, car miles offset), and a
     * 6-month monthly breakdown.
     */
    @GetMapping("/impact")
    public ResponseEntity<Map<String, Object>> getEnvironmentalImpact() {
        log.info("GET /api/analytics/impact");
        Map<String, Object> impact = analyticsService.getEnvironmentalImpact();
        return ResponseEntity.ok(impact);
    }

    /**
     * GET /api/analytics/trends?period=DAILY|WEEKLY|MONTHLY
     * Returns trend data showing waste reduction and food saved across the platform
     * for the given period granularity.
     * - DAILY: last 30 days
     * - WEEKLY: last 12 weeks
     * - MONTHLY: last 12 months
     */
    @GetMapping("/trends")
    public ResponseEntity<List<TimeSeriesDataPoint>> getTrends(
            @RequestParam(value = "period", defaultValue = "DAILY") Period period) {
        log.info("GET /api/analytics/trends?period={}", period);
        List<TimeSeriesDataPoint> trends = analyticsService.getTrends(period);
        return ResponseEntity.ok(trends);
    }
}
