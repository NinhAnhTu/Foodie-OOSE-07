package com.oose.restaurant_mis.controller;

import com.oose.restaurant_mis.entity.User;
import com.oose.restaurant_mis.enums.RoleType;
import com.oose.restaurant_mis.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired private AuthService authService;

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        try {
            User user = authService.authenticate(username, password);

            // Lưu thông tin vào Session
            session.setAttribute("loggedInUser", user);

            // Lấy Enum
            RoleType role = user.getRole();

            // Điều hướng dựa trên giá trị Enum
            if (role == RoleType.ADMIN) return "redirect:/admin/dashboard";
            if (role == RoleType.WAITER) return "redirect:/waiter";
            if (role == RoleType.CHEF) return "redirect:/chef";

            return "redirect:/";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Xóa sạch session
        return "redirect:/auth/login?logout=true";
    }

    @GetMapping("/api/ping")
    @ResponseBody
    public String keepAlive() {
        return "pong";
    }
}