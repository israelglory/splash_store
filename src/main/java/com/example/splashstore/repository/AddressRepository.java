package com.example.splashstore.repository;

import com.example.splashstore.model.AddressModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<AddressModel, Long> {
    List<AddressModel> findByUserId(Long userId);

    Optional<AddressModel> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}

