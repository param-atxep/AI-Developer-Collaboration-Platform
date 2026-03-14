package com.foodrescue.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "waste_predictions", indexes = {
        @Index(name = "idx_wp_restaurant_id", columnList = "restaurantId"),
        @Index(name = "idx_wp_predicted_date", columnList = "predictedDate"),
        @Index(name = "idx_wp_restaurant_date", columnList = "restaurantId, predictedDate")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WastePrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID restaurantId;

    @Column(nullable = false)
    private LocalDate predictedDate;

    @Column(nullable = false)
    private double predictedWasteKg;

    @Column(nullable = true)
    private Double actualWasteKg;

    @Column(nullable = false)
    private double confidence;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String factors;

    @Column(length = 50)
    private String modelVersion;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
