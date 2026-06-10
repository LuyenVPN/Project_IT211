package com.example.project.dto;

import com.example.project.model.RoleEnum;
import com.example.project.model.User;

import java.time.LocalDateTime;

public record UserResponse(Long id,
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
