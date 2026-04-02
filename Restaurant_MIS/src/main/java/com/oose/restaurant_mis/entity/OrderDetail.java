package com.oose.restaurant_mis.entity;

import com.oose.restaurant_mis.enums.ServedStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_details")
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderDetailId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private MenuItem menuItem;

    private Integer quantity;
    private Double unitPrice;
    private String note;

    @Enumerated(EnumType.STRING)
    private ServedStatus servedStatus = ServedStatus.PENDING;

    @PrePersist
    protected void onCreate() {
        // Tự động gán thời gian hiện tại khi món ăn được tạo ra
        this.createdAt = LocalDateTime.now();
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    private LocalDateTime createdAt;

    public OrderDetail() {}

    // Getters và Setters
    public Integer getOrderDetailId() { return orderDetailId; }
    public void setOrderDetailId(Integer id) { this.orderDetailId = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public MenuItem getMenuItem() { return menuItem; }
    public void setMenuItem(MenuItem item) { this.menuItem = item; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer q) { this.quantity = q; }
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double price) { this.unitPrice = price; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public ServedStatus getServedStatus() { return servedStatus; }
    public void setServedStatus(ServedStatus status) { this.servedStatus = status; }
}