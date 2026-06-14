package com.example.project.service;

import com.example.project.dto.UserResponse;
import com.example.project.model.RoleEnum;
import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UserResponse> search(String keyword, Pageable pageable) {
        Page<User> users = keyword == null || keyword.isBlank()
                ? userRepository.findAll(pageable)
                : userRepository.findByUsernameContainingIgnoreCase(keyword.trim(), pageable);
        return users.map(UserResponse::from);
    }

    public UserResponse getById(Long id) {
        return UserResponse.from(findById(id));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username đã tôn tại");
        }

        User user = new User();
        applyEditableFields(user, request, true);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = findById(id);
        if (!request.username().matches("^\\S+$")) {
            throw new IllegalArgumentException(
                    "Username không được để trống hoặc chứa khoảng trắng");
        }
        if (!user.getUsername().equals(request.username())
                && userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }
//        "^\\S{6,}$"
//        if (!request.username().matches("^[a-zA-Z0-9._-]{3,}$")) {
//            throw new IllegalArgumentException("Username phải có ít nhất 3 ký tự và chỉ chứa chữ cái, số, dấu chấm, gạch dưới hoặc gạch ngang");
//        }

        if (request.password() != null) {
            if (request.password().isBlank()) {
                throw new IllegalArgumentException("Password không được để trống");
            }

            if (!request.password().matches("^\\S+$")) {
                throw new IllegalArgumentException("Password không được chứa khoảng trắng");
            }
        }

        applyEditableFields(user, request, false);

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        User user = findById(id);

        if (user.getRole() == RoleEnum.ADMIN) {
            throw new IllegalArgumentException("Không được phép xóa tài khoản ADMIN");
        }

        userRepository.delete(user);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void resetPassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User không thấy"));
    }

    private void applyEditableFields(User user, UserRequest request, boolean requirePassword) {
        if (request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException("Username không được trống");
        }
        if (request.role() == null) {
            throw new IllegalArgumentException("Role không được trống");
        }
        if (requirePassword && (request.password() == null || request.password().isBlank())) {
            throw new IllegalArgumentException("Password không được trống");
        }

        user.setUsername(request.username().trim());
        user.setRole(request.role());
        user.setIsActive(request.isActive() == null || request.isActive());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
    }

    public record UserRequest(String username, String password, RoleEnum role, Boolean isActive) {}
}
