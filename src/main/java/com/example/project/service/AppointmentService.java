package com.example.project.service;

import com.example.project.dto.AppointmentRequest;
import com.example.project.dto.AppointmentResponse;
import com.example.project.model.Appointment;
import com.example.project.model.StatusEnum;
import com.example.project.model.User;
import com.example.project.repository.AppointmentRepository;
import com.example.project.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository, UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AppointmentResponse createAppointment(AppointmentRequest req, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() == null || !user.getRole().name().equals("PATIENT")) {
            throw new AccessDeniedException("Only patients can create appointments");
        }

        Appointment a = new Appointment();
        a.setDate(req.date());
        a.setTimeSlot(req.timeSlot());
        a.setSymptomDescription(req.symptomDescription());
        a.setReason(req.reason());
        a.setStatus(StatusEnum.PENDING);
        a.setPatient(user);

        Appointment saved = appointmentRepository.save(a);
        Long doctorId = saved.getDoctor() != null ? saved.getDoctor().getId() : null;
        return new AppointmentResponse(saved.getId(), saved.getDate(), saved.getTimeSlot(), saved.getStatus().name(), saved.getSymptomDescription(), saved.getReason(), saved.getPatient().getId(), doctorId);
    }

    public List<AppointmentResponse> getAppointmentsForPatient(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        List<Appointment> list = appointmentRepository.findAllByPatientIdOrderByDateDesc(user.getId());
        return list.stream().map(a -> {
            Long doctorId = a.getDoctor() != null ? a.getDoctor().getId() : null;
            return new AppointmentResponse(a.getId(), a.getDate(), a.getTimeSlot(), a.getStatus().name(), a.getSymptomDescription(), a.getReason(), a.getPatient().getId(), doctorId);
        }).collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse updateStatus(Long appointmentId, String newStatusStr, String username) {
        // Only allow ADMIN or DOCTOR to update status - check caller role by loading user
        User caller = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        String callerRole = caller.getRole() != null ? caller.getRole().name() : "";
        if (!callerRole.equals("ADMIN") && !callerRole.equals("DOCTOR")) {
            throw new SecurityException("Only admin or doctor can change appointment status");
        }

        Appointment ap = appointmentRepository.findById(appointmentId).orElseThrow(() -> new RuntimeException("Appointment not found"));
        StatusEnum newStatus = StatusEnum.valueOf(newStatusStr);
        ap.setStatus(newStatus);
        Appointment saved = appointmentRepository.save(ap);
        Long doctorId = saved.getDoctor() != null ? saved.getDoctor().getId() : null;
        return new AppointmentResponse(saved.getId(), saved.getDate(), saved.getTimeSlot(), saved.getStatus().name(), saved.getSymptomDescription(), saved.getReason(), saved.getPatient().getId(), doctorId);
    }
}



