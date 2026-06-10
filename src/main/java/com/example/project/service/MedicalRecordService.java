package com.example.project.service;

import com.example.project.model.MedicalRecord;
import com.example.project.model.User;
import com.example.project.repository.MedicalRecordRepository;
import com.example.project.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final UserRepository userRepository;

    @Autowired
    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository, UserRepository userRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MedicalRecord uploadRecord(MultipartFile file, String diagnosis, Long ownerId, String doctorUsername) throws IOException {
        // Only doctors are allowed by controller/auth; here we simply perform file save and DB entry
        User owner = userRepository.findById(ownerId).orElseThrow(() -> new RuntimeException("Owner not found"));

        // Save file to ./uploads/medical/ with a generated name
        Path uploadRoot = Path.of("uploads", "medical");
        Files.createDirectories(uploadRoot);
        String original = file.getOriginalFilename();
        String fname = System.currentTimeMillis() + "_" + (original != null ? original.replaceAll("[^a-zA-Z0-9_.-]", "_") : "file.bin");
        Path target = uploadRoot.resolve(fname);
        try (var in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        MedicalRecord rec = new MedicalRecord();
        rec.setFileUrl(target.toAbsolutePath().toString());
        rec.setDiagnosis(diagnosis);
        rec.setCreatedAt(LocalDateTime.now());
        rec.setOwner(owner);

        return medicalRecordRepository.save(rec);
    }
}


