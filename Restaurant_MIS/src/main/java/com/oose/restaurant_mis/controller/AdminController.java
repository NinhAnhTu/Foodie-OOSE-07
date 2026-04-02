package com.oose.restaurant_mis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping({"/admin", "/admin/categories", "/admin/dishes", "/admin/tables", "/admin/discounts", "/admin/staff", "/admin/orders", "/admin/dashboard", "/admin/reservations", "/admin/audit_logs"})
    public String showAdminIndex(Model model) {
        return "admin/index";
    }

}