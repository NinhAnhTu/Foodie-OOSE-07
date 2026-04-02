package com.oose.restaurant_mis.controller;

import com.oose.restaurant_mis.entity.MenuItem;
import com.oose.restaurant_mis.entity.User;
import com.oose.restaurant_mis.service.AuditService;
import com.oose.restaurant_mis.service.CategoryService;
import com.oose.restaurant_mis.service.MenuItemService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Controller
@RequestMapping("/admin/api/dishes")
public class DishAdminController {
    @Autowired private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;
    @Autowired private MenuItemService dishService;
    @Autowired private CategoryService categoryService;
    @Autowired private AuditService auditService;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/content")
    public String getContent(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Integer categoryId,
                             Model model) {

        Page<MenuItem> dishPage = dishService.searchDishes(keyword, categoryId, page, size);
        model.addAttribute("dishPage", dishPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);

        return "admin/dishes :: content";
    }

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> save(@ModelAttribute MenuItem dish, @RequestParam("imageFile") MultipartFile imageFile, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        MenuItem oldDish = null;
        String action = "CREATE";

        if (dish.getItemId() != null) {
            oldDish = dishService.getById(dish.getItemId());
            action = "UPDATE";
        }

        try {
            if (dish.getPrice() != null && dish.getPrice() < 0) {
                return ResponseEntity.badRequest().body("Giá bán không được là số âm!");
            }

            if (!imageFile.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                imageFile.transferTo(new File(uploadDir.getAbsolutePath() + "/" + fileName));
                dish.setImageUrl("/uploads/" + fileName);
            } else if (dish.getItemId() != null) {
                dish.setImageUrl(oldDish.getImageUrl());
            }

            dishService.save(dish);

            // GHI LOG
            MenuItem newDish = dishService.getById(dish.getItemId());
            auditService.log(currentUser, action, "MenuItem", newDish.getItemId(), oldDish, newDish);

            messagingTemplate.convertAndSend("/topic/menu", "MENU_UPDATED");
            return ResponseEntity.ok("success");

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("Tên món ăn này đã tồn tại! Vui lòng nhập tên khác.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Đã xảy ra lỗi hệ thống khi lưu món ăn!");
        }
    }
    @GetMapping("/edit/{id}")
    @ResponseBody
    public MenuItem edit(@PathVariable Integer id) {
        return dishService.getById(id);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public String delete(@PathVariable Integer id, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        MenuItem dish = dishService.getById(id);

        dishService.delete(id);

        // GHI LOG
        auditService.log(currentUser, "DELETE", "MenuItem", id, dish, null);
        messagingTemplate.convertAndSend("/topic/menu", "MENU_UPDATED");

        return "success";
    }
}