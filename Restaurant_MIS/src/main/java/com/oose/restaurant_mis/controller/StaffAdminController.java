package com.oose.restaurant_mis.controller;

import com.oose.restaurant_mis.entity.User;
import com.oose.restaurant_mis.enums.RoleType;
import com.oose.restaurant_mis.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/api/staff")
public class StaffAdminController {

    @Autowired private StaffService staffService;

    @GetMapping("/content")
    public String getContent(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String role,
                             Model model) {
        RoleType roleType = null;
        if (role != null && !role.trim().isEmpty()) {
            try {
                roleType = RoleType.valueOf(role);
            } catch (IllegalArgumentException e) {
                roleType = null;
            }
        }

        Page<User> staffPage = staffService.searchStaff(keyword, roleType, page, size);

        model.addAttribute("staffPage", staffPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("roles", RoleType.values());

        model.addAttribute("keyword", keyword);

        model.addAttribute("selectedRole", roleType);

        return "admin/staff :: content";
    }

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> save(@ModelAttribute User user) {
        try {
            staffService.save(user);
            return ResponseEntity.ok("success");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Đã xảy ra lỗi hệ thống: " + e.getMessage());
        }
    }

    @GetMapping("/edit/{id}")
    @ResponseBody
    public User edit(@PathVariable Integer id) {
        return staffService.getById(id);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public String delete(@PathVariable Integer id) {
        try {
            staffService.delete(id);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }
}