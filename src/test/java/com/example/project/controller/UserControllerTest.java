package com.example.project.controller;

import com.example.project.dto.UserResponse;
import com.example.project.model.RoleEnum;
import com.example.project.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin1")
    void searchUsers() throws Exception {
        var user = new UserResponse(1L, "patient1", RoleEnum.PATIENT, true, null, null);
        when(userService.search(eq("patient"), any()))
                .thenReturn(new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/admin/users?keyword=patient&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].username").value("patient1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin1")
    void createUser() throws Exception {
        var user = new UserResponse(2L, "doctor2", RoleEnum.DOCTOR, true, null, null);
        when(userService.create(any(UserService.UserRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"doctor2\",\"password\":\"Doctor123\",\"role\":\"DOCTOR\",\"isActive\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("DOCTOR"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin1")
    void deleteUser() throws Exception {
        doNothing().when(userService).delete(2L);

        mockMvc.perform(delete("/api/v1/admin/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
