package com.example.splashstore.service;

import com.example.splashstore.model.Category;
import com.example.splashstore.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void deleteCategoryById(Long id) {
        categoryRepository.deleteById(id);

    }
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
    }
    public Category addCategory(Category category) {
        return categoryRepository.save(category);
    }
}
