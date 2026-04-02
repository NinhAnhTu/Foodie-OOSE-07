package com.oose.restaurant_mis.repository;

import com.oose.restaurant_mis.entity.Order;
import com.oose.restaurant_mis.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Order findTopByTable_TableIdAndStatusOrderByCreatedAtDesc(Integer tableId, OrderStatus status);
    @Query("SELECT o.table.tableId FROM Order o WHERE o.status = :status")
    List<Integer> findTableIdsByStatus(@Param("status") OrderStatus status);
    Order findFirstByTable_TableIdAndStatusInOrderByCreatedAtDesc(
            Integer tableId,
            Collection<OrderStatus> statuses
    );

    // Dashboard
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(o.customerName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (CAST(:startOfDay AS timestamp) IS NULL OR o.createdAt >= :startOfDay) " +
            "AND (CAST(:endOfDay AS timestamp) IS NULL OR o.createdAt <= :endOfDay)")
    Page<Order> searchOrders(@Param("keyword") String keyword,
                             @Param("status") OrderStatus status,
                             @Param("startOfDay") LocalDateTime startOfDay,
                             @Param("endOfDay") LocalDateTime endOfDay,
                             Pageable pageable);
}