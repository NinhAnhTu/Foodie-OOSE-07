package com.oose.restaurant_mis.controller;

import com.oose.restaurant_mis.entity.Order;
import com.oose.restaurant_mis.entity.MenuItem;
import com.oose.restaurant_mis.entity.OrderDetail;
import com.oose.restaurant_mis.enums.OrderStatus;
import com.oose.restaurant_mis.repository.OrderDetailRepository;
import com.oose.restaurant_mis.service.CategoryService;
import com.oose.restaurant_mis.service.DiningTableService;
import com.oose.restaurant_mis.service.MenuItemService;
import com.oose.restaurant_mis.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/table")
public class CustomerOrderController {
    @Autowired private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;
    @Autowired private OrderService orderService;
    @Autowired private MenuItemService menuItemService;
    @Autowired private CategoryService categoryService;

    public static class CartItemDto implements Serializable {
        private static final long serialVersionUID = 1L;
        private int quantity;
        private String note;

        public CartItemDto(int quantity) {
            this.quantity = quantity;
            this.note = "";
        }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }
    @GetMapping("/login")
    public String showLoginForm(HttpSession session) {
        if (session.getAttribute("customerOrderId") != null) return "redirect:/table/menu";
        return "customer/table-login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String customerName, @RequestParam String tableCode, @RequestParam Integer orderId, HttpSession session, Model model) {
        try {
            Order order = orderService.getOrderById(orderId);
            if (order == null || (!order.getStatus().name().equals("OPEN") && !order.getStatus().name().equals("PENDING_PAYMENT"))) {
                model.addAttribute("error", "Mã hóa đơn không tồn tại hoặc đã được thanh toán!");
                return "customer/table-login";
            }
            if (order.getTable() == null || !order.getTable().getTableCode().equalsIgnoreCase(tableCode.trim())) {
                model.addAttribute("error", "Mã bàn không chính xác!");
                return "customer/table-login";
            }

            //Xóa sạch toàn bộ khoảng trắng và đưa về in thường để so sánh tuyệt đối
            String inputName = customerName.trim();
            String existingName = order.getCustomerName() != null ? order.getCustomerName().trim() : "";

            if (!existingName.isEmpty()) {
                String normalizedInput = inputName.replaceAll("\\s+", "").toLowerCase();
                String normalizedExisting = existingName.replaceAll("\\s+", "").toLowerCase();

                if (!normalizedExisting.equals(normalizedInput)) {
                    model.addAttribute("error", "Bàn đang được dùng bởi khách: " + existingName + ". Nhập đúng tên để tiếp tục!");
                    return "customer/table-login";
                }
            } else {
                order.setCustomerName(inputName);
                orderService.save(order);
            }

            session.setAttribute("customerOrderId", order.getOrderId());
            session.setAttribute("customerName", inputName);
            session.setAttribute("customerTableId", order.getTable().getTableId());

            if (order.getStatus().name().equals("PENDING_PAYMENT")) {
                return "redirect:/table/bill";
            }

            return "redirect:/table/menu";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra, kiểm tra lại thông tin!");
            return "customer/table-login";
        }
    }

    @GetMapping("/menu")
    public String showMenu(@RequestParam(required = false) Integer categoryId,
                           HttpSession session, Model model) {
        Integer orderId = (Integer) session.getAttribute("customerOrderId");
        if (orderId == null) return "redirect:/table/login";

        Order order = orderService.getOrderById(orderId);
        if (order == null || (!order.getStatus().name().equals("OPEN") && !order.getStatus().name().equals("PENDING_PAYMENT"))) {
            clearCustomerSession(session);
            return "redirect:/table/login";
        }

        List<MenuItem> allItems = menuItemService.getAllMenuItems();

        if (categoryId != null) {
            allItems = allItems.stream()
                    .filter(item -> item.getCategory().getCategoryId().equals(categoryId))
                    .toList();
        }

        Map<Integer, CartItemDto> sessionCart = (Map<Integer, CartItemDto>) session.getAttribute("cart");
        Map<MenuItem, CartItemDto> cartMap = new LinkedHashMap<>();
        double cartTotal = 0; int cartCount = 0;

        if (sessionCart != null) {
            for (Map.Entry<Integer, CartItemDto> entry : sessionCart.entrySet()) {
                MenuItem item = menuItemService.getById(entry.getKey());
                if (item != null) {
                    cartMap.put(item, entry.getValue());
                    // Lấy số lượng từ object CartItemDto
                    cartTotal += ((Number) item.getPrice()).doubleValue() * entry.getValue().getQuantity();
                    cartCount += entry.getValue().getQuantity();
                }
            }
        }

        model.addAttribute("order", order);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("menuItems", allItems);
        model.addAttribute("currentCategoryId", categoryId);
        model.addAttribute("cartMap", cartMap);
        model.addAttribute("cartTotal", cartTotal);
        model.addAttribute("cartCount", cartCount);
        return "customer/table-menu";
    }

