package com.foodrescue.ai.controller;

import com.foodrescue.ai.dto.PredictionRequest;
import com.foodrescue.ai.dto.PredictionResponse;
import com.foodrescue.ai.dto.WasteTrendResponse;
import com.foodrescue.ai.entity.WasteHistory;
import com.foodrescue.ai.service.PredictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private static final Logger log = LoggerFactory.getLogger(PredictionController.class);

    private final PredictionService predictionService;

    /**
     * Generate a waste prediction for a specific restaurant and date.
     *
     * POST /api/predictions/predict
     * Body: { "restaurantId": "uuid", "targetDate": "2025-12-25", "includeBreakdown": true }
     */
    @PostMapping("/predict")
    public ResponseEntity<PredictionResponse> predict(@Valid @RequestBody PredictionRequest request) {
        log.info("Prediction request received for restaurant {} on date {}",
                request.getRestaurantId(), request.getTargetDate());

        PredictionResponse response = predictionService.predictWaste(
                request.getRestaurantId(),
                request.getTargetDate(),
                request.isIncludeBreakdown());

        return ResponseEntity.ok(response);
    }

    /**
     * Get waste trend analysis for a restaurant over a specified number of days.
     *
     * GET /api/predictions/restaurant/{id}/trend?days=30
     */
    @GetMapping("/restaurant/{id}/trend")
    public ResponseEntity<WasteTrendResponse> getTrend(
            @PathVariable("id") UUID restaurantId,
            @RequestParam(value = "days", defaultValue = "30") int days) {
        log.info("Trend request for restaurant {} over {} days", restaurantId, days);

        if (days < 1 || days > 365) {
            return ResponseEntity.badRequest().build();
        }

        WasteTrendResponse response = predictionService.getWasteTrend(restaurantId, days);
        return ResponseEntity.ok(response);
    }

    /**
     * Get AI-generated recommendations for waste reduction.
     *
     * GET /api/predictions/restaurant/{id}/recommendations
     */
    @GetMapping("/restaurant/{id}/recommendations")
    public ResponseEntity<Map<String, Object>> getRecommendations(
            @PathVariable("id") UUID restaurantId) {
        log.info("Recommendations request for restaurant {}", restaurantId);

        List<String> recommendations = predictionService.generateRecommendations(restaurantId);
        return ResponseEntity.ok(Map.of(
                "restaurantId", restaurantId,
                "recommendations", recommendations,
                "generatedAt", java.time.LocalDateTime.now().toString()
        ));
    }

    /**
     * Get waste history records for a restaurant.
     *
     * GET /api/predictions/restaurant/{id}/history
     */
    @GetMapping("/restaurant/{id}/history")
    public ResponseEntity<Map<String, Object>> getHistory(
            @PathVariable("id") UUID restaurantId) {
        log.info("History request for restaurant {}", restaurantId);

        List<WasteHistory> history = predictionService.getWasteHistory(restaurantId);
        return ResponseEntity.ok(Map.of(
                "restaurantId", restaurantId,
                "records", history,
                "totalRecords", history.size()
        ));
    }

    /**
     * Trigger model retraining. Restricted to admin users.
     *
     * POST /api/predictions/model/retrain
     */
    @PostMapping("/model/retrain")
    public ResponseEntity<Map<String, String>> retrainModel() {
        log.info("Model retrain request received");

        try {
            predictionService.updateModel();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Model retraining initiated successfully.",
                    "timestamp", java.time.LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            log.error("Model retraining failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Model retraining failed: " + e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now().toString()
            ));
        }
    }
}
