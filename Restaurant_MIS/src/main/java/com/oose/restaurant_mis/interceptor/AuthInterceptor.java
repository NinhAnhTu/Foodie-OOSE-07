package com.oose.restaurant_mis.interceptor;

import com.oose.restaurant_mis.entity.User;
import com.oose.restaurant_mis.enums.RoleType; // Thêm import RoleType
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loggedInUser");

        // 1. Kiểm tra nếu chưa đăng nhập
        if (user == null) {
            response.sendRedirect("/auth/login");
            return false;
        }

        // 2. Kiểm tra quyền truy cập (Role-based Authorization)
        String uri = request.getRequestURI();
        RoleType role = user.getRole(); // Lấy trực tiếp Enum RoleType

        // Kiểm tra quyền Admin
        if (uri.startsWith("/admin") && role != RoleType.ADMIN) {
            response.sendRedirect("/auth/login?error=access_denied");
            return false;
        }

        // Kiểm tra quyền Phục vụ
        if (uri.startsWith("/waiter") && role != RoleType.WAITER) {
            response.sendRedirect("/auth/login?error=access_denied");
            return false;
        }

        // (Bổ sung thêm) Kiểm tra quyền Đầu bếp nếu URL bắt đầu bằng /chef
        if (uri.startsWith("/chef") && role != RoleType.CHEF) {
            response.sendRedirect("/auth/login?error=access_denied");
            return false;
        }

        return true; // Cho phép đi tiếp nếu mọi thứ ổn
    }
}