package com.example.splashstore.controller;


import com.example.splashstore.dto.CategoryRequest;
import com.example.splashstore.model.Category;
import com.example.splashstore.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/category")
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @Operation(summary = "Create a category")
    public ResponseEntity<Category> addCategory(@RequestBody Category category) {
        return ResponseEntity.ok(categoryService.addCategory(category));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category by ID")
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategoryById(id);
    }


}
