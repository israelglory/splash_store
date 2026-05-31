package com.example.splashstore.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
    // For now, this is a simple placeholder. In production, wire an SMTP client or third-party provider.
    public void sendOtp(String toEmail, String code) {
        System.out.println("[EmailService] Sending OTP to " + toEmail + ": " + code);
    }
}

