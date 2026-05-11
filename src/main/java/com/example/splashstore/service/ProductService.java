package com.example.splashstore.service;

import com.example.splashstore.dto.ProductRequest;
import com.example.splashstore.dto.ProductResponse;
import com.example.splashstore.dto.UserResponse;
import com.example.splashstore.model.AppUser;
import com.example.splashstore.model.Category;
import com.example.splashstore.model.ProductModel;
import com.example.splashstore.repository.AppUserRepository;
import com.example.splashstore.repository.CategoryRepository;
import com.example.splashstore.repository.ProductRepository;
import org.apache.juli.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {


    private final ProductRepository productRepository;
    private final AppUserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ProductService(ProductRepository productRepository,  AppUserRepository userRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
     }

    public AppUser getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    public ProductResponse mapToResponse(ProductModel product) {

        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setImage(product.getImage());
        response.setStatus(product.getStatus());

        // Category
        if(product.getCategory() != null) {
            response.setCategory(product.getCategory().getCategoryName());
        }

        // User
        if(product.getCreatedBy() != null) {
            AppUser user = product.getCreatedBy();
            UserResponse userResponse = new UserResponse(
                    user.getId(), user.getEmail(), user.getUsername(), user.getRole(), user.getFullname(), user.getPhone()
            );
            response.setCreatedBy(userResponse);
        }

        response.setCreatedAt(product.getCreatedAt());

        return response;
    }


    public ProductResponse  addProduct(ProductRequest productRequest) {
         Category category = categoryRepository
                 .findById(productRequest.getCategoryId())
                 .orElseThrow(() -> new RuntimeException("Category not found"));

         ProductModel product = new ProductModel();
         product.setName(productRequest.getName());
         product.setPrice(productRequest.getPrice());
         product.setDescription(productRequest.getDescription());
         product.setImage(productRequest.getImage());
         product.setStatus(productRequest.getStatus());
         product.setCategory(category);
         AppUser currentUser = getCurrentUser();
         product.setCreatedBy(currentUser);
         if(currentUser.getRole().equals("admin")){
             product = productRepository.save(product);
         }else {
             throw new ResponseStatusException(HttpStatus.CONFLICT, "ONLY ADMIN CAN ADD PRODUCT");
         }


         return mapToResponse(product);
     }

     public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setImage(productRequest.getImage());
         Category category = categoryRepository
                 .findById(productRequest.getCategoryId())
                 .orElseThrow(() -> new RuntimeException("Category not found"));
         product.setCategory(category);
        product.setStatus(productRequest.getStatus());
         AppUser currentUser = getCurrentUser();
         if(currentUser.getRole().equals("admin")){
             product = productRepository.save(product);
         }else {
             throw new ResponseStatusException(HttpStatus.CONFLICT, "ONLY ADMIN CAN UPDATE PRODUCT");
         }

        return mapToResponse(product);
     }

     public ProductResponse deleteProduct(Long id) {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
         AppUser currentUser = getCurrentUser();
         if(currentUser.getRole().equals("admin")){
             ProductResponse response = mapToResponse(product);
             productRepository.delete(product);
             return response;
         }else {
             throw new ResponseStatusException(HttpStatus.CONFLICT, "ONLY ADMIN CAN DELETE PRODUCT");
         }
     }

     public ProductResponse getProductById(Long id) {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToResponse(product);
     }

     public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
     }

}