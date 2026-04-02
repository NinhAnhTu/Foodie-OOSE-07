package com.oose.restaurant_mis.service;

import com.oose.restaurant_mis.entity.DiningTable;
import com.oose.restaurant_mis.entity.Reservation;
import com.oose.restaurant_mis.enums.ReservationStatus;
import com.oose.restaurant_mis.enums.TableStatus;
import com.oose.restaurant_mis.repository.DiningTableRepository;
import com.oose.restaurant_mis.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private DiningTableRepository diningTableRepository;

    // NEW
    @Transactional
    public void savePendingReservation(Reservation reservation) {
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setStatus(ReservationStatus.PENDING);
        // Bỏ logic set status của bàn ăn đi, table_id hiện tại đang null
        reservationRepository.save(reservation);
    }

    public List<Reservation> getAll() {
        return reservationRepository.findAllByOrderByCreatedAtDesc();
    }

    // Lấy thông tin đặt bàn theo ID
    public Reservation findById(Integer id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mã đặt bàn: " + id));
    }

    // Lưu cập nhật Reservation
    @Transactional
    public void save(Reservation reservation) {
        reservationRepository.save(reservation);
    }
    // Lấy thông tin đặt bàn hiện tại của một bàn ăn cụ thể
    public Reservation findActiveReservationByTable(Integer tableId) {
        return reservationRepository.findFirstByTable_TableIdAndStatusOrderByReservationTimeDesc(tableId, ReservationStatus.CONFIRMED);
    }

    @Transactional
    public void saveQuickReservation(Integer tableId, Reservation res) {
        DiningTable table = diningTableRepository.findById(tableId).orElseThrow();

        res.setStatus(ReservationStatus.CONFIRMED);
        res.setTable(table);
        reservationRepository.save(res);

        table.setStatus(TableStatus.RESERVED);
        diningTableRepository.save(table);
    }

    @Transactional
    public void cancelReservationByTable(Integer tableId) {
        // 1. Tìm phiếu đặt bàn đang ở trạng thái CONFIRMED (đang giữ bàn) của bàn này
        Reservation res = reservationRepository.findTopByTable_TableIdAndStatusOrderByCreatedAtDesc(
                tableId, ReservationStatus.CONFIRMED);

        if (res != null) {
            // 2. Chuyển trạng thái phiếu đặt sang CANCELLED
            res.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(res);
        }

        // 3. Giải phóng bàn: Chuyển trạng thái bàn về EMPTY (Màu xanh)
        DiningTable table = diningTableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn để giải phóng!"));

        table.setStatus(TableStatus.EMPTY);
        diningTableRepository.save(table);
    }

    public Page<Reservation> searchReservations(String keyword, LocalDate date, int page, int size) {
        LocalDateTime startOfDay = (date != null) ? date.atStartOfDay() : null;
        LocalDateTime endOfDay = (date != null) ? date.atTime(LocalTime.MAX) : null;

        // Sắp xếp các đơn mới nhất lên đầu tiên
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reservationRepository.searchReservations(keyword, startOfDay, endOfDay, pageRequest);
    }
    public long countPendingReservations() {
        return reservationRepository.countByStatus(ReservationStatus.PENDING);
    }
}