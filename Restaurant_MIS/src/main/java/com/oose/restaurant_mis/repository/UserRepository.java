package com.oose.restaurant_mis.repository;

import com.oose.restaurant_mis.entity.User;
import com.oose.restaurant_mis.enums.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Page<User> findAll(Pageable pageable);

    User findByUsername(String username);

    // Truy vấn JPQL tương thích hoàn toàn với Enum
    @Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:role IS NULL OR u.role = :role)")
    Page<User> searchStaff(@Param("keyword") String keyword,
                           @Param("role") RoleType role,
                           Pageable pageable);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    boolean existsByUsernameAndUserIdNot(String username, Integer userId);
    boolean existsByEmailAndUserIdNot(String email, Integer userId);
    boolean existsByPhoneAndUserIdNot(String phone, Integer userId);
}