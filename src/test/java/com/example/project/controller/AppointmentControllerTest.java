package com.example.project.controller;

import com.example.project.dto.AppointmentRequest;
import com.example.project.dto.AppointmentResponse;
import com.example.project.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AppointmentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AppointmentService appointmentService;

    @Test
    @WithMockUser(roles = "PATIENT", username = "p1")
    void createAppointment() throws Exception {
        AppointmentResponse resp = new AppointmentResponse(1L, LocalDate.of(2026,6,20), "09:00-09:30", "PENDING", "symp", null, 2L, null);
        when(appointmentService.createAppointment(any(AppointmentRequest.class), any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/patient/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\":\"2026-06-20\",\"timeSlot\":\"09:00-09:30\",\"symptomDescription\":\"symp\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PATIENT", username = "p1")
    void myAppointments() throws Exception {
        AppointmentResponse resp = new AppointmentResponse(2L, LocalDate.of(2026,6,20), "09:00-09:30", "PENDING", "s", null, 3L, null);
        when(appointmentService.getAppointmentsForPatient("p1")).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/v1/patient/appointments/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "DOCTOR", username = "d1")
    void changeStatus() throws Exception {
        AppointmentResponse resp = new AppointmentResponse(3L, LocalDate.of(2026,6,20), "09:00-09:30", "APPROVED", "s", null, 4L, null);
        when(appointmentService.updateStatus(3L, "APPROVED", "d1")).thenReturn(resp);

        mockMvc.perform(put("/api/v1/patient/appointments/3/status?status=APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }
}


