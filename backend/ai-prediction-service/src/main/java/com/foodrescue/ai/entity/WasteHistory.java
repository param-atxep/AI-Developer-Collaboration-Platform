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
@Table(name = "waste_history", indexes = {
        @Index(name = "idx_wh_restaurant_id", columnList = "restaurantId"),
        @Index(name = "idx_wh_date", columnList = "date"),
        @Index(name = "idx_wh_restaurant_date", columnList = "restaurantId, date"),
        @Index(name = "idx_wh_day_of_week", columnList = "dayOfWeek")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WasteHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID restaurantId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private double wasteKg;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false)
    private int dayOfWeek;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private boolean isHoliday;

    @Column(length = 50)
    private String weatherCondition;

    @Column(length = 200)
    private String specialEvent;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
