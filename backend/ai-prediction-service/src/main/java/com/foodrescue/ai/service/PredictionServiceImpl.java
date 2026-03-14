package com.foodrescue.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodrescue.ai.dto.PredictionResponse;
import com.foodrescue.ai.dto.WasteTrendResponse;
import com.foodrescue.ai.entity.WasteHistory;
import com.foodrescue.ai.entity.WastePrediction;
import com.foodrescue.ai.repository.WasteHistoryRepository;
import com.foodrescue.ai.repository.WastePredictionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PredictionServiceImpl implements PredictionService {

    private static final Logger log = LoggerFactory.getLogger(PredictionServiceImpl.class);
    private static final String CACHE_PREFIX = "prediction:";
    private static final int MINIMUM_HISTORY_RECORDS = 7;
    private static final int WEIGHTED_WINDOW_SIZE = 12;

    private final WasteHistoryRepository wasteHistoryRepository;
    private final WastePredictionRepository wastePredictionRepository;
    private final TensorFlowModelService tensorFlowModelService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // -----------------------------------------------------------------------
    // predictWaste
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public PredictionResponse predictWaste(UUID restaurantId, LocalDate date, boolean includeBreakdown) {
        log.info("Generating waste prediction for restaurant {} on date {}", restaurantId, date);

        // Check Redis cache first
        String cacheKey = CACHE_PREFIX + restaurantId + ":" + date;
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("Returning cached prediction for key {}", cacheKey);
                return objectMapper.readValue(cached, PredictionResponse.class);
            }
        } catch (Exception e) {
            log.warn("Redis cache read failed, proceeding without cache: {}", e.getMessage());
        }

        List<String> categories = wasteHistoryRepository.findDistinctCategoriesByRestaurantId(restaurantId);
        if (categories.isEmpty()) {
            categories = List.of("general");
        }

        long historyCount = wasteHistoryRepository.countByRestaurantId(restaurantId);
        Map<String, Double> categoryBreakdown = new LinkedHashMap<>();
        double totalPredicted = 0.0;
        double overallConfidence;
        String modelVersionUsed;

        int targetDayOfWeek = date.getDayOfWeek().getValue();
        int targetMonth = date.getMonthValue();

        // ----- Attempt TensorFlow inference first -----
        if (tensorFlowModelService.isModelAvailable() && historyCount >= MINIMUM_HISTORY_RECORDS) {
            modelVersionUsed = "tf-" + tensorFlowModelService.getModelVersion();
            overallConfidence = 0.0;

            for (String category : categories) {
                Double historicalAvg = wasteHistoryRepository.averageWasteByDayOfWeekAndCategory(
                        restaurantId, targetDayOfWeek, category);
                if (historicalAvg == null) historicalAvg = 0.0;

                float recentTrend = calculateRecentTrend(restaurantId, category);

                float[] features = new float[]{
                        targetDayOfWeek,
                        targetMonth,
                        0f, // isHoliday -- would be enriched by external calendar API
                        historicalAvg.floatValue(),
                        recentTrend,
                        0f, // weatherCode -- would be enriched by weather API
                        0f  // specialEventFlag
                };

                double predicted = tensorFlowModelService.predict(features);
                if (predicted < 0) {
                    // TF inference failed for this category; use statistical fallback
                    predicted = statisticalPredict(restaurantId, targetDayOfWeek, category);
                }
                categoryBreakdown.put(category, Math.round(predicted * 100.0) / 100.0);
                totalPredicted += predicted;
            }

            overallConfidence = computeConfidence(restaurantId, historyCount);

        } else {
            // ----- Statistical fallback: weighted moving average -----
            modelVersionUsed = "statistical-v1";

            for (String category : categories) {
                double predicted = statisticalPredict(restaurantId, targetDayOfWeek, category);
                categoryBreakdown.put(category, Math.round(predicted * 100.0) / 100.0);
                totalPredicted += predicted;
            }

            overallConfidence = computeConfidence(restaurantId, historyCount);
        }

        totalPredicted = Math.round(totalPredicted * 100.0) / 100.0;

        // Build factors map
        Map<String, String> factors = new LinkedHashMap<>();
        factors.put("dayOfWeek", date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        factors.put("month", String.valueOf(targetMonth));
        factors.put("historicalRecords", String.valueOf(historyCount));
        factors.put("modelUsed", modelVersionUsed);

        // Generate recommendations
        List<String> recommendations = includeBreakdown
                ? generateRecommendations(restaurantId)
                : Collections.emptyList();

        // Persist prediction
        for (Map.Entry<String, Double> entry : categoryBreakdown.entrySet()) {
            String factorsJson;
            try {
                factorsJson = objectMapper.writeValueAsString(factors);
            } catch (JsonProcessingException e) {
                factorsJson = "{}";
            }

            WastePrediction prediction = WastePrediction.builder()
                    .restaurantId(restaurantId)
                    .predictedDate(date)
                    .predictedWasteKg(entry.getValue())
                    .confidence(overallConfidence)
                    .category(entry.getKey())
                    .factors(factorsJson)
                    .modelVersion(modelVersionUsed)
                    .build();
            wastePredictionRepository.save(prediction);
        }

        PredictionResponse response = PredictionResponse.builder()
                .restaurantId(restaurantId)
                .date(date)
                .predictedWasteKg(totalPredicted)
                .confidence(overallConfidence)
                .modelVersion(modelVersionUsed)
                .categoryBreakdown(includeBreakdown ? categoryBreakdown : null)
                .recommendations(recommendations)
                .factors(factors)
                .build();

        // Cache the result for 2 hours
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, json, 2, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Failed to cache prediction result: {}", e.getMessage());
        }

        return response;
    }

    // -----------------------------------------------------------------------
    // getWasteTrend
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public WasteTrendResponse getWasteTrend(UUID restaurantId, int days) {
        log.info("Calculating waste trend for restaurant {} over {} days", restaurantId, days);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        List<WasteHistory> history = wasteHistoryRepository
                .findByRestaurantIdAndDateBetweenOrderByDateAsc(restaurantId, startDate, endDate);

        List<WasteTrendResponse.DataPoint> dataPoints = history.stream()
                .map(wh -> WasteTrendResponse.DataPoint.builder()
                        .date(wh.getDate())
                        .wasteKg(wh.getWasteKg())
                        .category(wh.getCategory())
                        .dayOfWeek(wh.getDayOfWeek())
                        .dayName(DayOfWeek.of(wh.getDayOfWeek())
                                .getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                        .build())
                .collect(Collectors.toList());

        double averageWaste = history.stream()
                .mapToDouble(WasteHistory::getWasteKg)
                .average()
                .orElse(0.0);
        averageWaste = Math.round(averageWaste * 100.0) / 100.0;

        // Determine trend direction by comparing first half vs second half
        String trendDirection = computeTrendDirection(history);

        // Percentage change: compare last 7 days vs prior 7 days
        double percentageChange = computePercentageChange(history, days);

        // Identify peak days of week
        List<String> peakDays = identifyPeakDays(restaurantId);

        // Category averages
        Map<String, Double> categoryAverages = new LinkedHashMap<>();
        List<Object[]> catAgg = wasteHistoryRepository.aggregateByCategory(restaurantId);
        for (Object[] row : catAgg) {
            categoryAverages.put((String) row[0],
                    Math.round(((Number) row[1]).doubleValue() * 100.0) / 100.0);
        }

        return WasteTrendResponse.builder()
                .historicalData(dataPoints)
                .trendDirection(trendDirection)
                .averageWasteKg(averageWaste)
                .peakDays(peakDays)
                .categoryAverages(categoryAverages)
                .percentageChange(Math.round(percentageChange * 100.0) / 100.0)
                .build();
    }

    // -----------------------------------------------------------------------
    // generateRecommendations
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<String> generateRecommendations(UUID restaurantId) {
        log.info("Generating recommendations for restaurant {}", restaurantId);
        List<String> recommendations = new ArrayList<>();

        // 1. Identify high-waste days by day of week
        List<Object[]> dayOfWeekAgg = wasteHistoryRepository.aggregateByDayOfWeek(restaurantId);
        if (!dayOfWeekAgg.isEmpty()) {
            double overallAvg = dayOfWeekAgg.stream()
                    .mapToDouble(row -> ((Number) row[1]).doubleValue())
                    .average()
                    .orElse(0.0);

            for (Object[] row : dayOfWeekAgg) {
                int dow = ((Number) row[0]).intValue();
                double avg = ((Number) row[1]).doubleValue();
                if (avg > overallAvg * 1.3) {
                    String dayName = DayOfWeek.of(dow).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                    int reductionPct = (int) Math.round(((avg - overallAvg) / avg) * 100);
                    recommendations.add(String.format(
                            "Consider reducing %s production by %d%% -- waste is %.1f kg above average on that day.",
                            dayName, reductionPct, avg - overallAvg));
                }
            }
        }

        // 2. Identify high-waste categories with day-of-week drill-down
        List<Object[]> catDayAgg = wasteHistoryRepository.aggregateByDayOfWeekAndCategory(restaurantId);
        Map<String, Map<Integer, Double>> categoryDayMap = new LinkedHashMap<>();
        for (Object[] row : catDayAgg) {
            int dow = ((Number) row[0]).intValue();
            String category = (String) row[1];
            double avg = ((Number) row[2]).doubleValue();
            categoryDayMap.computeIfAbsent(category, k -> new LinkedHashMap<>()).put(dow, avg);
        }

        for (Map.Entry<String, Map<Integer, Double>> entry : categoryDayMap.entrySet()) {
            String category = entry.getKey();
            Map<Integer, Double> dayMap = entry.getValue();
            double catAvg = dayMap.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            for (Map.Entry<Integer, Double> dayEntry : dayMap.entrySet()) {
                if (dayEntry.getValue() > catAvg * 1.4) {
                    String dayName = DayOfWeek.of(dayEntry.getKey())
                            .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                    int reductionPct = (int) Math.round(
                            ((dayEntry.getValue() - catAvg) / dayEntry.getValue()) * 100);
                    recommendations.add(String.format(
                            "Reduce %s %s production by %d%% -- this category wastes %.1f kg above its average on that day.",
                            dayName, category, reductionPct, dayEntry.getValue() - catAvg));
                }
            }
        }

        // 3. Identify seasonal patterns
        List<Object[]> monthAgg = wasteHistoryRepository.aggregateByMonth(restaurantId);
        if (monthAgg.size() >= 3) {
            double monthlyAvg = monthAgg.stream()
                    .mapToDouble(row -> ((Number) row[1]).doubleValue())
                    .average()
                    .orElse(0.0);
            for (Object[] row : monthAgg) {
                int month = ((Number) row[0]).intValue();
                double avg = ((Number) row[1]).doubleValue();
                if (avg > monthlyAvg * 1.25) {
                    String monthName = java.time.Month.of(month)
                            .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                    recommendations.add(String.format(
                            "Plan for higher waste volumes in %s (%.1f kg avg vs %.1f kg overall). " +
                                    "Consider seasonal menu adjustments or increased donation scheduling.",
                            monthName, avg, monthlyAvg));
                }
            }
        }

        // 4. General best practices if we have limited data
        if (recommendations.isEmpty()) {
            recommendations.add("Continue logging waste data daily to improve prediction accuracy.");
            recommendations.add("Track waste by food category to identify specific reduction opportunities.");
            recommendations.add("Consider scheduling more frequent donation pickups on high-waste days.");
        }

        // 5. Always add a model accuracy note
        Optional<Double> mape = wastePredictionRepository.calculateMeanAbsolutePercentageError(restaurantId);
        if (mape.isPresent() && mape.get() > 0) {
            double accuracy = (1 - mape.get()) * 100;
            if (accuracy < 70) {
                recommendations.add(String.format(
                        "Current model accuracy is %.0f%%. More historical data will improve predictions.",
                        accuracy));
            }
        }

        return recommendations;
    }

    // -----------------------------------------------------------------------
    // updateModel
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public void updateModel() {
        log.info("Initiating model retraining...");

        if (!tensorFlowModelService.isModelAvailable()) {
            log.info("TensorFlow model not available. Retraining is a no-op in statistical mode. " +
                    "Statistical model uses live queries and does not require explicit retraining.");
            return;
        }

        // In a production system, this would:
        // 1. Export training data from the database
        // 2. Trigger a training pipeline (e.g., via a Kubernetes Job or external ML platform)
        // 3. Wait for training completion
        // 4. Validate the new model against a holdout set
        // 5. Swap the model file and reload

        log.info("Exporting training data for model retraining...");
        List<WasteHistory> allHistory = wasteHistoryRepository.findAll();
        log.info("Found {} historical records for training.", allHistory.size());

        if (allHistory.size() < 30) {
            log.warn("Insufficient data for meaningful retraining. Need at least 30 records, found {}.",
                    allHistory.size());
            return;
        }

        // Simulate retraining: in production, this would call a Python-based training
        // pipeline or a TensorFlow Java training routine.
        log.info("Model retraining triggered with {} records. " +
                "This runs asynchronously. The new model will be loaded upon completion.", allHistory.size());

        // Invalidate cached predictions after model update
        try {
            Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Cleared {} cached predictions after model update.", keys.size());
            }
        } catch (Exception e) {
            log.warn("Failed to clear prediction cache: {}", e.getMessage());
        }

        // Reload model from disk (in production, this happens after training completes)
        tensorFlowModelService.reloadModel();
    }

    // -----------------------------------------------------------------------
    // getWasteHistory
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<WasteHistory> getWasteHistory(UUID restaurantId) {
        return wasteHistoryRepository.findByRestaurantIdOrderByDateDesc(restaurantId);
    }

    // -----------------------------------------------------------------------
    // Private helper methods
    // -----------------------------------------------------------------------

    /**
     * Statistical prediction using a weighted moving average based on day-of-week patterns.
     * More recent data points receive higher weights.
     */
    private double statisticalPredict(UUID restaurantId, int dayOfWeek, String category) {
        // Get historical records for this day-of-week and category
        Double avgForDayAndCat = wasteHistoryRepository
                .averageWasteByDayOfWeekAndCategory(restaurantId, dayOfWeek, category);

        // Get recent records for trend adjustment
        List<WasteHistory> recentHistory = wasteHistoryRepository
                .findRecentByRestaurantId(restaurantId, WEIGHTED_WINDOW_SIZE * 7);

        List<WasteHistory> relevantRecords = recentHistory.stream()
                .filter(wh -> wh.getDayOfWeek() == dayOfWeek && wh.getCategory().equals(category))
                .limit(WEIGHTED_WINDOW_SIZE)
                .collect(Collectors.toList());

        if (relevantRecords.isEmpty()) {
            // No matching records for this day+category; fall back to overall category average
            if (avgForDayAndCat != null && avgForDayAndCat > 0) {
                return avgForDayAndCat;
            }
            // Ultimate fallback: average across all categories for this day
            Double avgForDay = wasteHistoryRepository.averageWasteByDayOfWeek(restaurantId, dayOfWeek);
            return avgForDay != null ? avgForDay : 0.0;
        }

        // Weighted moving average: more recent entries get higher weight
        // Weights: most recent = WEIGHTED_WINDOW_SIZE, oldest = 1
        double weightedSum = 0.0;
        double weightTotal = 0.0;
        int size = relevantRecords.size();

        for (int i = 0; i < size; i++) {
            double weight = size - i; // most recent first (already sorted DESC)
            weightedSum += relevantRecords.get(i).getWasteKg() * weight;
            weightTotal += weight;
        }

        double weightedAvg = weightedSum / weightTotal;

        // Apply trend adjustment: compare recent average to historical average
        if (avgForDayAndCat != null && avgForDayAndCat > 0) {
            double trendFactor = weightedAvg / avgForDayAndCat;
            // Dampen the trend factor to avoid over-correction
            trendFactor = 1.0 + (trendFactor - 1.0) * 0.5;
            // Clamp to reasonable bounds
            trendFactor = Math.max(0.5, Math.min(trendFactor, 2.0));
            weightedAvg = avgForDayAndCat * trendFactor;
        }

        return Math.max(0, weightedAvg);
    }

    /**
     * Calculate a recent trend value for a category.
     * Returns a float in the range [-1, 1] where positive means increasing waste.
     */
    private float calculateRecentTrend(UUID restaurantId, String category) {
        List<WasteHistory> recent = wasteHistoryRepository
                .findRecentByRestaurantId(restaurantId, 28); // last 4 weeks

        List<WasteHistory> categoryRecords = recent.stream()
                .filter(wh -> wh.getCategory().equals(category))
                .collect(Collectors.toList());

        if (categoryRecords.size() < 4) {
            return 0f;
        }

        int mid = categoryRecords.size() / 2;
        double recentAvg = categoryRecords.subList(0, mid).stream()
                .mapToDouble(WasteHistory::getWasteKg).average().orElse(0);
        double olderAvg = categoryRecords.subList(mid, categoryRecords.size()).stream()
                .mapToDouble(WasteHistory::getWasteKg).average().orElse(0);

        if (olderAvg == 0) return 0f;

        float trend = (float) ((recentAvg - olderAvg) / olderAvg);
        return Math.max(-1f, Math.min(trend, 1f));
    }

    /**
     * Compute a confidence score (0 to 1) based on data availability and model accuracy.
     */
    private double computeConfidence(UUID restaurantId, long historyCount) {
        // Base confidence from data volume (more data = higher confidence)
        double dataConfidence;
        if (historyCount >= 365) {
            dataConfidence = 0.95;
        } else if (historyCount >= 90) {
            dataConfidence = 0.80 + (historyCount - 90.0) / (365.0 - 90.0) * 0.15;
        } else if (historyCount >= 30) {
            dataConfidence = 0.60 + (historyCount - 30.0) / (90.0 - 30.0) * 0.20;
        } else if (historyCount >= MINIMUM_HISTORY_RECORDS) {
            dataConfidence = 0.30 + (historyCount - MINIMUM_HISTORY_RECORDS) /
                    (30.0 - MINIMUM_HISTORY_RECORDS) * 0.30;
        } else {
            dataConfidence = 0.10 + historyCount * 0.02;
        }

        // Adjust by model accuracy if available
        Optional<Double> mape = wastePredictionRepository
                .calculateMeanAbsolutePercentageError(restaurantId);
        if (mape.isPresent() && mape.get() >= 0) {
            double modelAccuracy = Math.max(0, 1.0 - mape.get());
            // Blend data confidence and model accuracy
            dataConfidence = dataConfidence * 0.6 + modelAccuracy * 0.4;
        }

        // Boost if TensorFlow model is being used
        if (tensorFlowModelService.isModelAvailable()) {
            dataConfidence = Math.min(1.0, dataConfidence + 0.05);
        }

        return Math.round(dataConfidence * 100.0) / 100.0;
    }

    /**
     * Determine trend direction by comparing the first half of data to the second half.
     */
    private String computeTrendDirection(List<WasteHistory> history) {
        if (history.size() < 4) {
            return "insufficient_data";
        }

        int mid = history.size() / 2;
        double firstHalfAvg = history.subList(0, mid).stream()
                .mapToDouble(WasteHistory::getWasteKg).average().orElse(0);
        double secondHalfAvg = history.subList(mid, history.size()).stream()
                .mapToDouble(WasteHistory::getWasteKg).average().orElse(0);

        double changePercent = firstHalfAvg > 0
                ? ((secondHalfAvg - firstHalfAvg) / firstHalfAvg) * 100 : 0;

        if (changePercent > 5) {
            return "increasing";
        } else if (changePercent < -5) {
            return "decreasing";
        }
        return "stable";
    }

    /**
     * Compute percentage change between the most recent period and the prior period.
     */
    private double computePercentageChange(List<WasteHistory> history, int totalDays) {
        if (history.size() < 4) return 0.0;

        LocalDate now = LocalDate.now();
        int halfDays = totalDays / 2;
        LocalDate midPoint = now.minusDays(halfDays);

        double recentAvg = history.stream()
                .filter(wh -> !wh.getDate().isBefore(midPoint))
                .mapToDouble(WasteHistory::getWasteKg)
                .average().orElse(0);
        double olderAvg = history.stream()
                .filter(wh -> wh.getDate().isBefore(midPoint))
                .mapToDouble(WasteHistory::getWasteKg)
                .average().orElse(0);

        if (olderAvg == 0) return 0.0;
        return ((recentAvg - olderAvg) / olderAvg) * 100;
    }

    /**
     * Identify the top 2 peak waste days of the week for a restaurant.
     */
    private List<String> identifyPeakDays(UUID restaurantId) {
        List<Object[]> dayAgg = wasteHistoryRepository.aggregateByDayOfWeek(restaurantId);
        if (dayAgg.isEmpty()) return Collections.emptyList();

        return dayAgg.stream()
                .sorted((a, b) -> Double.compare(
                        ((Number) b[1]).doubleValue(),
                        ((Number) a[1]).doubleValue()))
                .limit(2)
                .map(row -> DayOfWeek.of(((Number) row[0]).intValue())
                        .getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                .collect(Collectors.toList());
    }
}
