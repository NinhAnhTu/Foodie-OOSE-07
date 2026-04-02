package com.oose.restaurant_mis.service;

import com.oose.restaurant_mis.entity.*;
import com.oose.restaurant_mis.enums.*;
import com.oose.restaurant_mis.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderDetailRepository detailRepository;
    @Autowired private MenuItemRepository itemRepository;
    @Autowired private DiningTableRepository tableRepository;
    @Autowired private DiscountRepository discountRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;
    @Autowired private PaymentRepository paymentRepository;

    public Page<Order> getOrdersPage(int page, int size) {
        return orderRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public Order getById(Integer id) {
        return orderRepository.findById(id).orElse(new Order());
    }

    public void save(Order order) {
        if (order.getOrderId() == null) {
            order.setCreatedAt(LocalDateTime.now());
        }
        orderRepository.save(order);
    }

    @Transactional
    public void delete(Integer id) {
        List<OrderDetail> details = detailRepository.findByOrder_OrderId(id);
        if (details != null && !details.isEmpty()) {
            detailRepository.deleteAll(details);
        }
        orderRepository.deleteById(id);
    }

    @Transactional
    public void addItemToOrder(Integer orderId, Integer itemId, Integer quantity) {
        addItemToOrder(orderId, itemId, quantity, null);
    }

    @Transactional
    public void addItemToOrder(Integer orderId, Integer itemId, Integer quantity, String note) {
        if (quantity <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0!");
        }

        Order order = orderRepository.findById(orderId).orElseThrow();

        if (order.getStatus() == OrderStatus.PAID) {
            throw new RuntimeException("Hóa đơn đã thanh toán, không thể chỉnh sửa!");
        }

        if (order.getCustomerName() == null || order.getCustomerName().trim().isEmpty()) {
            throw new RuntimeException("Bạn không thể thêm món nếu chưa mở menu cho khách.");
        }

        MenuItem item = itemRepository.findById(itemId).orElseThrow();

        // 🔥 FIX Ở ĐÂY
        OrderDetail detail = detailRepository
                .findFirstByOrder_OrderIdAndMenuItem_ItemIdAndServedStatus(
                        orderId, itemId, ServedStatus.PENDING
                );

        if (detail != null) {
            // 👉 Gộp món chưa làm
            detail.setQuantity(detail.getQuantity() + quantity);

            if (note != null && !note.isEmpty()) {
                detail.setNote(detail.getNote() != null
                        ? detail.getNote() + "; " + note
                        : note);
            }

        } else {
            // 👉 Tạo dòng mới nếu đã SERVED hoặc chưa có
            detail = new OrderDetail();
            detail.setOrder(order);
            detail.setMenuItem(item);
            detail.setQuantity(quantity);
            detail.setUnitPrice(item.getPrice());
            detail.setNote(note);
            detail.setServedStatus(ServedStatus.PENDING);
        }

        detailRepository.save(detail);
        recalculateTotal(orderId);
    }

    @Transactional
    public void removeItemFromOrder(Integer detailId, Integer quantityToRemove) {
        OrderDetail detail = detailRepository.findById(detailId).orElseThrow(() -> new RuntimeException("Không tìm thấy món!"));
        Order order = detail.getOrder();

        if (order.getStatus() == OrderStatus.PAID) {
            throw new RuntimeException("Hóa đơn đã thanh toán, không thể xóa món!");
        }

        // CHẶN HỦY MÓN ĐÃ PHỤC VỤ
        if (detail.getServedStatus() == ServedStatus.SERVED) {
            throw new RuntimeException("Món này đã được bếp nấu xong và phục vụ, không thể hủy!");
        }

        // Logic trừ bớt số lượng hoặc xóa hẳn
        if (quantityToRemove != null && quantityToRemove > 0 && quantityToRemove < detail.getQuantity()) {
            detail.setQuantity(detail.getQuantity() - quantityToRemove);
            detailRepository.save(detail);
        } else {
            detailRepository.delete(detail);
        }

        recalculateTotal(order.getOrderId());
    }

    @Transactional
    public void processPayment(Integer orderId, Integer discountId, Integer staffId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        List<OrderDetail> details = detailRepository.findByOrder_OrderId(orderId);
        boolean hasUnservedItems = details.stream().anyMatch(d -> d.getServedStatus() != ServedStatus.SERVED);
        if (hasUnservedItems) {
            throw new RuntimeException("Không thể thanh toán! Hóa đơn này vẫn còn món chưa được Đầu bếp phục vụ xong.");
        }

        if (discountId != null && discountId > 0) {
            Discount discount = discountRepository.findById(discountId).orElse(null);
            if (discount != null && discount.getIsActive()) {
                order.setDiscount(discount);
                double total = order.getTotalAmount();
                order.setFinalAmount(total * (1 - (double)discount.getDiscountPercent()/100));
            }
        } else {
            order.setDiscount(null);
            order.setFinalAmount(order.getTotalAmount());
        }

        order.setStatus(OrderStatus.PAID);
        order.setClosedAt(LocalDateTime.now());

        User staff = userRepository.findById(staffId).orElseThrow();
        order.setStaff(staff);

        DiningTable table = order.getTable();
        table.setStatus(TableStatus.EMPTY);
        tableRepository.save(table);

        orderRepository.save(order);
    }

    public void recalculateTotal(Integer orderId) {
        Order order = orderRepository.findById(orderId).get();
        List<OrderDetail> details = detailRepository.findByOrder_OrderId(orderId);

        double total = details.stream()
                .mapToDouble(d -> d.getUnitPrice() * d.getQuantity())
                .sum();

        order.setTotalAmount(total);

        if (order.getDiscount() != null && order.getDiscount().getIsActive()) {
            double pct = order.getDiscount().getDiscountPercent();
            order.setFinalAmount(total * (1 - pct/100));
        } else {
            order.setFinalAmount(total);
        }
        orderRepository.save(order);
    }

    @Transactional
    public void createOrderFromTable(Integer tableId) {
        DiningTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn!"));

        Reservation activeRes = reservationRepository
                .findTopByTable_TableIdAndStatusOrderByCreatedAtDesc(tableId, ReservationStatus.CONFIRMED);

        Order order = new Order();
        order.setTable(table);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.OPEN);
        order.setTotalAmount(0.0);
        order.setFinalAmount(0.0);

        if (activeRes != null) {
            activeRes.setStatus(ReservationStatus.DONE);
            reservationRepository.save(activeRes);
            order.setCustomerName(activeRes.getCustomerName());
        }

        orderRepository.save(order);

        table.setStatus(TableStatus.OCCUPIED);
        tableRepository.save(table);
    }

    public Order findActiveOrderByTable(Integer tableId) {
        return orderRepository.findFirstByTable_TableIdAndStatusInOrderByCreatedAtDesc(
                tableId,
                List.of(OrderStatus.OPEN, OrderStatus.PENDING_PAYMENT)
        );
    }

    public List<OrderDetail> getOrderDetails(Integer orderId) {
        return detailRepository.findByOrder_OrderId(orderId);
    }

    @Transactional
    public void payOrder(Integer orderId, String methodStr, Integer staffId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getFinalAmount() == null || order.getFinalAmount() <= 0) {
            throw new RuntimeException("Không có khoản tiền cần thanh toán! Nếu khách không dùng bữa, vui lòng Hủy hóa đơn.");
        }

        List<OrderDetail> details = detailRepository.findByOrder_OrderId(orderId);
        boolean hasUnservedItems = details.stream()
                .anyMatch(d -> d.getServedStatus() != ServedStatus.SERVED);

        if (hasUnservedItems) {
            throw new RuntimeException("Không thể thanh toán! Hóa đơn này vẫn còn món chưa được Đầu bếp phục vụ xong.");
        }
        // [CODE MỚI]: Tìm User và lưu vào order.staff
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên!"));
        order.setStaff(staff);
        order.setStatus(OrderStatus.PAID);
        order.setClosedAt(LocalDateTime.now());
        orderRepository.save(order);

        if (order.getTable() != null) {
            DiningTable table = order.getTable();
            table.setStatus(TableStatus.EMPTY);
            tableRepository.save(table);
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmountPaid(order.getFinalAmount());

        try {
            PaymentMethod method = PaymentMethod.valueOf(methodStr);
            payment.setPaymentMethod(method);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Phương thức thanh toán không hợp lệ: " + methodStr);
        }

        if (PaymentMethod.TRANSFER.name().equals(methodStr)) {
            payment.setTransactionCode("TRX" + System.currentTimeMillis());
        }

        paymentRepository.save(payment);
        messagingTemplate.convertAndSend("/topic/order/" + orderId, "PAID_SUCCESS");
    }

    public Order getOrderById(Integer orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    @Transactional
    public void applyDiscountToOrder(Integer orderId, Integer discountId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (discountId != null && discountId > 0) {
            Discount discount = discountRepository.findById(discountId)
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại"));
            order.setDiscount(discount);
        } else {
            order.setDiscount(null);
        }

        orderRepository.save(order);
        recalculateTotal(orderId);
    }

    public List<OrderDetail> getKitchenOrders() {
        List<OrderDetail> details = detailRepository.findByServedStatusInOrderByOrderDetailIdAsc(
                List.of(ServedStatus.PENDING, ServedStatus.COOKING)
        );
        return details.stream().filter(d -> d.getOrder() != null).toList();
    }

    @Transactional
    public void updateItemStatus(Integer detailId, ServedStatus newStatus) {
        OrderDetail detail = detailRepository.findById(detailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn"));

        detail.setServedStatus(newStatus);
        detailRepository.save(detail);

        messagingTemplate.convertAndSend("/topic/order/" + detail.getOrder().getOrderId(), "ITEM_STATUS_UPDATED");
    }

    public Page<Order> searchOrders(String keyword, OrderStatus status, java.time.LocalDate date, int page, int size) {
        LocalDateTime startOfDay = (date != null) ? date.atStartOfDay() : null;
        LocalDateTime endOfDay = (date != null) ? date.atTime(java.time.LocalTime.MAX) : null;

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.searchOrders(keyword, status, startOfDay, endOfDay, pageRequest);
    }
    @Transactional
    public void cancelOrder(Integer orderId, Integer staffId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        List<OrderDetail> details = detailRepository.findByOrder_OrderId(orderId);
        if (details != null && !details.isEmpty()) {
            throw new RuntimeException("Không thể hủy hóa đơn đang có món. Vui lòng xóa hết các món trước khi hủy!");
        }

        // [CẬP NHẬT]: Lưu người hủy hóa đơn
        if (staffId != null) {
            User staff = userRepository.findById(staffId).orElse(null);
            if (staff != null) {
                order.setStaff(staff);
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setClosedAt(LocalDateTime.now());
        orderRepository.save(order);

        if (order.getTable() != null) {
            DiningTable table = order.getTable();
            table.setStatus(TableStatus.EMPTY);
            tableRepository.save(table);
        }
    }
}