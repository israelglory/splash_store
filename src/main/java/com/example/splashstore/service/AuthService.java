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
import com.example.splashstore.model.RefreshToken;
import com.example.splashstore.model.EmailOtp;
import com.example.splashstore.repository.RefreshTokenRepository;
import com.example.splashstore.repository.EmailOtpRepository;
import com.example.splashstore.service.EmailService;

import java.util.UUID;
import java.util.Calendar;
import java.util.Date;
import java.security.SecureRandom;

@Service
public class AuthService {


    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailOtpRepository emailOtpRepository;
    private final EmailService emailService;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
            ,RefreshTokenRepository refreshTokenRepository
            ,EmailOtpRepository emailOtpRepository
            ,EmailService emailService
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.emailOtpRepository = emailOtpRepository;
        this.emailService = emailService;
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
        RefreshToken refreshToken = createRefreshToken(savedUser);
        return new AuthResponse(token, refreshToken.getToken(), savedUser.getId(), savedUser.getEmail(), savedUser.getUsername(), savedUser.getRole());
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        AppUser user = userDetails.getUser();
        String token = jwtService.generateToken(userDetails);
        // create refresh token
        RefreshToken refreshToken = createRefreshToken(user);
        return new AuthResponse(token, refreshToken.getToken(), user.getId(), user.getEmail(), user.getUsername(), user.getRole());
    }

    private RefreshToken createRefreshToken(AppUser user) {
        // remove existing tokens for user to keep single device refresh tokens simple
        refreshTokenRepository.deleteByUser(user);
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(UUID.randomUUID().toString());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7); // 7 days
        rt.setExpiryDate(cal.getTime());
        return refreshTokenRepository.save(rt);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        RefreshToken rt = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        if (rt.getExpiryDate().before(new Date())) {
            refreshTokenRepository.delete(rt);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        AppUser user = rt.getUser();
        AppUserDetails userDetails = new AppUserDetails(user);
        String newAccessToken = jwtService.generateToken(userDetails);
        // rotate refresh token
        RefreshToken newRt = createRefreshToken(user);
        return new AuthResponse(newAccessToken, newRt.getToken(), user.getId(), user.getEmail(), user.getUsername(), user.getRole());
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // generate 6-digit OTP
        SecureRandom rnd = new SecureRandom();
        int otpInt = 100000 + rnd.nextInt(900000);
        String code = String.valueOf(otpInt);

        EmailOtp otp = new EmailOtp();
        otp.setUser(user);
        otp.setCode(code);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 10); // 10 minutes
        otp.setExpiryDate(cal.getTime());
        emailOtpRepository.save(otp);

        emailService.sendOtp(user.getEmail(), code);
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        EmailOtp otp = emailOtpRepository.findByUserAndCodeAndUsedFalse(user, code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or used OTP"));

        if (otp.getExpiryDate().before(new Date())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        appUserRepository.save(user);

        otp.setUsed(true);
        emailOtpRepository.save(otp);
        // revoke existing refresh tokens
        refreshTokenRepository.deleteByUser(user);
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

