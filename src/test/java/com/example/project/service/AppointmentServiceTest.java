package com.example.project.service;

import com.example.project.dto.AppointmentRequest;
import com.example.project.model.Appointment;
import com.example.project.model.RoleEnum;
import com.example.project.model.User;
import com.example.project.repository.AppointmentRepository;
import com.example.project.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

    @Mock
    AppointmentRepository appointmentRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    AppointmentService appointmentService;

    @Test
    void createAppointmentSuccess() {
        User patient = new User();
        patient.setId(10L);
        patient.setUsername("p1");
        patient.setRole(RoleEnum.PATIENT);

        when(userRepository.findByUsername("p1")).thenReturn(Optional.of(patient));

        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment a = invocation.getArgument(0);
            a.setId(5L);
            return a;
        });

        var req = new AppointmentRequest(LocalDate.of(2026,6,20), "09:00-09:30", "fever", null);
        var resp = appointmentService.createAppointment(req, "p1");
        assertNotNull(resp);
        assertEquals(5L, resp.id());
    }
}


