package com.oose.restaurant_mis.repository;

import com.oose.restaurant_mis.entity.Reservation;
import com.oose.restaurant_mis.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    List<Reservation> findAllByOrderByCreatedAtDesc();

    Reservation findFirstByTable_TableIdAndStatusOrderByReservationTimeDesc(Integer tableId, ReservationStatus status);

    Reservation findTopByTable_TableIdAndStatusOrderByCreatedAtDesc(Integer tableId, ReservationStatus status);

    long countByStatus(ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(r.customerName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (CAST(:startOfDay AS timestamp) IS NULL OR r.reservationTime >= :startOfDay) " +
            "AND (CAST(:endOfDay AS timestamp) IS NULL OR r.reservationTime <= :endOfDay)")
    Page<Reservation> searchReservations(@Param("keyword") String keyword,
                                         @Param("startOfDay") LocalDateTime startOfDay,
                                         @Param("endOfDay") LocalDateTime endOfDay,
                                         Pageable pageable);
}