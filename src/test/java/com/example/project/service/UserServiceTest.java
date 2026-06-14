package com.example.project.service;

import com.example.project.dto.UserResponse;
import com.example.project.model.RoleEnum;
import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    /**
     * Chuẩn bị dữ liệu mẫu trước mỗi test.
     */
    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPasswordHash("encoded");
        user.setRole(RoleEnum.ADMIN);
        user.setIsActive(true);
    }

    /**
     * Kiểm tra lấy user theo id thành công.
     */
    @Test
    void getById_success() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserResponse response =
                userService.getById(1L);

        assertNotNull(response);

        verify(userRepository)
                .findById(1L);
    }

    /**
     * Kiểm tra trường hợp user không tồn tại.
     */
    @Test
    void getById_notFound() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> userService.getById(1L)
        );
    }

    /**
     * Kiểm tra xóa user thành công.
     */
    @Test
    void delete_success() {

        when(userRepository.existsById(1L))
                .thenReturn(true);

        userService.delete(1L);

        verify(userRepository)
                .existsById(1L);

        verify(userRepository)
                .deleteById(1L);
    }

    /**
     * Kiểm tra tạo user mới thành công.
     * Mục tiêu:
     * - Username chưa tồn tại.
     * - Password được encode.
     * - User được lưu xuống database.
     */
    @Test
    void create_success() {

        UserService.UserRequest request =
                new UserService.UserRequest(
                        "admin2",
                        "123456",
                        RoleEnum.ADMIN,
                        true
                );

        when(userRepository.findByUsername("admin2"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("123456"))
                .thenReturn("encoded");

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        UserResponse response =
                userService.create(request);

        assertNotNull(response);

        verify(passwordEncoder)
                .encode("123456");

        verify(userRepository)
                .save(any(User.class));
    }

    /**
     * Kiểm tra đổi mật khẩu thành công.
     * Mục tiêu:
     * - Mật khẩu cũ đúng.
     * - Mật khẩu mới được encode.
     * - User được cập nhật.
     */
    @Test
    void changePassword_success() {

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "oldpass",
                "encoded"))
                .thenReturn(true);

        when(passwordEncoder.encode("newpass"))
                .thenReturn("newEncoded");

        userService.changePassword(
                "admin",
                "oldpass",
                "newpass"
        );

        verify(userRepository)
                .save(user);
    }
}