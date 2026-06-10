package com.example.project.dto;

import java.time.LocalDate;

public record AppointmentRequest(LocalDate date, String timeSlot, String symptomDescription, String reason) {}

