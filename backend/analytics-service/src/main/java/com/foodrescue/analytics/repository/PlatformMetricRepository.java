package com.foodrescue.analytics.repository;

import com.foodrescue.analytics.entity.PlatformMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformMetricRepository extends JpaRepository<PlatformMetric, UUID> {

    Optional<PlatformMetric> findByDate(LocalDate date);

    Optional<PlatformMetric> findTopByOrderByDateDesc();

    List<PlatformMetric> findByDateBetweenOrderByDateAsc(LocalDate startDate, LocalDate endDate);

    @Query("SELECT p FROM PlatformMetric p WHERE p.date = " +
           "(SELECT MAX(p2.date) FROM PlatformMetric p2)")
    Optional<PlatformMetric> findLatest();

    // Time series for total users over time
    @Query("SELECT p.date, p.totalUsers FROM PlatformMetric p " +
           "WHERE p.date BETWEEN :startDate AND :endDate " +
           "ORDER BY p.date ASC")
    List<Object[]> userGrowthTimeSeries(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Time series for total listings over time
    @Query("SELECT p.date, p.totalListings FROM PlatformMetric p " +
           "WHERE p.date BETWEEN :startDate AND :endDate " +
           "ORDER BY p.date ASC")
    List<Object[]> listingsTimeSeries(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Time series for total pickups over time
    @Query("SELECT p.date, p.totalPickups FROM PlatformMetric p " +
           "WHERE p.date BETWEEN :startDate AND :endDate " +
           "ORDER BY p.date ASC")
    List<Object[]> pickupsTimeSeries(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Sum of food saved across all platform metrics
    @Query("SELECT COALESCE(SUM(p.totalFoodSavedKg), 0) FROM PlatformMetric p")
    double sumTotalFoodSavedKg();

    // Sum of meals provided across all platform metrics
    @Query("SELECT COALESCE(SUM(p.totalMealsProvided), 0) FROM PlatformMetric p")
    long sumTotalMealsProvided();
}
