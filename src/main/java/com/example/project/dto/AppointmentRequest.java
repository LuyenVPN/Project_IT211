package com.example.project.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AppointmentRequest(

        @NotNull(message = "Bắt buộc nhập ngày")
        @FutureOrPresent(message = "Phải là ngày hôm nay hoặc tương lai")
        LocalDate date,

        @NotBlank(message = "Bắt buộc nhập thời gian")
        String timeSlot,

        @NotBlank(message = "Mô tả triệu chứng bắt buộc")
        String symptomDescription,

        @NotBlank(message = "Lý do khám bắt buộc")
        String reason,

        @NotNull(message = "Doctor không thấy")
        Long doctorId

) {}