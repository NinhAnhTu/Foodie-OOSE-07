package com.oose.restaurant_mis.service;

import com.oose.restaurant_mis.entity.User;
import com.oose.restaurant_mis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public User authenticate(String username, String password) throws Exception {
        // Tìm user trong DB
        User user = userRepository.findByUsername(username);

        // Kiểm tra tồn tại và sai mật khẩu
        if (user == null || !user.getPassword().equals(password)) {
            throw new Exception("Sai tên đăng nhập hoặc mật khẩu!");
        }

        // Kiểm tra trạng thái khóa
        if (!user.getStatus()) {
            throw new Exception("Tài khoản này đã bị khóa!");
        }

        // Nếu qua hết các bài kiểm tra thì trả về User
        return user;
    }
}