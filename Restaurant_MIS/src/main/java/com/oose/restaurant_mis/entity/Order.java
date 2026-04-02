package com.oose.restaurant_mis.entity;

import com.oose.restaurant_mis.enums.OrderStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    @ManyToOne
    @JoinColumn(name = "table_id", nullable = false)
    private DiningTable table;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private User staff;

    @ManyToOne
    @JoinColumn(name = "discount_id")
    private Discount discount;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "total_amount")
    private Double totalAmount = 0.0;

    @Column(name = "final_amount")
    private Double finalAmount = 0.0;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.OPEN;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    // Constructors
    public Order() {}

    public Order(Integer orderId, DiningTable table, User staff, Discount discount, String customerName, Double totalAmount, Double finalAmount, OrderStatus status, LocalDateTime createdAt, LocalDateTime closedAt) {
        this.orderId = orderId;
        this.table = table;
        this.staff = staff;
        this.discount = discount;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.finalAmount = finalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.closedAt = closedAt;
    }

    // Getters and Setters
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public DiningTable getTable() { return table; }
    public void setTable(DiningTable table) { this.table = table; }

    public User getStaff() { return staff; }
    public void setStaff(User staff) { this.staff = staff; }

    public Discount getDiscount() { return discount; }
    public void setDiscount(Discount discount) { this.discount = discount; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public Double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(Double finalAmount) { this.finalAmount = finalAmount; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
}