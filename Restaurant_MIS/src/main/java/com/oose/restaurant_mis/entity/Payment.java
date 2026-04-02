package com.oose.restaurant_mis.entity;

import com.oose.restaurant_mis.enums.PaymentMethod;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer paymentId;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private Double amountPaid;

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod; // CASH, TRANSFER

    private String transactionCode;
    private LocalDateTime paymentTime = LocalDateTime.now();
    public Payment() {}

    // Getters và Setters (Viết đầy đủ như các class trước)
    public Integer getPaymentId() { return paymentId; }
    public void setPaymentId(Integer id) { this.paymentId = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(Double amount) { this.amountPaid = amount; }
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String code) { this.transactionCode = code; }
    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime time) { this.paymentTime = time; }
}