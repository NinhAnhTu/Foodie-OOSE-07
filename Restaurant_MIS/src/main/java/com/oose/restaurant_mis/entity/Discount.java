package com.oose.restaurant_mis.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDateTime;

@Entity
@Table(name = "discounts")
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discount_id")
    private Integer discountId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "discount_percent", nullable = false)
    private Integer discountPercent;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @AssertTrue(message = "Ngày kết thúc phải sau ngày bắt đầu")
    public boolean isValidTime() {
        return endTime.isAfter(startTime);
    }

    // Constructors
    public Discount() {}

    public Discount(Integer discountId, String name, Integer discountPercent, LocalDateTime startTime, LocalDateTime endTime, Boolean isActive) {
        this.discountId = discountId;
        this.name = name;
        this.discountPercent = discountPercent;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isActive = isActive;
    }

    // Getters and Setters
    public Integer getDiscountId() { return discountId; }
    public void setDiscountId(Integer discountId) { this.discountId = discountId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(Integer discountPercent) { this.discountPercent = discountPercent; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }
}