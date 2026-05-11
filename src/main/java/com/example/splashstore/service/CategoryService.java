package com.example.splashstore.service;

import com.example.splashstore.dto.CategoryRequest;
import com.example.splashstore.dto.CategoryResponse;
import com.example.splashstore.model.AppUser;
import com.example.splashstore.model.Category;
import com.example.splashstore.repository.AppUserRepository;
import com.example.splashstore.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final AppUserRepository userRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, AppUserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    private AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    private void requireAdmin() {
        AppUser currentUser = getCurrentUser();
        if (currentUser.getRole() == null || !currentUser.getRole().equalsIgnoreCase("admin")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        return mapToResponse(category);
    }

    @Transactional
    public CategoryResponse addCategory(CategoryRequest request) {
        requireAdmin();

        String categoryName = request.getCategoryName().trim();
        if (categoryRepository.existsByCategoryNameIgnoreCase(categoryName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
        }

        Category category = new Category();
        category.setCategoryName(categoryName);
        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        requireAdmin();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        String categoryName = request.getCategoryName().trim();
        categoryRepository.findByCategoryNameIgnoreCase(categoryName)
                .filter(existing -> !existing.getCategoryId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
                });

        category.setCategoryName(categoryName);
        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse deleteCategoryById(Long id) {
        requireAdmin();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        CategoryResponse response = mapToResponse(category);
        categoryRepository.delete(category);
        return response;
    }

    private CategoryResponse mapToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setCategoryId(category.getCategoryId());
        response.setCategoryName(category.getCategoryName());
        return response;
    }
}
