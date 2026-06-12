package com.example.project.controller;

import com.example.project.dto.ApiResponse;
import com.example.project.dto.AppointmentResponse;
import com.example.project.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctor/appointments")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorAppointmentController {

    private final AppointmentService appointmentService;

    @Autowired
    public DoctorAppointmentController(
            AppointmentService appointmentService
    ) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public ApiResponse<List<AppointmentResponse>> getMyAppointments() {

        String username =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        return ApiResponse.success(
                "Lấy danh sách cuộc hẹn thành công",
                appointmentService.getAppointmentsForDoctor(username)
        );
    }
}