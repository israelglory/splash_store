package com.example.splashstore.controller;

import com.example.splashstore.dto.AuthRequest;
import com.example.splashstore.dto.AuthResponse;
import com.example.splashstore.dto.SignupRequest;
import com.example.splashstore.dto.UserResponse;
import com.example.splashstore.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Signup, login and user lookup endpoints")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @Operation(summary = "Sign up a new user")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive a JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

//    @GetMapping("/{id}")
//    @Operation(summary = "Get a user by ID")
//    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
//        return ResponseEntity.ok(authService.getUserById(id));
//    }
}

