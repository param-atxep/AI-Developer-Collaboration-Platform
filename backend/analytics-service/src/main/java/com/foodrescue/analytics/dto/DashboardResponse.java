package com.foodrescue.analytics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardResponse implements Serializable {

    private double totalFoodSaved;
    private long totalMeals;
    private double totalCo2Saved;
    private BigDecimal totalMoneyValue;
    private long activeRestaurants;
    private long activeNgos;
    private List<RecentActivityItem> recentActivity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivityItem implements Serializable {
        private String type;
        private String description;
        private String restaurantName;
        private String ngoName;
        private double foodKg;
        private LocalDateTime timestamp;
    }
}
