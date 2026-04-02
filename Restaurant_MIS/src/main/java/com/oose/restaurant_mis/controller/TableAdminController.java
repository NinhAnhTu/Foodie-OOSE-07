package com.oose.restaurant_mis.controller;

import com.oose.restaurant_mis.entity.DiningTable;
import com.oose.restaurant_mis.entity.Reservation;
import com.oose.restaurant_mis.enums.TableStatus;
import com.oose.restaurant_mis.entity.User;
import com.oose.restaurant_mis.service.AuditService;
import com.oose.restaurant_mis.service.DiningTableService;
import com.oose.restaurant_mis.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/api/tables")
public class TableAdminController {

    @Autowired private DiningTableService tableService;
    @Autowired private ReservationService reservationService;
    @Autowired private AuditService auditService;

    @GetMapping("/content")
    public String getContent(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) TableStatus status,
                             @RequestParam(required = false) Integer capacity,
                             Model model) {

        Page<DiningTable> tablePage = tableService.searchTables(keyword, status, capacity, page, size);

        model.addAttribute("tablePage", tablePage);
        model.addAttribute("currentPage", page);
        model.addAttribute("statuses", TableStatus.values());

        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCapacity", capacity);

        return "admin/tables :: content";
    }

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> save(@ModelAttribute DiningTable table, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        DiningTable oldTable = null;
        String action = "CREATE";

        if (table.getTableId() != null) {
            oldTable = tableService.getById(table.getTableId());
            action = "UPDATE";
            table.setStatus(oldTable.getStatus());

        } else {
            table.setStatus(TableStatus.EMPTY);
        }

        try {
            tableService.save(table);
            auditService.log(currentUser, action, "DiningTable", table.getTableId(), oldTable, table);

            return ResponseEntity.ok("success");

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("Mã bàn (Code) này đã tồn tại trên hệ thống! Vui lòng chọn mã khác.");
        } catch (jakarta.validation.ConstraintViolationException e) {
            return ResponseEntity.badRequest().body("Sức chứa của bàn phải từ 1 người trở lên!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Đã xảy ra lỗi hệ thống khi lưu thông tin bàn!");
        }
    }

    @GetMapping("/edit/{id}")
    @ResponseBody
    public DiningTable edit(@PathVariable Integer id) {
        return tableService.getById(id);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable Integer id, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        DiningTable table = tableService.getById(id);

        if (table.getStatus() == TableStatus.OCCUPIED) {
            return ResponseEntity.badRequest().body("Bàn này đang có khách ngồi và hóa đơn đang mở. Bạn không thể xóa bàn lúc này!");
        }

        tableService.delete(id);
        auditService.log(currentUser, "DELETE", "DiningTable", id, table, null);

        return ResponseEntity.ok("success");
    }

    @GetMapping("/reservation/{tableId}")
    @ResponseBody
    public Reservation getTableReservation(@PathVariable Integer tableId) {
        return reservationService.findActiveReservationByTable(tableId);
    }
}