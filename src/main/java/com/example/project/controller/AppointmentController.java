package com.example.project.controller;

import com.example.project.dto.ApiResponse;
import com.example.project.dto.AppointmentRequest;
import com.example.project.dto.AppointmentResponse;
import com.example.project.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
// expose both v1/patient path and legacy /api/appointments used by integration tests
@RequestMapping({"/api/v1/patient/appointments", "/api/appointments"})
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public AppointmentResponse create(@Valid @RequestBody AppointmentRequest req) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new RuntimeException("Unauthenticated");
        String username = auth.getName();
        return appointmentService.createAppointment(req, username);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<List<AppointmentResponse>> myAppointments() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new RuntimeException("Unauthenticated");
        String username = auth.getName();
        return ApiResponse.success("Retrieved successfully", appointmentService.getAppointmentsForPatient(username));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public ApiResponse<AppointmentResponse> changeStatus(@PathVariable("id") Long id, @RequestParam("status") String status) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new RuntimeException("Unauthenticated");
        String username = auth.getName();
        return ApiResponse.success("Updated successfully", appointmentService.updateStatus(id, status, username));
    }
}





