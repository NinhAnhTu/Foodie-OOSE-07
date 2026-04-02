package com.oose.restaurant_mis.controller;

import com.oose.restaurant_mis.entity.*;
import com.oose.restaurant_mis.enums.OrderStatus;
import com.oose.restaurant_mis.enums.ReservationStatus;
import com.oose.restaurant_mis.enums.TableStatus;
import com.oose.restaurant_mis.repository.DiscountRepository;
import com.oose.restaurant_mis.repository.ReservationRepository;
import com.oose.restaurant_mis.repository.OrderDetailRepository;
import com.oose.restaurant_mis.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
@RequestMapping("/waiter")
public class WaiterController {
    @Autowired private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;
    @Autowired private OrderService orderService;
    @Autowired private DiningTableService tableService;
    @Autowired private ReservationService reservationService;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private MenuItemService menuItemService;
    @Autowired private DiscountRepository discountRepository;
    @Autowired private OrderDetailRepository detailRepository;
    @Autowired private AuditService auditService;

    @GetMapping({"", "/", "/tables", "/orders"})
    public String index() {
        return "waiter/index";
    }

    @GetMapping("/api/menu-items")
    @ResponseBody
    public List<MenuItem> getMenuItems() {
        return menuItemService.getAllMenuItems();
    }

    @PostMapping("/api/orders/create-from-table")
    @ResponseBody
    public ResponseEntity<?> createOrder(@RequestParam Integer tableId) {
        orderService.createOrderFromTable(tableId);
        messagingTemplate.convertAndSend("/topic/tables", "UPDATE_TABLE_MAP");
        return ResponseEntity.ok("success");
    }

    @PostMapping("/api/reservations/quick-reserve")
    @ResponseBody
    public ResponseEntity<?> reserve(@RequestParam Integer tableId, @ModelAttribute Reservation res) {
        res.setStatus(ReservationStatus.DONE);
        reservationService.saveQuickReservation(tableId, res);
        messagingTemplate.convertAndSend("/topic/tables", "UPDATE_TABLE_MAP");
        return ResponseEntity.ok("success");
    }

    @GetMapping("/api/orders/active-by-table/{tableId}")
    @ResponseBody
    public Order getActiveOrder(@PathVariable Integer tableId) {
        return orderService.findActiveOrderByTable(tableId);
    }

    @GetMapping("/api/reservations/by-table/{tableId}")
    @ResponseBody
    public Reservation getReservation(@PathVariable Integer tableId) {
        return reservationRepository.findTopByTable_TableIdAndStatusOrderByCreatedAtDesc(
                tableId, ReservationStatus.CONFIRMED);
    }

    @PostMapping("/api/orders/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelOrder(@RequestParam Integer tableId, HttpSession session) {
        User waiter = (User) session.getAttribute("loggedInUser");
        if (waiter == null) return ResponseEntity.badRequest().body("Lỗi phiên đăng nhập");

        try {
            Order order = orderService.findActiveOrderByTable(tableId);
            if (order != null) {
                Order oldOrder = orderService.getOrderById(order.getOrderId());

                orderService.cancelOrder(order.getOrderId(), waiter.getUserId());

                Order newOrder = orderService.getOrderById(order.getOrderId());

                if(auditService != null) {
                    auditService.log(waiter, "CANCEL", "Order", order.getOrderId(), oldOrder, newOrder);
                }

                messagingTemplate.convertAndSend("/topic/tables", "UPDATE_TABLE_MAP");
            }
            return ResponseEntity.ok("success");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi hệ thống khi hủy hóa đơn!");
        }
    }

