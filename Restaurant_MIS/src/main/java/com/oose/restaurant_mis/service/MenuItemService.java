package com.oose.restaurant_mis.service;

import com.oose.restaurant_mis.entity.Category;
import com.oose.restaurant_mis.entity.MenuItem;
import com.oose.restaurant_mis.repository.CategoryRepository;
import com.oose.restaurant_mis.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuItemService {
    @Autowired private MenuItemRepository dishRepository;
    @Autowired private CategoryRepository categoryRepository;


    public Page<MenuItem> getDishesPage(int page, int size) {
        return dishRepository.findAll(PageRequest.of(page, size));
    }

    public Page<MenuItem> searchDishes(String keyword, Integer categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        boolean hasKeyword = (keyword != null && !keyword.trim().isEmpty());
        boolean hasCategory = (categoryId != null && categoryId > 0);

        if (hasKeyword && hasCategory) {
            return dishRepository.findByNameContainingIgnoreCaseAndCategory_CategoryId(keyword.trim(), categoryId, pageable);
        } else if (hasKeyword) {
            return dishRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
        } else if (hasCategory) {
            return dishRepository.findByCategory_CategoryId(categoryId, pageable);
        } else {
            return dishRepository.findAll(pageable);
        }
    }

    public List<MenuItem> getAllActiveItems() {
        return dishRepository.findByIsAvailableTrue();
    }

    public List<MenuItem> getAllMenuItems() { return dishRepository.findAll(); }

    public MenuItem getById(Integer id) { return dishRepository.findById(id).orElse(new MenuItem()); }

    public void save(MenuItem dish) { dishRepository.save(dish); }

    public void delete(Integer id) { dishRepository.deleteById(id); }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}