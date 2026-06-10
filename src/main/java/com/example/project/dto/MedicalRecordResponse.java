package com.example.project.dto;

import java.time.LocalDateTime;

public record MedicalRecordResponse(Long id, String fileUrl, String diagnosis, LocalDateTime createdAt, Long ownerId) {
}

