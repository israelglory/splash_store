package com.example.splashstore.repository;

import com.example.splashstore.model.OrderModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderModel, Long> {
    List<OrderModel> findByCreatedById(Long createdById);
}

