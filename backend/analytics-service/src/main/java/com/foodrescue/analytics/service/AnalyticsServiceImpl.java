package com.foodrescue.analytics.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodrescue.analytics.dto.DashboardResponse;
import com.foodrescue.analytics.dto.RestaurantAnalyticsResponse;
import com.foodrescue.analytics.dto.TimeSeriesDataPoint;
import com.foodrescue.analytics.entity.FoodSavedMetric;
import com.foodrescue.analytics.entity.PlatformMetric;
import com.foodrescue.analytics.entity.RestaurantAnalytics;
import com.foodrescue.analytics.entity.RestaurantAnalytics.Period;
import com.foodrescue.analytics.repository.FoodSavedMetricRepository;
import com.foodrescue.analytics.repository.PlatformMetricRepository;
import com.foodrescue.analytics.repository.RestaurantAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final FoodSavedMetricRepository foodSavedMetricRepository;
    private final PlatformMetricRepository platformMetricRepository;
    private final RestaurantAnalyticsRepository restaurantAnalyticsRepository;
    private final ObjectMapper objectMapper;

    private static final double CO2_PER_KG_FOOD = 2.5;
    private static final double MEALS_PER_KG_FOOD = 2.0;
    private static final double MONETARY_VALUE_PER_KG = 4.5;

    @Override
    @Cacheable(value = "dashboard", key = "'platform-dashboard'")
    public DashboardResponse getDashboardStats() {
        log.info("Computing platform dashboard stats");

        double totalFoodSaved = foodSavedMetricRepository.sumTotalFoodSavedKg();
        double totalCo2Saved = foodSavedMetricRepository.sumTotalCo2SavedKg();
        long totalMeals = foodSavedMetricRepository.sumTotalMealsProvided();
        BigDecimal totalMoneyValue = foodSavedMetricRepository.sumTotalMonetaryValueSaved();

        Optional<PlatformMetric> latestMetric = platformMetricRepository.findLatest();
        long activeRestaurants = 0;
        long activeNgos = 0;

        if (latestMetric.isPresent()) {
            PlatformMetric pm = latestMetric.get();
            activeRestaurants = pm.getTotalRestaurants();
            activeNgos = pm.getTotalNgos();
        }

        // Build recent activity from last 7 days of food saved metrics
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        List<FoodSavedMetric> recentMetrics = foodSavedMetricRepository
                .findByDateBetweenOrderByDateAsc(sevenDaysAgo, LocalDate.now());

        List<DashboardResponse.RecentActivityItem> recentActivity = recentMetrics.stream()
                .filter(m -> m.getTotalPickedUp() > 0)
                .map(m -> DashboardResponse.RecentActivityItem.builder()
                        .type("PICKUP_COMPLETED")
                        .description(String.format("%.1f kg of food rescued", m.getFoodSavedKg()))
                        .foodKg(m.getFoodSavedKg())
                        .timestamp(m.getUpdatedAt())
                        .build())
                .limit(20)
                .toList();

        return DashboardResponse.builder()
                .totalFoodSaved(totalFoodSaved)
                .totalMeals(totalMeals)
                .totalCo2Saved(totalCo2Saved)
                .totalMoneyValue(totalMoneyValue != null ? totalMoneyValue : BigDecimal.ZERO)
                .activeRestaurants(activeRestaurants)
                .activeNgos(activeNgos)
                .recentActivity(recentActivity)
                .build();
    }

    @Override
    @Cacheable(value = "restaurant-analytics", key = "#restaurantId")
    public RestaurantAnalyticsResponse getRestaurantAnalytics(UUID restaurantId) {
        log.info("Computing analytics for restaurant: {}", restaurantId);

        LocalDate now = LocalDate.now();
        LocalDate thirtyDaysAgo = now.minusDays(30);

        // Aggregate summary stats
        Object[] agg = foodSavedMetricRepository.aggregateByRestaurantAndDateRange(
                restaurantId, thirtyDaysAgo, now);

        RestaurantAnalyticsResponse.SummaryStats summary = buildSummaryStats(agg);

        // Waste analysis from latest restaurant analytics record
        RestaurantAnalyticsResponse.WasteAnalysis wasteAnalysis = null;
        Optional<RestaurantAnalytics> latestAnalytics = restaurantAnalyticsRepository
                .findTopByRestaurantIdAndPeriodOrderByPeriodStartDesc(
                        restaurantId, Period.MONTHLY);

        if (latestAnalytics.isPresent()) {
            wasteAnalysis = buildWasteAnalysis(latestAnalytics.get());
        }

        // Food saved time series for the restaurant
        List<FoodSavedMetric> metrics = foodSavedMetricRepository
                .findByRestaurantIdAndDateBetweenOrderByDateAsc(
                        restaurantId, thirtyDaysAgo, now);

        List<TimeSeriesDataPoint> foodSavedTimeSeries = metrics.stream()
                .map(m -> TimeSeriesDataPoint.builder()
                        .date(m.getDate())
                        .value(m.getFoodSavedKg())
                        .label("Food Saved (kg)")
                        .build())
                .toList();

        // Waste reduction time series
        List<Object[]> reductionTrend = restaurantAnalyticsRepository.wasteReductionTrend(
                restaurantId, Period.DAILY, thirtyDaysAgo, now);

        List<TimeSeriesDataPoint> wasteReductionTimeSeries = reductionTrend.stream()
                .map(row -> TimeSeriesDataPoint.builder()
                        .date((LocalDate) row[0])
                        .value(((Number) row[1]).doubleValue())
                        .label("Waste Reduction (%)")
                        .build())
                .toList();

        return RestaurantAnalyticsResponse.builder()
                .restaurantId(restaurantId)
                .summary(summary)
                .wasteAnalysis(wasteAnalysis)
                .foodSavedTimeSeries(foodSavedTimeSeries)
                .wasteReductionTimeSeries(wasteReductionTimeSeries)
                .build();
    }

    @Override
    @Cacheable(value = "food-saved-timeseries", key = "#from.toString() + '-' + #to.toString()")
    public List<TimeSeriesDataPoint> getFoodSavedTimeSeries(LocalDate from, LocalDate to) {
        log.info("Computing food saved time series from {} to {}", from, to);

        List<Object[]> dailyData = foodSavedMetricRepository.dailyFoodSavedTimeSeries(from, to);

        return dailyData.stream()
                .map(row -> TimeSeriesDataPoint.builder()
                        .date((LocalDate) row[0])
                        .value(((Number) row[1]).doubleValue())
                        .label("Food Saved (kg)")
                        .build())
                .toList();
    }

    @Override
    @Cacheable(value = "leaderboard", key = "'top-' + #limit")
    public List<Map<String, Object>> getLeaderboard(int limit) {
        log.info("Computing leaderboard for top {} restaurants", limit);

        LocalDate now = LocalDate.now();
        LocalDate thirtyDaysAgo = now.minusDays(30);

        List<Object[]> topRestaurants = foodSavedMetricRepository.findTopRestaurantsByFoodSaved(
                thirtyDaysAgo, now, limit);

        List<Map<String, Object>> leaderboard = new ArrayList<>();
        int rank = 1;

        for (Object[] row : topRestaurants) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("rank", rank++);
            entry.put("restaurantId", row[0]);
            entry.put("totalFoodSavedKg", ((Number) row[1]).doubleValue());
            entry.put("totalMealsProvided", ((Number) row[2]).longValue());
            entry.put("totalCo2SavedKg", ((Number) row[3]).doubleValue());
            leaderboard.add(entry);
        }

        return leaderboard;
    }

    @Override
    @Cacheable(value = "environmental-impact", key = "'global-impact'")
    public Map<String, Object> getEnvironmentalImpact() {
        log.info("Computing global environmental impact");

        double totalFoodSaved = foodSavedMetricRepository.sumTotalFoodSavedKg();
        double totalCo2Saved = foodSavedMetricRepository.sumTotalCo2SavedKg();
        long totalMeals = foodSavedMetricRepository.sumTotalMealsProvided();
        BigDecimal totalMoney = foodSavedMetricRepository.sumTotalMonetaryValueSaved();

        // Equivalents for context
        double treesEquivalent = totalCo2Saved / 21.0; // avg tree absorbs ~21kg CO2/year
        double carMilesEquivalent = totalCo2Saved / 0.404; // avg car emits 0.404 kg CO2/mile

        Map<String, Object> impact = new LinkedHashMap<>();
        impact.put("totalFoodSavedKg", round(totalFoodSaved));
        impact.put("totalCo2SavedKg", round(totalCo2Saved));
        impact.put("totalMealsProvided", totalMeals);
        impact.put("totalMonetaryValueSaved", totalMoney != null ? totalMoney : BigDecimal.ZERO);
        impact.put("equivalents", Map.of(
                "treesPlantedEquivalent", Math.round(treesEquivalent),
                "carMilesOffsetEquivalent", Math.round(carMilesEquivalent)
        ));

        // Monthly breakdown for the last 6 months
        List<Map<String, Object>> monthlyBreakdown = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            double foodSaved = foodSavedMetricRepository.sumFoodSavedKgByDateRange(
                    monthStart, monthEnd);
            double co2Saved = foodSavedMetricRepository.sumCo2SavedKgByDateRange(
                    monthStart, monthEnd);
            long meals = foodSavedMetricRepository.sumMealsProvidedByDateRange(
                    monthStart, monthEnd);

            Map<String, Object> month = new LinkedHashMap<>();
            month.put("month", monthStart.toString());
            month.put("foodSavedKg", round(foodSaved));
            month.put("co2SavedKg", round(co2Saved));
            month.put("mealsProvided", meals);
            monthlyBreakdown.add(month);
        }
        impact.put("monthlyBreakdown", monthlyBreakdown);

        return impact;
    }

    @Override
    @Cacheable(value = "trends", key = "#period.name()")
    public List<TimeSeriesDataPoint> getTrends(Period period) {
        log.info("Computing trends for period: {}", period);

        LocalDate now = LocalDate.now();
        LocalDate startDate;

        switch (period) {
            case DAILY -> startDate = now.minusDays(30);
            case WEEKLY -> startDate = now.minusWeeks(12);
            case MONTHLY -> startDate = now.minusMonths(12);
            default -> startDate = now.minusDays(30);
        }

        List<Object[]> trendData = restaurantAnalyticsRepository.platformWasteReductionTrend(
                period, startDate, now);

        if (trendData.isEmpty()) {
            // Fallback to daily food saved data if no restaurant analytics exist
            List<Object[]> dailyData = foodSavedMetricRepository
                    .dailyFoodSavedTimeSeries(startDate, now);

            return dailyData.stream()
                    .map(row -> TimeSeriesDataPoint.builder()
                            .date((LocalDate) row[0])
                            .value(((Number) row[1]).doubleValue())
                            .label("Food Saved (kg) - " + period.name())
                            .build())
                    .toList();
        }

        return trendData.stream()
                .map(row -> TimeSeriesDataPoint.builder()
                        .date((LocalDate) row[0])
                        .value(((Number) row[2]).doubleValue())
                        .label("Waste Redirected (kg) - " + period.name())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = {"dashboard", "leaderboard", "environmental-impact",
            "food-saved-timeseries", "trends"}, allEntries = true)
    public void recordFoodListed(UUID restaurantId, double foodKg, double monetaryValue) {
        log.info("Recording food listed: restaurant={}, foodKg={}", restaurantId, foodKg);

        LocalDate today = LocalDate.now();
        FoodSavedMetric metric = getOrCreateMetric(restaurantId, today);
        metric.setTotalListings(metric.getTotalListings() + 1);
        metric.setUpdatedAt(LocalDateTime.now());
        foodSavedMetricRepository.save(metric);

        updatePlatformMetricOnListing();
    }

    @Override
    @Transactional
    @CacheEvict(value = {"dashboard", "leaderboard", "environmental-impact",
            "food-saved-timeseries", "trends"}, allEntries = true)
    public void recordFoodClaimed(UUID restaurantId, double foodKg) {
        log.info("Recording food claimed: restaurant={}, foodKg={}", restaurantId, foodKg);

        LocalDate today = LocalDate.now();
        FoodSavedMetric metric = getOrCreateMetric(restaurantId, today);
        metric.setTotalClaimed(metric.getTotalClaimed() + 1);
        metric.setUpdatedAt(LocalDateTime.now());
        foodSavedMetricRepository.save(metric);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"dashboard", "restaurant-analytics", "leaderboard",
            "environmental-impact", "food-saved-timeseries", "trends"}, allEntries = true)
    public void recordPickupCompleted(UUID restaurantId, double foodKg,
                                       double co2SavedKg, int mealsProvided,
                                       double monetaryValue) {
        log.info("Recording pickup completed: restaurant={}, foodKg={}, co2={}, meals={}",
                restaurantId, foodKg, co2SavedKg, mealsProvided);

        LocalDate today = LocalDate.now();
        FoodSavedMetric metric = getOrCreateMetric(restaurantId, today);

        metric.setTotalPickedUp(metric.getTotalPickedUp() + 1);
        metric.setFoodSavedKg(metric.getFoodSavedKg() + foodKg);
        metric.setCo2SavedKg(metric.getCo2SavedKg() + (co2SavedKg > 0 ? co2SavedKg : foodKg * CO2_PER_KG_FOOD));
        metric.setMealsProvided(metric.getMealsProvided() + (mealsProvided > 0 ? mealsProvided : (int)(foodKg * MEALS_PER_KG_FOOD)));
        metric.setMonetaryValueSaved(metric.getMonetaryValueSaved().add(
                monetaryValue > 0
                        ? BigDecimal.valueOf(monetaryValue)
                        : BigDecimal.valueOf(foodKg * MONETARY_VALUE_PER_KG)));
        metric.setUpdatedAt(LocalDateTime.now());

        foodSavedMetricRepository.save(metric);

        updatePlatformMetricOnPickup(foodKg, mealsProvided);
    }

    // ---- Private Helpers ----

    private FoodSavedMetric getOrCreateMetric(UUID restaurantId, LocalDate date) {
        return foodSavedMetricRepository.findByRestaurantIdAndDate(restaurantId, date)
                .orElseGet(() -> FoodSavedMetric.builder()
                        .restaurantId(restaurantId)
                        .date(date)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());
    }

    private void updatePlatformMetricOnListing() {
        LocalDate today = LocalDate.now();
        PlatformMetric pm = platformMetricRepository.findByDate(today)
                .orElseGet(() -> PlatformMetric.builder()
                        .date(today)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());

        pm.setTotalListings(pm.getTotalListings() + 1);
        pm.setUpdatedAt(LocalDateTime.now());
        platformMetricRepository.save(pm);
    }

    private void updatePlatformMetricOnPickup(double foodKg, int mealsProvided) {
        LocalDate today = LocalDate.now();
        PlatformMetric pm = platformMetricRepository.findByDate(today)
                .orElseGet(() -> PlatformMetric.builder()
                        .date(today)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());

        pm.setTotalPickups(pm.getTotalPickups() + 1);
        pm.setTotalFoodSavedKg(pm.getTotalFoodSavedKg() + foodKg);
        pm.setTotalMealsProvided(pm.getTotalMealsProvided() +
                (mealsProvided > 0 ? mealsProvided : (long)(foodKg * MEALS_PER_KG_FOOD)));
        pm.setUpdatedAt(LocalDateTime.now());
        platformMetricRepository.save(pm);
    }

    private RestaurantAnalyticsResponse.SummaryStats buildSummaryStats(Object[] agg) {
        if (agg == null || agg.length == 0 || agg[0] == null) {
            return RestaurantAnalyticsResponse.SummaryStats.builder()
                    .totalListings(0)
                    .totalClaimed(0)
                    .totalPickedUp(0)
                    .totalExpired(0)
                    .totalFoodSavedKg(0)
                    .totalCo2SavedKg(0)
                    .totalMealsProvided(0)
                    .totalMonetaryValueSaved(BigDecimal.ZERO)
                    .claimRate(0)
                    .pickupRate(0)
                    .build();
        }

        int totalListings = ((Number) agg[0]).intValue();
        int totalClaimed = ((Number) agg[1]).intValue();
        int totalPickedUp = ((Number) agg[2]).intValue();
        int totalExpired = ((Number) agg[3]).intValue();
        double totalFoodSaved = ((Number) agg[4]).doubleValue();
        double totalCo2Saved = ((Number) agg[5]).doubleValue();
        int totalMeals = ((Number) agg[6]).intValue();
        BigDecimal totalMoney = agg[7] instanceof BigDecimal
                ? (BigDecimal) agg[7]
                : BigDecimal.valueOf(((Number) agg[7]).doubleValue());

        double claimRate = totalListings > 0
                ? (double) totalClaimed / totalListings * 100 : 0;
        double pickupRate = totalClaimed > 0
                ? (double) totalPickedUp / totalClaimed * 100 : 0;

        return RestaurantAnalyticsResponse.SummaryStats.builder()
                .totalListings(totalListings)
                .totalClaimed(totalClaimed)
                .totalPickedUp(totalPickedUp)
                .totalExpired(totalExpired)
                .totalFoodSavedKg(round(totalFoodSaved))
                .totalCo2SavedKg(round(totalCo2Saved))
                .totalMealsProvided(totalMeals)
                .totalMonetaryValueSaved(totalMoney.setScale(2, RoundingMode.HALF_UP))
                .claimRate(round(claimRate))
                .pickupRate(round(pickupRate))
                .build();
    }

    private RestaurantAnalyticsResponse.WasteAnalysis buildWasteAnalysis(
            RestaurantAnalytics analytics) {
        List<String> categories = Collections.emptyList();
        Map<String, Double> peakHours = Collections.emptyMap();

        try {
            if (analytics.getTopCategories() != null) {
                categories = objectMapper.readValue(
                        analytics.getTopCategories(),
                        new TypeReference<List<String>>() {});
            }
            if (analytics.getPeakWasteHours() != null) {
                peakHours = objectMapper.readValue(
                        analytics.getPeakWasteHours(),
                        new TypeReference<Map<String, Double>>() {});
            }
        } catch (Exception e) {
            log.warn("Failed to parse JSON fields in restaurant analytics: {}",
                    e.getMessage());
        }

        return RestaurantAnalyticsResponse.WasteAnalysis.builder()
                .wasteGenerated(analytics.getWasteGenerated())
                .wasteRedirected(analytics.getWasteRedirected())
                .wasteReductionPercent(round(analytics.getWasteReductionPercent()))
                .topCategories(categories)
                .peakWasteHours(peakHours)
                .periodStart(analytics.getPeriodStart())
                .periodEnd(analytics.getPeriodEnd())
                .build();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
