package com.oose.restaurant_mis.repository;

import com.oose.restaurant_mis.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {
    Page<MenuItem> findAll(Pageable pageable);
    List<MenuItem> findByIsAvailableTrue();

    //Searh bar
    Page<MenuItem> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<MenuItem> findByCategory_CategoryId(Integer categoryId, Pageable pageable);
    Page<MenuItem> findByNameContainingIgnoreCaseAndCategory_CategoryId(String name, Integer categoryId, Pageable pageable);
}