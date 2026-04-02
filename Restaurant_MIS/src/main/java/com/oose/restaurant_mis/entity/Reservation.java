package com.oose.restaurant_mis.entity;

import com.oose.restaurant_mis.enums.ReservationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Integer reservationId;

    @ManyToOne
    @JoinColumn(name = "table_id")
    private DiningTable table;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(name = "guest_count", nullable = false)
    private Integer guestCount;

    @Column(name = "reservation_time", nullable = false)
    private LocalDateTime reservationTime;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt;



    public Reservation() {}

    public Reservation(Integer reservationId, DiningTable table, String customerName, String phone,
                       String email, Integer guestCount, LocalDateTime reservationTime,
                       String note, ReservationStatus status, LocalDateTime createdAt) {
        this.reservationId = reservationId;
        this.table = table;
        this.customerName = customerName;
        this.phone = phone;
        this.email = email;
        this.guestCount = guestCount;
        this.reservationTime = reservationTime;
        this.note = note;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Integer getReservationId() { return reservationId; }
    public void setReservationId(Integer reservationId) { this.reservationId = reservationId; }

    public DiningTable getTable() { return table; }
    public void setTable(DiningTable table) { this.table = table; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getGuestCount() { return guestCount; }
    public void setGuestCount(Integer guestCount) { this.guestCount = guestCount; }

    public LocalDateTime getReservationTime() { return reservationTime; }
    public void setReservationTime(LocalDateTime reservationTime) { this.reservationTime = reservationTime; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}