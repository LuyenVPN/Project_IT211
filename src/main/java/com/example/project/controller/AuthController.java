package com.example.project.controller;

import com.example.project.dto.ApiResponse;
import com.example.project.service.AuthService;
import com.example.project.service.UserService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", resp));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request) {
        var user = authService.registerPatient(request.username(), request.password());
        return ResponseEntity.status(201).body(ApiResponse.success("Đăng ký tài khoản thành công", user));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest request) {
        var resp = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("Làm mới token thành công", resp));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.accessToken(), request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new RuntimeException("Người dùng chưa đăng nhập");
        userService.changePassword(auth.getName(), request.oldPassword(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.resetPassword(request.username(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công"));
    }

    public record AuthRequest(

            @NotBlank(message = "Tên đăng nhập không được để trống")
            String username,

            @NotBlank(message = "Mật khẩu không được để trống")
            String password

    ) {}

    public record RefreshRequest(

            @NotBlank(message = "Refresh token không được để trống")
            String refreshToken

    ) {}

    public record LogoutRequest(

            @NotBlank(message = "Access token không được để trống")
            String accessToken,

            @NotBlank(message = "Refresh token không được để trống")
            String refreshToken

    ) {}

    public record ChangePasswordRequest(

            @NotBlank(message = "Mật khẩu cũ không được để trống")
            String oldPassword,

            @NotBlank(message = "Mật khẩu mới không được để trống")
            @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
            String newPassword

    ) {}

    public record ForgotPasswordRequest(

            @NotBlank(message = "Tên đăng nhập không được để trống")
            String username,

            @NotBlank(message = "Mật khẩu mới không được để trống")
            @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
            String newPassword

    ) {}
}

