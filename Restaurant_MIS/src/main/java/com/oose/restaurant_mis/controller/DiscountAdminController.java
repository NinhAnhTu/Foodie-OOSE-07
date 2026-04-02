package com.oose.restaurant_mis.controller;

import com.oose.restaurant_mis.entity.Discount;
import com.oose.restaurant_mis.entity.User;
import com.oose.restaurant_mis.service.DiscountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/api/discounts")
public class DiscountAdminController {

    @Autowired private DiscountService discountService;

    @GetMapping("/content")
    public String getContent(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate,
                             @RequestParam(required = false) Boolean isActive,
                             Model model) {

        LocalDate sDate = (startDate != null && !startDate.trim().isEmpty()) ? LocalDate.parse(startDate) : null;
        LocalDate eDate = (endDate != null && !endDate.trim().isEmpty()) ? LocalDate.parse(endDate) : null;

        // Gọi hàm tìm kiếm
        Page<Discount> discountPage = discountService.searchDiscounts(keyword, sDate, eDate, isActive, page, size);

        model.addAttribute("discountPage", discountPage);
        model.addAttribute("currentPage", page);

        // Trả tham số về lại HTML để giữ nguyên trạng thái filter
        model.addAttribute("keyword", keyword);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("isActive", isActive);

        return "admin/discounts :: content";
    }

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> save(@ModelAttribute Discount discount, HttpSession session) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null) return ResponseEntity.status(401).body("Phiên đăng nhập đã hết hạn!");

        try {
            // Bắt lỗi ngày tháng ở phía Server (Dự phòng)
            if (discount.getEndTime().isBefore(discount.getStartTime())) {
                return ResponseEntity.badRequest().body("Ngày kết thúc không được nhỏ hơn ngày bắt đầu!");
            }

            discountService.save(discount, admin);
            return ResponseEntity.ok("success");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("Tên chương trình giảm giá đã tồn tại!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Đã xảy ra lỗi khi lưu chương trình giảm giá!");
        }
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable Integer id, HttpSession session) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null) return ResponseEntity.status(401).body("Phiên đăng nhập đã hết hạn!");

        try {
            discountService.delete(id, admin);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể xóa mã giảm giá này vì đã có hóa đơn sử dụng!");
        }
    }

    @GetMapping("/edit/{id}")
    @ResponseBody
    public Discount edit(@PathVariable Integer id) {
        return discountService.getById(id);
    }

}