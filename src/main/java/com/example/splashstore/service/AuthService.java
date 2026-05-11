package com.example.splashstore.service;

import com.example.splashstore.dto.AuthRequest;
import com.example.splashstore.dto.AuthResponse;
import com.example.splashstore.dto.SignupRequest;
import com.example.splashstore.dto.UserResponse;
import com.example.splashstore.model.AppUser;
import com.example.splashstore.repository.AppUserRepository;
import com.example.splashstore.security.AppUserDetails;
import com.example.splashstore.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@Service
public class AuthService {


    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
        if (appUserRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already in use");
        }

        AppUser user = new AppUser();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPhone(request.getPhone());
        user.setFullname(request.getFullname());
        user.setRole(request.getRole());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(new Date());

        AppUser savedUser = appUserRepository.save(user);
        String token = jwtService.generateToken(new AppUserDetails(savedUser));
        return new AuthResponse(token, savedUser.getId(), savedUser.getEmail(), savedUser.getUsername(), savedUser.getRole());
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        AppUser user = userDetails.getUser();
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getUsername(), user.getRole());
    }
    public UserResponse getCurrentUser(AppUserDetails userDetails) {
        AppUser user = userDetails.getUser();
        return new UserResponse(user.getId(), user.getEmail(), user.getUsername(), user.getRole(), user.getFullname(), user.getPhone());
    }

    public UserResponse getUserById(Long userId) {
        return appUserRepository.findById(userId)
                .map(user -> new UserResponse(user.getId(), user.getEmail(), user.getUsername(), user.getRole(), user.getFullname(), user.getPhone()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}

