package com.foodrescue.analytics.repository;

import com.foodrescue.analytics.entity.FoodSavedMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FoodSavedMetricRepository extends JpaRepository<FoodSavedMetric, UUID> {

    Optional<FoodSavedMetric> findByRestaurantIdAndDate(UUID restaurantId, LocalDate date);

    List<FoodSavedMetric> findByRestaurantIdOrderByDateDesc(UUID restaurantId);

    List<FoodSavedMetric> findByRestaurantIdAndDateBetweenOrderByDateAsc(
            UUID restaurantId, LocalDate startDate, LocalDate endDate);

    List<FoodSavedMetric> findByDateBetweenOrderByDateAsc(LocalDate startDate, LocalDate endDate);

    // Aggregate: total food saved across entire platform in a date range
    @Query("SELECT COALESCE(SUM(m.foodSavedKg), 0) FROM FoodSavedMetric m " +
           "WHERE m.date BETWEEN :startDate AND :endDate")
    double sumFoodSavedKgByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Aggregate: total CO2 saved across entire platform in a date range
    @Query("SELECT COALESCE(SUM(m.co2SavedKg), 0) FROM FoodSavedMetric m " +
           "WHERE m.date BETWEEN :startDate AND :endDate")
    double sumCo2SavedKgByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Aggregate: total meals provided across entire platform in a date range
    @Query("SELECT COALESCE(SUM(m.mealsProvided), 0) FROM FoodSavedMetric m " +
           "WHERE m.date BETWEEN :startDate AND :endDate")
    long sumMealsProvidedByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Aggregate: total monetary value saved across entire platform in a date range
    @Query("SELECT COALESCE(SUM(m.monetaryValueSaved), 0) FROM FoodSavedMetric m " +
           "WHERE m.date BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumMonetaryValueSavedByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Aggregate: sum food saved per restaurant in a date range
    @Query("SELECT m.restaurantId, COALESCE(SUM(m.foodSavedKg), 0) " +
           "FROM FoodSavedMetric m " +
           "WHERE m.date BETWEEN :startDate AND :endDate " +
           "GROUP BY m.restaurantId " +
           "ORDER BY SUM(m.foodSavedKg) DESC")
    List<Object[]> sumFoodSavedByRestaurantInDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Aggregate: sum all metrics for a specific restaurant in a date range
    @Query("SELECT COALESCE(SUM(m.totalListings), 0), " +
           "COALESCE(SUM(m.totalClaimed), 0), " +
           "COALESCE(SUM(m.totalPickedUp), 0), " +
           "COALESCE(SUM(m.totalExpired), 0), " +
           "COALESCE(SUM(m.foodSavedKg), 0), " +
           "COALESCE(SUM(m.co2SavedKg), 0), " +
           "COALESCE(SUM(m.mealsProvided), 0), " +
           "COALESCE(SUM(m.monetaryValueSaved), 0) " +
           "FROM FoodSavedMetric m " +
           "WHERE m.restaurantId = :restaurantId " +
           "AND m.date BETWEEN :startDate AND :endDate")
    Object[] aggregateByRestaurantAndDateRange(
            @Param("restaurantId") UUID restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Top N restaurants by food saved in a date range
    @Query("SELECT m.restaurantId, SUM(m.foodSavedKg) as totalSaved, " +
           "SUM(m.mealsProvided) as totalMeals, SUM(m.co2SavedKg) as totalCo2 " +
           "FROM FoodSavedMetric m " +
           "WHERE m.date BETWEEN :startDate AND :endDate " +
           "GROUP BY m.restaurantId " +
           "ORDER BY totalSaved DESC " +
           "LIMIT :limit")
    List<Object[]> findTopRestaurantsByFoodSaved(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("limit") int limit);

    // Daily aggregates across all restaurants for time series
    @Query("SELECT m.date, COALESCE(SUM(m.foodSavedKg), 0) " +
           "FROM FoodSavedMetric m " +
           "WHERE m.date BETWEEN :startDate AND :endDate " +
           "GROUP BY m.date " +
           "ORDER BY m.date ASC")
    List<Object[]> dailyFoodSavedTimeSeries(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Count distinct active restaurants in a date range
    @Query("SELECT COUNT(DISTINCT m.restaurantId) FROM FoodSavedMetric m " +
           "WHERE m.date BETWEEN :startDate AND :endDate " +
           "AND m.totalListings > 0")
    long countActiveRestaurantsInDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // All-time platform totals
    @Query("SELECT COALESCE(SUM(m.foodSavedKg), 0) FROM FoodSavedMetric m")
    double sumTotalFoodSavedKg();

    @Query("SELECT COALESCE(SUM(m.co2SavedKg), 0) FROM FoodSavedMetric m")
    double sumTotalCo2SavedKg();

    @Query("SELECT COALESCE(SUM(m.mealsProvided), 0) FROM FoodSavedMetric m")
    long sumTotalMealsProvided();

    @Query("SELECT COALESCE(SUM(m.monetaryValueSaved), 0) FROM FoodSavedMetric m")
    java.math.BigDecimal sumTotalMonetaryValueSaved();
}
