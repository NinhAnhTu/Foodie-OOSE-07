package com.oose.restaurant_mis.repository;

import com.oose.restaurant_mis.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Page<Category> findAll(Pageable pageable);

    //Search bar
    Page<Category> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}