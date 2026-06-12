package com.example.project.dto;

import com.example.project.model.RoleEnum;
import com.example.project.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record UserResponse(Long id,
                           @NotBlank(message = "Username không được trống")
                           @Pattern(
                                   regexp = "^[a-zA-Z0-9_]+$",
                                   message = "Username chỉ được chứa các chữ cái, số và dấu gạch dưới"
                           )
                           String username,
                           RoleEnum role,
                           Boolean isActive,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
