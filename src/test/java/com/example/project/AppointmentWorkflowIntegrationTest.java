package com.example.project;

import com.example.project.model.RoleEnum;
import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
public class AppointmentWorkflowIntegrationTest {

    @Autowired WebApplicationContext wac;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void initMockMvc() {
        this.mockMvc = webAppContextSetup(wac).build();
    }

    @BeforeEach
    void setupDoctor() {
        userRepository.findByUsername("doctor1").orElseGet(() ->
                {
                    User user = new User();
                    user.setUsername("doctor1");
                    user.setPasswordHash(passwordEncoder.encode("Doctor123"));
                    user.setRole(RoleEnum.DOCTOR);
                    user.setIsActive(true);
                    return userRepository.save(user);
                });
    }

    @Test
    void patientCanCreateAppointmentAndDoctorCanApproveIt() throws Exception {
        // register patient
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"patient3\"," +
                                "\"password\":\"Pa$$w0rd\"}"))
                .andExpect(status().isOk());

        String patientLoginBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"patient3\"," +
                                "\"password\":\"Pa$$w0rd\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String patientToken = extractJsonValue(patientLoginBody, "accessToken");

        String createResp = mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + patientToken)
                        .content("{" +
                                "\"date\":\"2026-06-20\"," +
                                "\"timeSlot\":\"09:00-09:30\"," +
                                "\"symptomDescription\":\"Dau dau\"," +
                                "\"reason\":\"Kham tong quat\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(createResp).contains("\"status\":\"PENDING\"");
        long appointmentId = Long.parseLong(extractJsonValue(createResp, "id"));

        String doctorLoginBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"doctor1\"," +
                                "\"password\":\"Doctor123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String doctorToken = extractJsonValue(doctorLoginBody, "accessToken");

        String approveResp = mockMvc.perform(put("/api/appointments/" + appointmentId + "/status")
                        .param("status", "CONFIRMED")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(approveResp).contains("\"status\":\"CONFIRMED\"");
    }

    @Test
    void logoutShouldRevokeAccessToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"patient4\"," +
                                "\"password\":\"Pa$$w0rd\"}"))
                .andExpect(status().isOk());

        String loginBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"patient4\"," +
                                "\"password\":\"Pa$$w0rd\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String accessToken = extractJsonValue(loginBody, "accessToken");
        String refreshToken = extractJsonValue(loginBody, "refreshToken");

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"accessToken\":\"" + accessToken + "\"," +
                                "\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/appointments/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());
    }

    private static String extractJsonValue(String json, String key) {
        Matcher matcher = Pattern.compile("\\\"" + key + "\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }
}






