package com.example.project.dto;

import java.time.LocalDate;

public record AppointmentResponse(Long id, LocalDate date, String timeSlot, String status, String symptomDescription, String reason, Long patientId, Long doctorId) {
}

