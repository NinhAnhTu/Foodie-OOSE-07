package com.oose.restaurant_mis.controller;

import com.oose.restaurant_mis.entity.DiningTable;
import com.oose.restaurant_mis.entity.Reservation;
import com.oose.restaurant_mis.enums.ReservationStatus;
import com.oose.restaurant_mis.enums.TableStatus;
import com.oose.restaurant_mis.service.DiningTableService;
import com.oose.restaurant_mis.service.EmailService;
import com.oose.restaurant_mis.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin/api/reservations")
public class ReservationAdminController {

    @Autowired private ReservationService reservationService;
    @Autowired private EmailService emailService;
    @Autowired private DiningTableService tableService;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/content")
    public String getContent(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String date,
                             Model model) {

        LocalDate searchDate = (date != null && !date.trim().isEmpty()) ? LocalDate.parse(date) : null;
        Page<Reservation> reservationPage = reservationService.searchReservations(keyword, searchDate, page, size);

        model.addAttribute("reservationPage", reservationPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchDate", date);
        model.addAttribute("tables", tableService.getTablesPage(0, 1000).getContent());

        return "admin/reservations :: content";
    }

    @PostMapping("/accept")
    @ResponseBody
    public ResponseEntity<?> accept(@RequestParam Integer reservationId, @RequestParam Integer tableId) {
        Reservation reservation = reservationService.findById(reservationId);
        DiningTable table = tableService.getById(tableId);

        if (table.getStatus() != TableStatus.EMPTY) {
            return ResponseEntity.badRequest().body("⚠️ Bàn này vừa được nhân viên mở hoặc gán cho khách khác! Vui lòng chọn bàn khác.");
        }

        reservation.setTable(table);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationService.save(reservation);

        table.setStatus(TableStatus.RESERVED);
        tableService.save(table);

        if (reservation.getEmail() != null && !reservation.getEmail().isEmpty()) {
            String timeStr = reservation.getReservationTime().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
            emailService.sendConfirmationEmail(
                    reservation.getEmail(),
                    reservation.getCustomerName(),
                    timeStr,
                    table.getTableName()
            );
        }

        messagingTemplate.convertAndSend("/topic/tables", "UPDATE_TABLE_MAP");
        messagingTemplate.convertAndSend("/topic/tables", "RESERVATION_CONFIRMED|" + table.getTableName());
        messagingTemplate.convertAndSend("/topic/admin", "RESERVATION_UPDATED");
        return ResponseEntity.ok("success");
    }

    @PostMapping("/reject")
    @ResponseBody
    public String reject(@RequestParam Integer reservationId, @RequestParam String reason) {
        Reservation reservation = reservationService.findById(reservationId);
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationService.save(reservation);

        if (reservation.getEmail() != null && !reservation.getEmail().isEmpty()) {
            String timeStr = reservation.getReservationTime().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
            emailService.sendRejectionEmail(
                    reservation.getEmail(),
                    reservation.getCustomerName(),
                    timeStr,
                    reason
            );
        }
        messagingTemplate.convertAndSend("/topic/admin", "RESERVATION_UPDATED");
        return "success";
    }

    @GetMapping("/count-pending")
    @ResponseBody
    public long getPendingCount() {
        return reservationService.countPendingReservations();
    }
}