package com.foodrescue.ai.repository;

import com.foodrescue.ai.entity.WasteHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface WasteHistoryRepository extends JpaRepository<WasteHistory, UUID> {

    List<WasteHistory> findByRestaurantIdOrderByDateDesc(UUID restaurantId);

    List<WasteHistory> findByRestaurantIdAndDateBetweenOrderByDateAsc(
            UUID restaurantId, LocalDate startDate, LocalDate endDate);

    List<WasteHistory> findByDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT wh FROM WasteHistory wh WHERE wh.restaurantId = :restaurantId " +
            "ORDER BY wh.date DESC LIMIT :limit")
    List<WasteHistory> findRecentByRestaurantId(
            @Param("restaurantId") UUID restaurantId,
            @Param("limit") int limit);

    @Query("SELECT wh.dayOfWeek, AVG(wh.wasteKg) FROM WasteHistory wh " +
            "WHERE wh.restaurantId = :restaurantId " +
            "GROUP BY wh.dayOfWeek ORDER BY wh.dayOfWeek")
    List<Object[]> aggregateByDayOfWeek(@Param("restaurantId") UUID restaurantId);

    @Query("SELECT wh.category, AVG(wh.wasteKg) FROM WasteHistory wh " +
            "WHERE wh.restaurantId = :restaurantId " +
            "GROUP BY wh.category ORDER BY AVG(wh.wasteKg) DESC")
    List<Object[]> aggregateByCategory(@Param("restaurantId") UUID restaurantId);

    @Query("SELECT wh.month, AVG(wh.wasteKg) FROM WasteHistory wh " +
            "WHERE wh.restaurantId = :restaurantId " +
            "GROUP BY wh.month ORDER BY wh.month")
    List<Object[]> aggregateByMonth(@Param("restaurantId") UUID restaurantId);

    @Query("SELECT wh.dayOfWeek, wh.category, AVG(wh.wasteKg) FROM WasteHistory wh " +
            "WHERE wh.restaurantId = :restaurantId " +
            "GROUP BY wh.dayOfWeek, wh.category ORDER BY wh.dayOfWeek, wh.category")
    List<Object[]> aggregateByDayOfWeekAndCategory(@Param("restaurantId") UUID restaurantId);

    @Query("SELECT AVG(wh.wasteKg) FROM WasteHistory wh " +
            "WHERE wh.restaurantId = :restaurantId AND wh.dayOfWeek = :dayOfWeek")
    Double averageWasteByDayOfWeek(
            @Param("restaurantId") UUID restaurantId,
            @Param("dayOfWeek") int dayOfWeek);

    @Query("SELECT AVG(wh.wasteKg) FROM WasteHistory wh " +
            "WHERE wh.restaurantId = :restaurantId AND wh.dayOfWeek = :dayOfWeek " +
            "AND wh.category = :category")
    Double averageWasteByDayOfWeekAndCategory(
            @Param("restaurantId") UUID restaurantId,
            @Param("dayOfWeek") int dayOfWeek,
            @Param("category") String category);

    @Query("SELECT DISTINCT wh.category FROM WasteHistory wh " +
            "WHERE wh.restaurantId = :restaurantId")
    List<String> findDistinctCategoriesByRestaurantId(@Param("restaurantId") UUID restaurantId);

    @Query("SELECT COUNT(wh) FROM WasteHistory wh WHERE wh.restaurantId = :restaurantId")
    long countByRestaurantId(@Param("restaurantId") UUID restaurantId);
}
