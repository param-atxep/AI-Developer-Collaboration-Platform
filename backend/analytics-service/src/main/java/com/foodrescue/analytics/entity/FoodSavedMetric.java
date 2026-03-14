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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "food_saved_metrics",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"restaurant_id", "date"})
    },
    indexes = {
        @Index(name = "idx_fsm_restaurant_id", columnList = "restaurant_id"),
        @Index(name = "idx_fsm_date", columnList = "date"),
        @Index(name = "idx_fsm_restaurant_date", columnList = "restaurant_id, date")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodSavedMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "total_listings", nullable = false)
    @Builder.Default
    private int totalListings = 0;

    @Column(name = "total_claimed", nullable = false)
    @Builder.Default
    private int totalClaimed = 0;

    @Column(name = "total_picked_up", nullable = false)
    @Builder.Default
    private int totalPickedUp = 0;

    @Column(name = "total_expired", nullable = false)
    @Builder.Default
    private int totalExpired = 0;

    @Column(name = "food_saved_kg", nullable = false)
    @Builder.Default
    private double foodSavedKg = 0.0;

    @Column(name = "co2_saved_kg", nullable = false)
    @Builder.Default
    private double co2SavedKg = 0.0;

    @Column(name = "meals_provided", nullable = false)
    @Builder.Default
    private int mealsProvided = 0;

    @Column(name = "monetary_value_saved", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal monetaryValueSaved = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
