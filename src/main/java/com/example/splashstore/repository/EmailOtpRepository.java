package com.example.splashstore.repository;

import com.example.splashstore.model.EmailOtp;
import com.example.splashstore.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {
    Optional<EmailOtp> findByUserAndCodeAndUsedFalse(AppUser user, String code);
}

