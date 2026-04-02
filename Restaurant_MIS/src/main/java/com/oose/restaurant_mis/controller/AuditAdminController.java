package com.oose.restaurant_mis.controller;

import com.oose.restaurant_mis.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuditAdminController {

    @Autowired private AuditService auditService;

    // Route 1:Tải lại giao diện khung Admin
    @GetMapping("/admin/audit-logs")
    public String index() {
        return "admin/index";
    }

    // Route 2: Dành cho AJAX tải dữ liệu bảng (Fragment)
    @GetMapping("/admin/api/audit-logs/content")
    public String getContent(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(required = false) String date,
                             Model model) {


        model.addAttribute("logPage", auditService.searchLogsByDate(date, page, 20));
        model.addAttribute("currentPage", page);
        model.addAttribute("searchDate", date);

        return "admin/audit_logs :: content";
    }
}