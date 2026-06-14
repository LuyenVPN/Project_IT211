package com.example.project.controller;

import com.example.project.model.User;
import com.example.project.service.AuthService;
import com.example.project.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    /**
     * Kiểm tra đăng nhập thành công.
     * Mục tiêu:
     * - Mock AuthService trả về access token và refresh token.
     * - Kiểm tra HTTP status = 200.
     * - Kiểm tra AuthService.login được gọi đúng tham số.
     */
    @Test
    void login_success() {

        AuthController.AuthRequest request =
                new AuthController.AuthRequest(
                        "admin",
                        "123456"
                );

        when(authService.login(anyString(), anyString()))
                .thenReturn(
                        new AuthService.AuthResponse(
                                "access-token",
                                "refresh-token"
                        )
                );

        ResponseEntity<?> response =
                authController.login(request);

        assertEquals(200, response.getStatusCode().value());

        verify(authService)
                .login("admin", "123456");
    }

    /**
     * Kiểm tra đăng ký tài khoản thành công.
     * Mục tiêu:
     * - Mock tạo user mới.
     * - Kiểm tra HTTP status = 201.
     * - Kiểm tra AuthService.registerPatient được gọi.
     */
    @Test
    void register_success() {

        AuthController.AuthRequest request =
                new AuthController.AuthRequest(
                        "user1",
                        "123456"
                );

        User user = new User();
        user.setUsername("user1");

        when(authService.registerPatient(anyString(), anyString()))
                .thenReturn(user);

        ResponseEntity<?> response =
                authController.register(request);

        assertEquals(201, response.getStatusCode().value());

        verify(authService)
                .registerPatient("user1", "123456");
    }

    /**
     * Kiểm tra refresh token thành công.
     * Mục tiêu:
     * - Mock tạo access token mới.
     * - Kiểm tra HTTP status = 200.
     * - Kiểm tra AuthService.refresh được gọi.
     */
    @Test
    void refresh_success() {

        AuthController.RefreshRequest request =
                new AuthController.RefreshRequest(
                        "refresh-token"
                );

        when(authService.refresh(anyString()))
                .thenReturn(
                        new AuthService.AuthResponse(
                                "new-access-token",
                                "new-refresh-token"
                        )
                );

        ResponseEntity<?> response =
                authController.refresh(request);

        assertEquals(200, response.getStatusCode().value());

        verify(authService)
                .refresh("refresh-token");
    }

    /**
     * Kiểm tra đăng xuất thành công.
     * Mục tiêu:
     * - Gọi logout controller.
     * - Kiểm tra AuthService.logout được gọi.
     */
    @Test
    void logout_success() {

        AuthController.LogoutRequest request =
                new AuthController.LogoutRequest(
                        "access",
                        "refresh"
                );

        ResponseEntity<?> response =
                authController.logout(request);

        assertEquals(200, response.getStatusCode().value());

        verify(authService)
                .logout("access", "refresh");
    }

    /**
     * Kiểm tra chức năng quên mật khẩu.
     * Mục tiêu:
     * - Gọi resetPassword.
     * - Kiểm tra HTTP status = 200.
     */
    @Test
    void forgotPassword_success() {

        AuthController.ForgotPasswordRequest request =
                new AuthController.ForgotPasswordRequest(
                        "admin",
                        "123456"
                );

        ResponseEntity<?> response =
                authController.forgotPassword(request);

        assertEquals(200, response.getStatusCode().value());

        verify(userService)
                .resetPassword("admin", "123456");
    }
}