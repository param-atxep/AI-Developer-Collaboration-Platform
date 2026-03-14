package com.foodrescue.analytics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    name = "platform_metrics",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"date"})
    },
    indexes = {
        @Index(name = "idx_pm_date", columnList = "date")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "date", nullable = false, unique = true)
    private LocalDate date;

    @Column(name = "total_users", nullable = false)
    @Builder.Default
    private long totalUsers = 0;

    @Column(name = "active_users", nullable = false)
    @Builder.Default
    private long activeUsers = 0;

    @Column(name = "total_restaurants", nullable = false)
    @Builder.Default
    private long totalRestaurants = 0;

    @Column(name = "total_ngos", nullable = false)
    @Builder.Default
    private long totalNgos = 0;

    @Column(name = "total_listings", nullable = false)
    @Builder.Default
    private long totalListings = 0;

    @Column(name = "total_pickups", nullable = false)
    @Builder.Default
    private long totalPickups = 0;

    @Column(name = "total_food_saved_kg", nullable = false)
    @Builder.Default
    private double totalFoodSavedKg = 0.0;

    @Column(name = "total_meals_provided", nullable = false)
    @Builder.Default
    private long totalMealsProvided = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
