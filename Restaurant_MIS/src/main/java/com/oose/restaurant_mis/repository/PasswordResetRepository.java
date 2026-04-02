package com.oose.restaurant_mis.repository;

import com.oose.restaurant_mis.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, Integer> {
    // Tìm token để xác thực khi user click vào link
    Optional<PasswordReset> findByToken(String token);
}