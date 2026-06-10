package com.example.project.config;

import com.example.project.model.Appointment;
import com.example.project.model.MedicalRecord;
import com.example.project.model.RoleEnum;
import com.example.project.model.StatusEnum;
import com.example.project.model.User;
import com.example.project.repository.AppointmentRepository;
import com.example.project.repository.MedicalRecordRepository;
import com.example.project.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               AppointmentRepository appointmentRepository,
                               MedicalRecordRepository medicalRecordRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            User patient = upsertUser(userRepository, passwordEncoder, "patient1", "pass123", RoleEnum.PATIENT);
            User doctor = upsertUser(userRepository, passwordEncoder, "doctor1", "Doctor123", RoleEnum.DOCTOR);
            upsertUser(userRepository, passwordEncoder, "admin1", "Admin123", RoleEnum.ADMIN);
            upsertUser(userRepository, passwordEncoder, "patient2", "pass123", RoleEnum.PATIENT);

            if (appointmentRepository.count() == 0) {
                Appointment appointment = new Appointment();
                appointment.setDate(LocalDate.now().plusDays(3));
                appointment.setTimeSlot("09:00-09:30");
                appointment.setStatus(StatusEnum.PENDING);
                appointment.setSymptomDescription("Fever and headache");
                appointment.setReason("Kham tong quat");
                appointment.setPatient(patient);
                appointment.setDoctor(doctor);
                appointmentRepository.save(appointment);
            }

            if (medicalRecordRepository.count() == 0) {
                MedicalRecord record = new MedicalRecord();
                record.setFileUrl("uploads/medical/sample-record.txt");
                record.setDiagnosis("Sample diagnosis for Postman testing");
                record.setCreatedAt(LocalDateTime.now());
                record.setOwner(patient);
                medicalRecordRepository.save(record);
            }
        };
    }

    private User upsertUser(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            String username,
                            String rawPassword,
                            RoleEnum role) {
        return userRepository.findByUsername(username)
                .map(existing -> {
                    existing.setRole(role);
                    existing.setIsActive(true);
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    User user = new User();
                    user.setUsername(username);
                    user.setPasswordHash(passwordEncoder.encode(rawPassword));
                    user.setRole(role);
                    user.setIsActive(true);
                    return userRepository.save(user);
                });
    }
}
