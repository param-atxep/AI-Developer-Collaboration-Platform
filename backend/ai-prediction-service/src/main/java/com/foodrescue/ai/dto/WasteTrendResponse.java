package com.foodrescue.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WasteTrendResponse {

    private List<DataPoint> historicalData;
    private String trendDirection;
    private double averageWasteKg;
    private List<String> peakDays;
    private Map<String, Double> categoryAverages;
    private double percentageChange;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {
        private LocalDate date;
        private double wasteKg;
        private String category;
        private int dayOfWeek;
        private String dayName;
    }
}
