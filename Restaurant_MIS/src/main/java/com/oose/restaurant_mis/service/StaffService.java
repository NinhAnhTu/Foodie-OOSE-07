package com.oose.restaurant_mis.service;

import com.oose.restaurant_mis.entity.User;
import com.oose.restaurant_mis.enums.RoleType;
import com.oose.restaurant_mis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class StaffService {

    @Autowired
    private UserRepository userRepository;

    public Page<User> getStaffPage(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size));
    }

    public Page<User> searchStaff(String keyword, RoleType role, int page, int size) {
        return userRepository.searchStaff(keyword, role, PageRequest.of(page, size));
    }

    public User getById(Integer id) {
        return userRepository.findById(id).orElse(new User());
    }

    public void save(User user) {
        boolean isUpdate = user.getUserId() != null;

        if (isUpdate) {
            // Kiểm tra khi cập nhật
            if (userRepository.existsByUsernameAndUserIdNot(user.getUsername(), user.getUserId())) {
                throw new IllegalArgumentException("Tên đăng nhập đã tồn tại!");
            }
            if (userRepository.existsByEmailAndUserIdNot(user.getEmail(), user.getUserId())) {
                throw new IllegalArgumentException("Email đã được sử dụng!");
            }
            if (user.getPhone() != null && !user.getPhone().trim().isEmpty() &&
                    userRepository.existsByPhoneAndUserIdNot(user.getPhone(), user.getUserId())) {
                throw new IllegalArgumentException("Số điện thoại đã được sử dụng!");
            }
        } else {
            // Kiểm tra khi thêm mới
            if (userRepository.existsByUsername(user.getUsername())) {
                throw new IllegalArgumentException("Tên đăng nhập đã tồn tại!");
            }
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new IllegalArgumentException("Email đã được sử dụng!");
            }
            if (user.getPhone() != null && !user.getPhone().trim().isEmpty() &&
                    userRepository.existsByPhone(user.getPhone())) {
                throw new IllegalArgumentException("Số điện thoại đã được sử dụng!");
            }
        }

        // Dữ liệu hợp lệ thì lưu vào Database
        userRepository.save(user);
    }

    // Tách riêng hàm delete bị dính dòng
    public void delete(Integer id) {
        userRepository.deleteById(id);
    }
}