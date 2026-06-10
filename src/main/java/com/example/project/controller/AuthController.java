package com.example.project.controller;

import com.example.project.dto.ApiResponse;
import com.example.project.service.AuthService;
import com.example.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
// Keep compatibility with existing tests and older clients that hit /api/auth
@RequestMapping({"/api/v1/auth", "/api/auth"})
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        var resp = authService.login(request.username(), request.password());
        return ResponseEntity.ok(ApiResponse.success("Logged in successfully", resp));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request) {
        var user = authService.registerPatient(request.username(), request.password());
        return ResponseEntity.status(201).body(ApiResponse.success("Created successfully", user));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest request) {
        var resp = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", resp));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.accessToken(), request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new RuntimeException("Unauthenticated");
        userService.changePassword(auth.getName(), request.oldPassword(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.resetPassword(request.username(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
    }

    public record AuthRequest(String username, String password) {}
    public record RefreshRequest(String refreshToken) {}
    public record LogoutRequest(String accessToken, String refreshToken) {}
    public record ChangePasswordRequest(String oldPassword, String newPassword) {}
    public record ForgotPasswordRequest(String username, String newPassword) {}
}

