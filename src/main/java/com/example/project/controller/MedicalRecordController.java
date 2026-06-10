package com.example.project.controller;

import com.example.project.dto.ApiResponse;
import com.example.project.dto.MedicalRecordResponse;
import com.example.project.model.MedicalRecord;
import com.example.project.service.MedicalRecordService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/doctor/records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @Autowired
    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    // Only doctors can upload medical records for a patient
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> upload(@RequestParam("file") @NotNull MultipartFile file,
                                    @RequestParam("diagnosis") String diagnosis,
                                    @RequestParam("ownerId") Long ownerId) throws IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new RuntimeException("Unauthenticated");
        String doctorUsername = auth.getName();

        MedicalRecord rec = medicalRecordService.uploadRecord(file, diagnosis, ownerId, doctorUsername);
        MedicalRecordResponse resp = new MedicalRecordResponse(rec.getId(), rec.getFileUrl(), rec.getDiagnosis(), rec.getCreatedAt(), rec.getOwner() != null ? rec.getOwner().getId() : null);
        return ResponseEntity.ok(ApiResponse.success("Created successfully", resp));
    }
}




