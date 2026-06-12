package com.example.project.controller;

import com.example.project.dto.ApiResponse;
import com.example.project.dto.MedicalRecordResponse;
import com.example.project.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patient/records")
public class PatientMedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @Autowired
    public PatientMedicalRecordController(
            MedicalRecordService medicalRecordService
    ) {
        this.medicalRecordService =
                medicalRecordService;
    }

    @GetMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<List<MedicalRecordResponse>>
    myRecords() {

        String username =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        return ApiResponse.success(
                "Lấy hồ sơ bệnh án thành công",
                medicalRecordService
                        .getMedicalRecordsForPatient(
                                username
                        )
        );
    }
}