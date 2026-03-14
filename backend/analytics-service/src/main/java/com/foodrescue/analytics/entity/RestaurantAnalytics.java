package com.foodrescue.analytics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "restaurant_analytics",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"restaurant_id", "period", "period_start"})
    },
    indexes = {
        @Index(name = "idx_ra_restaurant_id", columnList = "restaurant_id"),
        @Index(name = "idx_ra_period", columnList = "period"),
        @Index(name = "idx_ra_restaurant_period", columnList = "restaurant_id, period, period_start")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantAnalytics {

    public enum Period {
        DAILY,
        WEEKLY,
        MONTHLY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "period", nullable = false, length = 10)
    private Period period;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "waste_generated", nullable = false)
    @Builder.Default
    private double wasteGenerated = 0.0;

    @Column(name = "waste_redirected", nullable = false)
    @Builder.Default
    private double wasteRedirected = 0.0;

    @Column(name = "waste_reduction_percent", nullable = false)
    @Builder.Default
    private double wasteReductionPercent = 0.0;

    @Column(name = "top_categories", columnDefinition = "TEXT")
    private String topCategories;

    @Column(name = "peak_waste_hours", columnDefinition = "TEXT")
    private String peakWasteHours;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
