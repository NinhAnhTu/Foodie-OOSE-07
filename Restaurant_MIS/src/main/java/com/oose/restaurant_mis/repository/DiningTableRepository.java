package com.oose.restaurant_mis.repository;

import com.oose.restaurant_mis.entity.DiningTable;
import com.oose.restaurant_mis.enums.TableStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiningTableRepository extends JpaRepository<DiningTable, Integer> {
    @Query("SELECT t.tableId as id, t.tableName as name, t.status as status, t.capacity as capacity, " +
            "(SELECT COUNT(o) > 0 FROM Order o WHERE o.table = t AND o.status = 'PENDING_PAYMENT') as isPending " +
            "FROM DiningTable t")
    List<Object[]> findAllTableStatusWithPendingFlag();

    @Query("SELECT t FROM DiningTable t WHERE " +
            "(:code IS NULL OR :code = '' OR LOWER(t.tableCode) LIKE LOWER(CONCAT('%', :code, '%'))) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:capacity IS NULL OR t.capacity = :capacity)")
    Page<DiningTable> searchTables(@Param("code") String code,
                                   @Param("status") TableStatus status,
                                   @Param("capacity") Integer capacity,
                                   Pageable pageable);
}