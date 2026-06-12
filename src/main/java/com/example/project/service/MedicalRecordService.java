package com.example.project.service;

import com.cloudinary.Cloudinary;
import com.example.project.dto.MedicalRecordResponse;
import com.example.project.model.MedicalRecord;
import com.example.project.model.RoleEnum;
import com.example.project.model.User;
import com.example.project.repository.MedicalRecordRepository;
import com.example.project.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;

    @Autowired
    public MedicalRecordService(
            MedicalRecordRepository medicalRecordRepository,
            UserRepository userRepository,
            Cloudinary cloudinary
    ) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.userRepository = userRepository;
        this.cloudinary = cloudinary;
    }

    @Transactional
    public MedicalRecord uploadRecord(
            MultipartFile file,
            String diagnosis,
            Long ownerId,
            String doctorUsername
    ) throws IOException {
        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException(
                    "Tên tập tin không hợp lệ."
            );
        }

        String lowerName = fileName.toLowerCase();

        if (!lowerName.endsWith(".jpg")
                && !lowerName.endsWith(".jpeg")
                && !lowerName.endsWith(".png")
                && !lowerName.endsWith(".pdf")) {

            throw new IllegalArgumentException(
                    "Chỉ cho phép tải lên tập tin JPG, JPEG, PNG hoặc PDF."
            );
        }

        // 2. Kiểm tra sự tồn tại của người dùng
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy thông tin bệnh nhân."
                        )
                );

        if (owner.getRole() != RoleEnum.PATIENT) {

            throw new IllegalArgumentException(
                    "Chỉ được tạo hồ sơ bệnh án cho bệnh nhân."
            );
        }
        // 3. Kiểm tra nội dung và dung lượng tệp
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn tập tin để tải lên.");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Kích thước tập tin vượt quá giới hạn cho phép (Tối đa 10MB).");
        }

        // 4. Tiến hành tải lên Cloudinary
        Map<?, ?> uploadResult =
                cloudinary.uploader().upload(
                        file.getBytes(),
                        java.util.Map.of(
                                "folder",
                                "hospital-medical-records"
                        )
                );

        String secureUrl =
                uploadResult.get("secure_url").toString();

        // 5. Lưu thông tin vào Cơ sở dữ liệu
        MedicalRecord rec = new MedicalRecord();

        rec.setFileUrl(secureUrl);
        rec.setDiagnosis(diagnosis);
        rec.setOwner(owner);

        return medicalRecordRepository.save(rec);

    }

    public List<MedicalRecordResponse>
    getMedicalRecordsForPatient(
            String username) {

        User patient = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy bệnh nhân"
                        ));

        return medicalRecordRepository
                .findAllByOwnerIdOrderByCreatedAtDesc(
                        patient.getId()
                )
                .stream()
                .map(record ->
                        new MedicalRecordResponse(
                                record.getId(),
                                record.getFileUrl(),
                                record.getDiagnosis(),
                                record.getCreatedAt(),
                                record.getOwner().getId()
                        )
                )
                .toList();
    }
}