    @PostMapping("/api/reservations/cancel")
    @ResponseBody
    public String cancelReservation(@RequestParam Integer tableId) {
        try {
            reservationService.cancelReservationByTable(tableId);
            return "success";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @GetMapping("/api/categories/list")
    @ResponseBody
    public List<Category> getCategories() {
        return menuItemService.getAllCategories();
    }

    @GetMapping("/api/orders/details/{orderId}")
    @ResponseBody
    public List<OrderDetail> getOrderDetails(@PathVariable Integer orderId) {
        return orderService.getOrderDetails(orderId);
    }

    @PostMapping("/api/orders/add-item")
    @ResponseBody
    public ResponseEntity<?> addItem(@RequestParam Integer orderId, @RequestParam Integer itemId, @RequestParam Integer quantity, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        try {
            MenuItem item = menuItemService.getById(itemId);
            if (item == null || !item.getIsAvailable()) {
                return ResponseEntity.badRequest().body("Lỗi: Món này hiện đang tạm hết hàng!");
            }

            Order oldOrder = orderService.getOrderById(orderId);
            orderService.addItemToOrder(orderId, itemId, quantity);
            Order newOrder = orderService.getOrderById(orderId);

            if(auditService != null) auditService.log(currentUser, "ADD_ITEM", "Order", orderId, oldOrder, newOrder);

            messagingTemplate.convertAndSend("/topic/kitchen", "NEW_ORDER");
            messagingTemplate.convertAndSend("/topic/order/" + orderId, "WAITER_UPDATE|ADD|" + item.getName());

            return ResponseEntity.ok("success");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống!");
        }
    }

    @PostMapping("/api/orders/remove-item")
    @ResponseBody
    public ResponseEntity<?> removeItem(@RequestParam Integer detailId, @RequestParam(required = false) Integer quantity) {
        try {
            OrderDetail detail = detailRepository.findById(detailId).orElse(null);
            String itemName = "";
            Integer orderId = null;

            if (detail != null) {
                itemName = detail.getMenuItem().getName();
                orderId = detail.getOrder().getOrderId();
            }

            orderService.removeItemFromOrder(detailId, quantity);

            if (orderId != null) {
                messagingTemplate.convertAndSend("/topic/order/" + orderId, "WAITER_UPDATE|REMOVE|" + itemName);
                messagingTemplate.convertAndSend("/topic/kitchen", "ITEM_REMOVED|" + orderId + "|" + itemName);
            }
            return ResponseEntity.ok("success");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/api/orders/pay")
    @ResponseBody
    public ResponseEntity<?> payOrder(@RequestParam Integer orderId, @RequestParam String method, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        try {
            Order oldOrder = orderService.getOrderById(orderId);
            orderService.payOrder(orderId, method, currentUser.getUserId());
            Order newOrder = orderService.getOrderById(orderId);

            if(auditService != null) auditService.log(currentUser, "PAY", "Order", orderId, oldOrder, newOrder);

            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/api/discounts/active")
    @ResponseBody
    public List<Discount> getActiveDiscounts() {
        return discountRepository.findByIsActiveTrue();
    }

    @PostMapping("/api/orders/apply-discount")
    @ResponseBody
    public ResponseEntity<?> applyDiscount(@RequestParam Integer orderId, @RequestParam(required = false) Integer discountId) {
        orderService.applyDiscountToOrder(orderId, discountId);
        return ResponseEntity.ok("success");
    }

    @GetMapping("/api/tables/content")
    public String getTableContent(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) TableStatus status,
                                  Model model) {
        List<DiningTable> tables = tableService.getAllTables();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.toLowerCase().trim();
            tables = tables.stream()
                    .filter(t -> {
                        String name = t.getTableName() != null ? t.getTableName().toLowerCase() : "";
                        String code = t.getTableCode() != null ? t.getTableCode().toLowerCase() : "";
                        return name.contains(kw) || code.contains(kw);
                    })
                    .toList();
        }

        if (status != null) {
            tables = tables.stream()
                    .filter(t -> t.getStatus() == status)
                    .toList();
        }

        for (DiningTable table : tables) {
            if (table.getStatus() == TableStatus.OCCUPIED) {
                Order activeOrder = orderService.findActiveOrderByTable(table.getTableId());
                if (activeOrder != null && activeOrder.getStatus() == OrderStatus.PENDING_PAYMENT) {
                    table.setHasPendingPayment(true);
                }
            }
        }

        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("tables", tables);

        return "waiter/tables :: content";
    }

    @GetMapping("/api/orders/get/{orderId}")
    @ResponseBody
    public Order getOrder(@PathVariable Integer orderId) {
        return orderService.getOrderById(orderId);
    }

    @GetMapping("/print-invoice/{orderId}")
    public String printInvoice(@PathVariable Integer orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        List<OrderDetail> details = orderService.getOrderDetails(orderId);

        model.addAttribute("order", order);
        model.addAttribute("details", details);
        model.addAttribute("bankId", "970423");
        model.addAttribute("accountNo", "0123456789");

        return "waiter/invoice_print";
    }

    @PostMapping("/api/reservations/notify-admin")
    @ResponseBody
    public ResponseEntity<?> notifyAdminReservation() {
        messagingTemplate.convertAndSend("/topic/admin", "NEW_RESERVATION");
        return ResponseEntity.ok("success");
    }

    @PostMapping("/api/reservations/notify-waiter")
    @ResponseBody
    public ResponseEntity<?> notifyWaiterReservation(@RequestParam String tableName) {
        messagingTemplate.convertAndSend("/topic/tables", "RESERVATION_CONFIRMED|" + tableName);
        return ResponseEntity.ok("success");
    }
}