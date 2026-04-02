package com.oose.restaurant_mis.controller;

import com.oose.restaurant_mis.entity.PasswordReset;
import com.oose.restaurant_mis.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ForgotPasswordController {

    @Autowired private PasswordResetService resetService;

    // Hiển thị form nhập Email
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot_password";
    }

    // Xử lý gửi mail
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, HttpServletRequest request, Model model) {
        String appUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

        boolean isSent = resetService.createAndSendResetToken(email, appUrl);

        if (isSent) {
            model.addAttribute("message", "Link khôi phục mật khẩu đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư.");
        } else {
            model.addAttribute("error", "Không tìm thấy tài khoản nào với email này!");
        }
        return "forgot_password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        PasswordReset reset = resetService.validateToken(token);
        if (reset == null) {
            model.addAttribute("error", "Link khôi phục không hợp lệ hoặc đã hết hạn.");
            return "reset_password";
        }
        model.addAttribute("token", token);
        return "reset_password";
    }

    // Xử lý lưu mật khẩu mới
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token, @RequestParam("password") String password, Model model) {
        PasswordReset reset = resetService.validateToken(token);
        if (reset == null) {
            model.addAttribute("error", "Link khôi phục không hợp lệ hoặc đã hết hạn.");
            return "reset_password";
        }

        resetService.updatePassword(reset, password);
        model.addAttribute("message", "Đổi mật khẩu thành công! Bạn có thể đăng nhập ngay bây giờ.");
        return "reset_password";
    }
}