    @PostMapping("/api/cart/update")
    @ResponseBody
    public String updateCart(@RequestParam Integer itemId, @RequestParam int qtyChange, HttpSession session) {
        Integer orderId = (Integer) session.getAttribute("customerOrderId");
        if (orderId != null) {
            Order order = orderService.getOrderById(orderId);
            if (order.getStatus().name().equals("PENDING_PAYMENT")) return "Lỗi: Đã yêu cầu thanh toán, không thể thêm món!";
        }

        if (qtyChange > 0) {
            MenuItem item = menuItemService.getById(itemId);
            if (item == null || !item.getIsAvailable()) {
                return "Món ăn này hiện đang tạm hết hàng, mong bạn thông cảm!";
            }
        }

        Map<Integer, CartItemDto> cart = (Map<Integer, CartItemDto>) session.getAttribute("cart");
        if (cart == null) cart = new HashMap<>();

        CartItemDto cartItem = cart.getOrDefault(itemId, new CartItemDto(0));
        int newQty = cartItem.getQuantity() + qtyChange;

        if (newQty <= 0) {
            cart.remove(itemId);
        } else {
            cartItem.setQuantity(newQty);
            cart.put(itemId, cartItem);
        }

        session.setAttribute("cart", cart);
        return "success";
    }

    @PostMapping("/api/cart/submit")
    @ResponseBody
    public String submitCart(HttpSession session) {
        Integer orderId = (Integer) session.getAttribute("customerOrderId");
        Map<Integer, CartItemDto> cart = (Map<Integer, CartItemDto>) session.getAttribute("cart");

        if (orderId == null) return "error_session";
        if (cart == null || cart.isEmpty()) return "empty";

        try {
            // Truyền thêm tham số Note vào service
            for (Map.Entry<Integer, CartItemDto> entry : cart.entrySet()) {
                orderService.addItemToOrder(orderId, entry.getKey(), entry.getValue().getQuantity(), entry.getValue().getNote());
            }

            messagingTemplate.convertAndSend("/topic/kitchen", "NEW_ORDER");
            messagingTemplate.convertAndSend("/topic/order/" + orderId, "CUSTOMER_UPDATE");

            session.removeAttribute("cart");
            return "success";
        } catch (Exception e) { return e.getMessage(); }
    }

    @GetMapping("/bill")
    public String showBill(HttpSession session, Model model) {
        Integer orderId = (Integer) session.getAttribute("customerOrderId");
        if (orderId == null) return "redirect:/table/login";

        Order order = orderService.getOrderById(orderId);
        if (order == null || (!order.getStatus().name().equals("OPEN") && !order.getStatus().name().equals("PENDING_PAYMENT"))) {
            clearCustomerSession(session);
            return "redirect:/table/login";
        }
        List<OrderDetail> details = orderService.getOrderDetails(orderId);
        boolean allServed = !details.isEmpty() && details.stream()
                .allMatch(d -> d.getServedStatus().name().equals("SERVED"));
        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderService.getOrderDetails(orderId));
        model.addAttribute("allServed", allServed);
        return "customer/table-bill";
    }

    @PostMapping("/api/checkout")
    @ResponseBody
    public String requestCheckout(HttpSession session) {
        Integer orderId = (Integer) session.getAttribute("customerOrderId");
        if (orderId == null) return "error_session";
        Order order = orderService.getOrderById(orderId);
        if(order.getStatus().name().equals("PENDING_PAYMENT")) return "success";

        if (order.getTotalAmount() == null || order.getTotalAmount() <= 0) {
            return "Chưa có khoản tiền cần thanh toán! Vui lòng gọi món trước khi yêu cầu tính tiền.";
        }
        List<OrderDetail> details = orderService.getOrderDetails(orderId);
        for (OrderDetail detail : details) {
            if (!detail.getServedStatus().name().equals("SERVED")) {
                return "Chưa thể thanh toán! Vui lòng đợi tất cả các món được phục vụ (Hoàn tất) trước khi tính tiền.";
            }
        }
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        orderService.save(order);

        String tableName = order.getTable().getTableName();
        messagingTemplate.convertAndSend("/topic/tables", "CHECKOUT_REQUEST|" + tableName);
        return "success";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        clearCustomerSession(session);
        return "redirect:/table/login";
    }

    private void clearCustomerSession(HttpSession session) {
        session.removeAttribute("customerOrderId");
        session.removeAttribute("customerName");
        session.removeAttribute("customerTableId");
        session.removeAttribute("cart");
    }

    @PostMapping("/api/cart/note")
    @ResponseBody
    public String updateCartNote(@RequestParam Integer itemId, @RequestParam String note, HttpSession session) {
        Map<Integer, CartItemDto> cart = (Map<Integer, CartItemDto>) session.getAttribute("cart");
        if (cart != null && cart.containsKey(itemId)) {
            cart.get(itemId).setNote(note);
            session.setAttribute("cart", cart);
        }
        return "success";
    }
}