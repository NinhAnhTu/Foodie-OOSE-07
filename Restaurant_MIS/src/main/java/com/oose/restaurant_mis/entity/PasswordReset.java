package com.oose.restaurant_mis.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_resets")
public class PasswordReset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer resetId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_used")
    private Boolean isUsed = false;

    public PasswordReset(Integer resetId, User user, String token, LocalDateTime expiresAt, Boolean isUsed, LocalDateTime createdAt) {
        this.resetId = resetId;
        this.user = user;
        this.token = token;
        this.expiresAt = expiresAt;
        this.isUsed = isUsed;
        this.createdAt = createdAt;
    }
    public PasswordReset(){}

    public Integer getResetId() {
        return resetId;
    }

    public void setResetId(Integer resetId) {
        this.resetId = resetId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Boolean used) {
        isUsed = used;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}