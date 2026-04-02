package com.oose.restaurant_mis.controller;

import com.oose.restaurant_mis.entity.Category;
import com.oose.restaurant_mis.entity.User;
import com.oose.restaurant_mis.service.AuditService;
import com.oose.restaurant_mis.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/api/categories")
public class CategoryAdminController {

    @Autowired private CategoryService categoryService;
    @Autowired private AuditService auditService;

    @GetMapping("/content")
    public String getContent(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "5") int size,
                             @RequestParam(required = false) String keyword,
                             Model model) {
        Page<Category> categoryPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            categoryPage = categoryService.searchCategories(keyword.trim(), page, size);
        } else {
            categoryPage = categoryService.getCategoriesPage(page, size);
        }

        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);

        return "admin/categories :: content";
    }

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> save(@ModelAttribute Category category, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        Category oldCategory = null;
        String action = "CREATE";

        if (category.getCategoryId() != null) {
            oldCategory = categoryService.getById(category.getCategoryId());
            action = "UPDATE";
        }

        try {
            categoryService.save(category);
            Category newCategory = categoryService.getById(category.getCategoryId());
            auditService.log(currentUser, action, "Category", newCategory.getCategoryId(), oldCategory, newCategory);

            return ResponseEntity.ok("success");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            //Bắt lỗi trùng tên danh mục
            return ResponseEntity.badRequest().body("Tên loại món ăn này đã tồn tại! Vui lòng chọn tên khác.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Đã xảy ra lỗi hệ thống khi lưu!");
        }
    }

    @GetMapping("/edit/{id}")
    @ResponseBody
    public Category edit(@PathVariable Integer id) {
        return categoryService.getById(id);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public String delete(@PathVariable Integer id, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        Category category = categoryService.getById(id);

        categoryService.delete(id);

        // GHI LOG
        auditService.log(currentUser, "DELETE", "Category", id, category, null);

        return "success";
    }
}