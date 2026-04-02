package com.oose.restaurant_mis.controller;

import com.oose.restaurant_mis.entity.DiningTable;
import com.oose.restaurant_mis.entity.Reservation;
import com.oose.restaurant_mis.enums.TableStatus;
import com.oose.restaurant_mis.service.CategoryService;
import com.oose.restaurant_mis.service.DiningTableService;
import com.oose.restaurant_mis.service.MenuItemService;
import com.oose.restaurant_mis.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired private MenuItemService dishService;
    @Autowired private ReservationService reservationService;
    @Autowired private DiningTableService diningTableService;

    //Công cụ bắn tín hiệu Real-time
    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private CategoryService categoryService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("activePage", "home");
        model.addAttribute("menuItems", dishService.getAllMenuItems());
        return "home";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("activePage", "about");
        return "home";
    }

    @GetMapping("/menu")
    public String menu(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword,
            Model model) {

        model.addAttribute("activePage", "menu");

        // Truyền danh sách loại món để hiển thị lên các nút bấm
        model.addAttribute("categories", categoryService.getAllCategories());

        // Lấy tất cả món ăn
        List<com.oose.restaurant_mis.entity.MenuItem> allItems = dishService.getAllMenuItems();

        // 1. Lọc theo loại món ăn
        if (categoryId != null) {
            allItems = allItems.stream()
                    .filter(item -> item.getCategory().getCategoryId().equals(categoryId))
                    .collect(Collectors.toList());
        }

        // 2. Tìm kiếm theo tên hoặc mô tả
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.toLowerCase().trim();
            allItems = allItems.stream()
                    .filter(item -> item.getName().toLowerCase().contains(kw) ||
                            (item.getDescription() != null && item.getDescription().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
        }

        model.addAttribute("menuItems", allItems);
        model.addAttribute("currentCategoryId", categoryId);
        model.addAttribute("currentKeyword", keyword);
        return "home";
    }

    @PostMapping("/reservation/submit")
    @ResponseBody
    public String submitReservation(
            @RequestParam String customerName,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam Integer guestCount,
            @RequestParam String reservationDate,
            @RequestParam String reservationTime,
            @RequestParam(required = false) String note) {

        try {
            Reservation reservation = new Reservation();
            reservation.setCustomerName(customerName);
            reservation.setPhone(phone);
            reservation.setEmail(email);
            reservation.setGuestCount(guestCount);
            reservation.setNote(note);

            LocalDateTime reservationDateTime = LocalDateTime.parse(reservationDate + "T" + reservationTime);
            reservation.setReservationTime(reservationDateTime);

            reservationService.savePendingReservation(reservation);

            messagingTemplate.convertAndSend("/topic/admin", "NEW_RESERVATION");

            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/api/tables/available")
    @ResponseBody
    public List<DiningTable> getAvailableTables() {
        return diningTableService.getTablesPage(0, 100).getContent()
                .stream()
                .filter(table -> table.getStatus() == TableStatus.EMPTY)
                .toList();
    }
}