package com.foodrescue.analytics.repository;

import com.foodrescue.analytics.entity.RestaurantAnalytics;
import com.foodrescue.analytics.entity.RestaurantAnalytics.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantAnalyticsRepository extends JpaRepository<RestaurantAnalytics, UUID> {

    List<RestaurantAnalytics> findByRestaurantIdOrderByPeriodStartDesc(UUID restaurantId);

    List<RestaurantAnalytics> findByRestaurantIdAndPeriodOrderByPeriodStartDesc(
            UUID restaurantId, Period period);

    List<RestaurantAnalytics> findByRestaurantIdAndPeriodAndPeriodStartBetweenOrderByPeriodStartAsc(
            UUID restaurantId, Period period, LocalDate startDate, LocalDate endDate);

    Optional<RestaurantAnalytics> findByRestaurantIdAndPeriodAndPeriodStart(
            UUID restaurantId, Period period, LocalDate periodStart);

    // Find the latest analytics record for a restaurant and period
    Optional<RestaurantAnalytics> findTopByRestaurantIdAndPeriodOrderByPeriodStartDesc(
            UUID restaurantId, Period period);

    // Trend data: waste reduction over time for a specific restaurant
    @Query("SELECT ra.periodStart, ra.wasteReductionPercent " +
           "FROM RestaurantAnalytics ra " +
           "WHERE ra.restaurantId = :restaurantId " +
           "AND ra.period = :period " +
           "AND ra.periodStart BETWEEN :startDate AND :endDate " +
           "ORDER BY ra.periodStart ASC")
    List<Object[]> wasteReductionTrend(
            @Param("restaurantId") UUID restaurantId,
            @Param("period") Period period,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Aggregate: average waste reduction across all restaurants for a period type
    @Query("SELECT AVG(ra.wasteReductionPercent) " +
           "FROM RestaurantAnalytics ra " +
           "WHERE ra.period = :period " +
           "AND ra.periodStart BETWEEN :startDate AND :endDate")
    Double averageWasteReduction(
            @Param("period") Period period,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Find all analytics records for a given period type and date range
    List<RestaurantAnalytics> findByPeriodAndPeriodStartBetweenOrderByPeriodStartAsc(
            Period period, LocalDate startDate, LocalDate endDate);

    // Trend data aggregated across all restaurants for a period
    @Query("SELECT ra.periodStart, AVG(ra.wasteReductionPercent), SUM(ra.wasteRedirected) " +
           "FROM RestaurantAnalytics ra " +
           "WHERE ra.period = :period " +
           "AND ra.periodStart BETWEEN :startDate AND :endDate " +
           "GROUP BY ra.periodStart " +
           "ORDER BY ra.periodStart ASC")
    List<Object[]> platformWasteReductionTrend(
            @Param("period") Period period,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
