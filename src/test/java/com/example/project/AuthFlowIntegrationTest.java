package com.example.project;

import com.example.project.model.RoleEnum;
import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
public class AuthFlowIntegrationTest {

    @Autowired WebApplicationContext wac;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    void setup() {
        this.mockMvc = webAppContextSetup(wac).build();
    }

    @Test
    void registerLoginAndRefresh_shouldWork() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"patient1\"," +
                                "\"password\":\"Pa$$w0rd\"}"))
                .andExpect(status().isOk());

        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"patient1\"," +
                                "\"password\":\"Pa$$w0rd\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String loginBody = loginResult.getResponse().getContentAsString();
        assertThat(extractJsonValue(loginBody, "accessToken")).isNotBlank();
        String refreshToken = extractJsonValue(loginBody, "refreshToken");
        assertThat(refreshToken).isNotBlank();

        var refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String refreshBody = refreshResult.getResponse().getContentAsString();
        assertThat(extractJsonValue(refreshBody, "accessToken")).isNotBlank();
        assertThat(extractJsonValue(refreshBody, "refreshToken")).isNotBlank();
    }

    @Test
    void registerShouldStoreBcryptPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"username\":\"patient2\"," +
                                "\"password\":\"Secret123\"}"))
                .andExpect(status().isOk());

        User saved = userRepository.findByUsername("patient2").orElseThrow();
        assertThat(saved.getRole()).isEqualTo(RoleEnum.PATIENT);
        assertThat(saved.getPasswordHash()).startsWith("$2");
        assertThat(passwordEncoder.matches("Secret123", saved.getPasswordHash())).isTrue();
    }

    private static String extractJsonValue(String json, String key) {
        Matcher matcher = Pattern.compile("\\\"" + key + "\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }
}





