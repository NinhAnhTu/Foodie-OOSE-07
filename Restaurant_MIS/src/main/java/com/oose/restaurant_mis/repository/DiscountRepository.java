package com.oose.restaurant_mis.repository;

import com.oose.restaurant_mis.entity.Discount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Integer> {

    @Query("SELECT d FROM Discount d WHERE d.isActive = true " +
            "AND d.startTime <= :now AND d.endTime >= :now")
    List<Discount> findValidDiscounts(LocalDateTime now);

    List<Discount> findByIsActiveTrue();

    @Query("SELECT d FROM Discount d WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (CAST(:startDate AS timestamp) IS NULL OR d.startTime >= :startDate) " +
            "AND (CAST(:endDate AS timestamp) IS NULL OR d.endTime <= :endDate) " +
            "AND (:isActive IS NULL OR d.isActive = :isActive)")
    Page<Discount> searchDiscounts(@Param("keyword") String keyword,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   @Param("isActive") Boolean isActive,
                                   Pageable pageable);
}