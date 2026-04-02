package com.oose.restaurant_mis.service;

import com.oose.restaurant_mis.entity.Category;
import com.oose.restaurant_mis.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public Page<Category> getCategoriesPage(int page, int size) {
        return categoryRepository.findAll(PageRequest.of(page, size));
    }
    public Page<Category> searchCategories(String keyword, int page, int size) {
        return categoryRepository.findByNameContainingIgnoreCase(keyword, PageRequest.of(page, size));
    }

    public Category getById(Integer id) {
        return categoryRepository.findById(id).orElse(new Category());
    }

    public void save(Category category) {
        categoryRepository.save(category);
    }

    public void delete(Integer id) {
        categoryRepository.deleteById(id);
    }
    // Thêm vào CategoryService.java
    public long countTotalCategories() {
        return categoryRepository.count();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}