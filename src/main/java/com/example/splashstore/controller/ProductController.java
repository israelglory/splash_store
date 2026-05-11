package com.example.splashstore.controller;


import com.example.splashstore.dto.ProductRequest;
import com.example.splashstore.dto.ProductResponse;
import com.example.splashstore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping
    public ProductResponse addProduct(@RequestBody ProductRequest request) {
        return productService.addProduct(request);
    }

    @DeleteMapping("/{id}")
    public ProductResponse deleteProductById(@PathVariable Long id) {
        return productService.deleteProduct(id);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProductById(@PathVariable Long id, @RequestBody ProductRequest request) {
        return productService.updateProduct(id, request);
    }

}
