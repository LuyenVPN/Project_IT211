package com.example.project.service;

import com.example.project.model.MedicalRecord;
import com.example.project.model.User;
import com.example.project.repository.MedicalRecordRepository;
import com.example.project.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Files;
// ...existing code...
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MedicalRecordServiceTest {

    @Mock
    MedicalRecordRepository medicalRecordRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    MedicalRecordService medicalRecordService;

    @AfterEach
    void cleanup() {
        File uploads = new File("uploads/medical");
        if (uploads.exists() && uploads.isDirectory()) {
            File[] files = uploads.listFiles();
            if (files != null) {
                for (File f : files) {
                    try { f.delete(); } catch (Exception ignored) {}
                }
            }
            try { uploads.delete(); } catch (Exception ignored) {}
        }
    }

    @Test
    void uploadRecordSavesFileAndDb() throws Exception {
        User owner = new User();
        owner.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());

        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenAnswer(invocation -> {
            MedicalRecord saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        var rec = medicalRecordService.uploadRecord(file, "diag", 2L, "doc1");
        assertNotNull(rec);
        assertEquals(99L, rec.getId());
        // file saved to uploads
        assertTrue(Files.exists(new File(rec.getFileUrl()).toPath()));
    }
}


