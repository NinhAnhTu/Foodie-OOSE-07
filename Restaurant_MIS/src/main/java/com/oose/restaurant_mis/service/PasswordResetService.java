package com.oose.restaurant_mis.service;

import com.oose.restaurant_mis.entity.PasswordReset;
import com.oose.restaurant_mis.entity.User;
import com.oose.restaurant_mis.repository.PasswordResetRepository;
import com.oose.restaurant_mis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordResetRepository resetRepository;
    @Autowired private JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Autowired private EmailService emailService;

    public boolean createAndSendResetToken(String email, String appUrl) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();

        PasswordReset reset = new PasswordReset();
        reset.setUser(user);
        reset.setToken(token);
        reset.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        resetRepository.save(reset);

        // Gọi hàm gửi mail ngầm. Code ở đây sẽ không chờ mail gửi xong mà chạy tiếp luôn!
        emailService.sendResetEmail(user.getEmail(), appUrl, token);

        return true;
    }

    // 3. Xác thực Token có hợp lệ không
    public PasswordReset validateToken(String token) {
        Optional<PasswordReset> resetOpt = resetRepository.findByToken(token);
        if (resetOpt.isEmpty()) return null; // Token sai

        PasswordReset reset = resetOpt.get();
        if (reset.getIsUsed() || reset.getExpiresAt().isBefore(LocalDateTime.now())) {
            return null; // Token đã dùng hoặc hết hạn
        }
        return reset;
    }

    // 4. Cập nhật mật khẩu mới
    public void updatePassword(PasswordReset reset, String newPassword) {
        User user = reset.getUser();

        user.setPassword(newPassword);
        userRepository.save(user);

        // Khóa token lại
        reset.setIsUsed(true);
        resetRepository.save(reset);
    }
}