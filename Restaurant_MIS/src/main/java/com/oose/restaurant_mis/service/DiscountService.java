package com.oose.restaurant_mis.service;

import com.oose.restaurant_mis.entity.Discount;
import com.oose.restaurant_mis.entity.User;
import com.oose.restaurant_mis.repository.DiscountRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class DiscountService {
    @Autowired private DiscountRepository discountRepository;
    @Autowired private AuditService auditService;
    public Page<Discount> getDiscountsPage(int page, int size) {
        return discountRepository.findAll(PageRequest.of(page, size));
    }

    public Page<Discount> searchDiscounts(String keyword, LocalDate startDate, LocalDate endDate, Boolean isActive, int page, int size) {
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime end = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;
        return discountRepository.searchDiscounts(keyword, start, end, isActive, PageRequest.of(page, size));
    }

    public Discount getById(Integer id) {
        return discountRepository.findById(id).orElse(new Discount());
    }

    @Transactional
    public void save(Discount discount, User admin) {
        Discount oldValue = null;
        String action = "TẠO MỚI GIẢM GIÁ";

        // Kiểm tra nếu là cập nhật
        if (discount.getDiscountId() != null) {
            // Lấy dữ liệu cũ từ DB trước khi lưu đè
            oldValue = discountRepository.findById(discount.getDiscountId()).orElse(null);
            action = "CẬP NHẬT GIẢM GIÁ";
        }

        // Thực hiện lưu
        Discount newValue = discountRepository.save(discount);

        // Ghi vào nhật ký hệ thống
        auditService.log(
                admin,
                action,
                "discounts",
                newValue.getDiscountId(),
                oldValue,
                newValue
        );
    }
    @Transactional
    public void delete(Integer id, User admin) {
        // Lấy dữ liệu trước khi xóa để lưu log (biết đã xóa cái gì)
        Discount oldValue = discountRepository.findById(id).orElse(null);

        discountRepository.deleteById(id);

        // Ghi log hành động xóa
        auditService.log(
                admin,
                "XÓA GIẢM GIÁ",
                "discounts",
                id,
                oldValue,
                null // newValue là null vì đối tượng đã bị xóa
        );
    }}