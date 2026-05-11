package com.example.splashstore.repository;

import com.example.splashstore.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	Optional<Category> findByCategoryNameIgnoreCase(String categoryName);

	boolean existsByCategoryNameIgnoreCase(String categoryName);
}
