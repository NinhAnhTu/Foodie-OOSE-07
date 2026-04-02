package com.oose.restaurant_mis.repository;

import com.oose.restaurant_mis.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {

    // Tìm kiếm nhật ký kèm phân trang, sắp xếp mới nhất lên đầu
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Lọc nhật ký theo User hoặc Hành động hoặc Khoảng thời gian
    @Query("SELECT l FROM AuditLog l WHERE " +
            "(:userId IS NULL OR l.user.userId = :userId) AND " +
            "(:action IS NULL OR l.action LIKE %:action%) AND " +
            "(:tableName IS NULL OR l.tableName = :tableName) AND " +
            "(CAST(:startDate AS timestamp) IS NULL OR l.createdAt >= :startDate) AND " +
            "(CAST(:endDate AS timestamp) IS NULL OR l.createdAt <= :endDate)")
    Page<AuditLog> searchLogs(@Param("userId") Integer userId,
                              @Param("action") String action,
                              @Param("tableName") String tableName,
                              @Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate,
                              Pageable pageable);
}