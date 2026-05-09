package com.example.splashstore.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {
    @Test
    void generateAndValidateToken() {
        JwtService jwtService = new JwtService("01234567890123456789012345678901", 60000);
        UserDetails user = User.withUsername("test@example.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        String token = jwtService.generateToken(user);

        assertEquals("test@example.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, user));
    }
}

