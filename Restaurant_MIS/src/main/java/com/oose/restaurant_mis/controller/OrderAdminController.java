package com.oose.restaurant_mis.controller;

import com.oose.restaurant_mis.entity.*;
import com.oose.restaurant_mis.enums.OrderStatus;
import com.oose.restaurant_mis.repository.DiscountRepository;
import com.oose.restaurant_mis.repository.OrderDetailRepository;
import com.oose.restaurant_mis.service.AuditService;
import com.oose.restaurant_mis.service.DiningTableService;
import com.oose.restaurant_mis.service.MenuItemService;
import com.oose.restaurant_mis.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/api/orders")
public class OrderAdminController {

    @Autowired private OrderService orderService;
    @Autowired private DiningTableService tableService;
    @Autowired private OrderDetailRepository detailRepository;
    @Autowired private MenuItemService menuItemService;
    @Autowired private DiscountRepository discountRepository;
    @Autowired private AuditService auditService;

    @GetMapping("/content")
    public String getContent(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) OrderStatus status,
                             @RequestParam(required = false) String date,
                             Model model) {

        LocalDate searchDate = (date != null && !date.trim().isEmpty()) ? LocalDate.parse(date) : null;

        Page<Order> orderPage = orderService.searchOrders(keyword, status, searchDate, page, size);

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("tables", tableService.getAllTables());

        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("searchDate", date);
        model.addAttribute("allStatuses", OrderStatus.values());

        return "admin/orders :: content";
    }

    @PostMapping("/create")
    @ResponseBody
    public String createOrder(@RequestParam Integer tableId, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        Order active = orderService.findActiveOrderByTable(tableId);
        if (active != null) return "Bàn này đang có hóa đơn mở!";

        orderService.createOrderFromTable(tableId);

        // Lấy hóa đơn vừa tạo ra để ghi log
        Order newOrder = orderService.findActiveOrderByTable(tableId);
        auditService.log(currentUser, "CREATE_ORDER", "Order", newOrder.getOrderId(), null, newOrder);

        return "success";
    }

    @GetMapping("/details/{orderId}")
    @ResponseBody
    public List<java.util.Map<String, Object>> getOrderDetails(@PathVariable Integer orderId) {
        List<OrderDetail> details = detailRepository.findByOrder_OrderId(orderId);
        return details.stream().map(d -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("detailId", d.getOrderDetailId());
            map.put("servedStatus", d.getServedStatus().name());

            map.put("quantity", d.getQuantity());
            map.put("unitPrice", d.getUnitPrice());

            java.util.Map<String, Object> menuItem = new java.util.HashMap<>();
            menuItem.put("name", d.getMenuItem().getName());
            map.put("menuItem", menuItem);

            return map;
        }).collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/menu-items")
    @ResponseBody
    public List<java.util.Map<String, Object>> getMenuItems() {
        return menuItemService.getAllActiveItems().stream().map(m -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("itemId", m.getItemId());
            map.put("name", m.getName());
            map.put("price", m.getPrice());
            return map;
        }).collect(java.util.stream.Collectors.toList());
    }

    @PostMapping("/add-item")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> addItem(@RequestParam Integer orderId, @RequestParam Integer itemId, @RequestParam Integer quantity, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        try {
            Order oldOrder = orderService.getOrderById(orderId);
            orderService.addItemToOrder(orderId, itemId, quantity);
            Order newOrder = orderService.getOrderById(orderId);
            auditService.log(currentUser, "ADD_ITEM", "Order", orderId, oldOrder, newOrder);
            return org.springframework.http.ResponseEntity.ok("success");
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/pay")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> pay(@RequestParam Integer orderId,
                                                          @RequestParam(required = false) String discountId,
                                                          HttpSession session) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null) return org.springframework.http.ResponseEntity.badRequest().body("Lỗi phiên đăng nhập");

        Integer dId = (discountId != null && !discountId.isEmpty()) ? Integer.parseInt(discountId) : null;
        try {
            Order oldOrder = orderService.getOrderById(orderId);
            orderService.processPayment(orderId, dId, admin.getUserId());
            Order newOrder = orderService.getOrderById(orderId);
            auditService.log(admin, "PAY", "Order", orderId, oldOrder, newOrder);
            return org.springframework.http.ResponseEntity.ok("success");
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/active-discounts")
    @ResponseBody
    public List<java.util.Map<String, Object>> getActiveDiscounts() {
        return discountRepository.findByIsActiveTrue().stream().map(d -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("discountId", d.getDiscountId());
            map.put("name", d.getName());
            map.put("discountPercent", d.getDiscountPercent());
            return map;
        }).collect(java.util.stream.Collectors.toList());
    }

    @PostMapping("/remove-item")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> removeItem(@RequestParam Integer detailId, @RequestParam(required = false) Integer quantity, HttpSession session) {
        try {
            orderService.removeItemFromOrder(detailId, quantity);
            return org.springframework.http.ResponseEntity.ok("success");
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/cancel")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> cancelOrder(@RequestParam Integer orderId, HttpSession session) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null) return org.springframework.http.ResponseEntity.badRequest().body("Lỗi phiên đăng nhập");

        try {
            Order oldOrder = orderService.getOrderById(orderId);
            orderService.cancelOrder(orderId, admin.getUserId());
            Order newOrder = orderService.getOrderById(orderId);

            auditService.log(admin, "CANCEL", "Order", orderId, oldOrder, newOrder);

            return org.springframework.http.ResponseEntity.ok("success");
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}