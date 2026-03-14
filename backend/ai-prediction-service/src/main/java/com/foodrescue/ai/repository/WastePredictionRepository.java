package com.foodrescue.ai.repository;

import com.foodrescue.ai.entity.WastePrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WastePredictionRepository extends JpaRepository<WastePrediction, UUID> {

    List<WastePrediction> findByRestaurantIdOrderByPredictedDateDesc(UUID restaurantId);

    List<WastePrediction> findByRestaurantIdAndPredictedDateBetween(
            UUID restaurantId, LocalDate startDate, LocalDate endDate);

    Optional<WastePrediction> findByRestaurantIdAndPredictedDateAndCategory(
            UUID restaurantId, LocalDate predictedDate, String category);

    @Query("SELECT wp FROM WastePrediction wp WHERE wp.restaurantId = :restaurantId " +
            "AND wp.predictedDate = :date ORDER BY wp.createdAt DESC")
    List<WastePrediction> findLatestPredictions(
            @Param("restaurantId") UUID restaurantId,
            @Param("date") LocalDate date);

    @Query("SELECT wp FROM WastePrediction wp WHERE wp.actualWasteKg IS NOT NULL " +
            "AND wp.restaurantId = :restaurantId ORDER BY wp.predictedDate DESC")
    List<WastePrediction> findVerifiedPredictions(@Param("restaurantId") UUID restaurantId);

    @Query("SELECT AVG(ABS(wp.predictedWasteKg - wp.actualWasteKg) / wp.actualWasteKg) " +
            "FROM WastePrediction wp WHERE wp.actualWasteKg IS NOT NULL " +
            "AND wp.actualWasteKg > 0 AND wp.restaurantId = :restaurantId")
    Optional<Double> calculateMeanAbsolutePercentageError(@Param("restaurantId") UUID restaurantId);

    void deleteByRestaurantIdAndPredictedDateBefore(UUID restaurantId, LocalDate date);
}
