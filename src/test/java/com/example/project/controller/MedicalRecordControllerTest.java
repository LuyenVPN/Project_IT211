package com.example.project.controller;

import com.example.project.model.MedicalRecord;
import com.example.project.service.MedicalRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MedicalRecordControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MedicalRecordService medicalRecordService;

    @Test
    @WithMockUser(roles = "DOCTOR", username = "doc1")
    void uploadRecord() throws Exception {
        MedicalRecord rec = new MedicalRecord();
        rec.setId(12L);
        rec.setFileUrl("/tmp/f.pdf");
        rec.setDiagnosis("diag");
        rec.setCreatedAt(LocalDateTime.now());

        when(medicalRecordService.uploadRecord(any(), any(), any(), any())).thenReturn(rec);

        MockMultipartFile file = new MockMultipartFile("file", "f.pdf", "application/pdf", "bytes".getBytes());

        mockMvc.perform(multipart("/api/v1/doctor/records").file(file)
                .param("diagnosis", "diag")
                .param("ownerId", "1")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}


