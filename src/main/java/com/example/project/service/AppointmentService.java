package com.example.project.service;

import com.example.project.dto.AppointmentRequest;
import com.example.project.dto.AppointmentResponse;
import com.example.project.model.Appointment;
import com.example.project.model.RoleEnum;
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
        User patient = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không thấy"));

        if (patient.getRole() != RoleEnum.PATIENT) {
            throw new AccessDeniedException("Chỉ bệnh nhân mới đặt được lịch hẹn");
        }

        User doctor = userRepository.findById(req.doctorId())
                .orElseThrow(() -> new RuntimeException("Doctor không thấy"));

        if (doctor.getRole() == null || !doctor.getRole().name().equals("DOCTOR")) {
            throw new RuntimeException("Người dùng không phải bác sĩ");
        }
        boolean exists = appointmentRepository.existsByDoctorIdAndDateAndTimeSlot(
                doctor.getId(),
                req.date(),
                req.timeSlot()
        );

        if (exists) {
            throw new RuntimeException(
                    "Doctor đã có lịch hẹn vào lúc "
                            + req.date()
                            + " at "
                            + req.timeSlot()
            );
        }

        if (appointmentRepository.existsByPatientIdAndDateAndTimeSlot(
                patient.getId(),
                req.date(),
                req.timeSlot())) {

            throw new RuntimeException(
                    "Bạn đã có lịch hẹn vào khung giờ này rồi"
            );
        }
        Appointment appointment = new Appointment();
        appointment.setDate(req.date());
        appointment.setTimeSlot(req.timeSlot());
        appointment.setSymptomDescription(req.symptomDescription());
        appointment.setReason(req.reason());
        appointment.setStatus(StatusEnum.PENDING);

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);

        Appointment saved = appointmentRepository.save(appointment);

        return new AppointmentResponse(
                saved.getId(),
                saved.getDate(),
                saved.getTimeSlot(),
                saved.getStatus().name(),
                saved.getSymptomDescription(),
                saved.getReason(),
                saved.getPatient().getId(),
                saved.getDoctor().getId()
        );
    }

    public List<AppointmentResponse> getAppointmentsForPatient(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User không thấy"));
        List<Appointment> list = appointmentRepository.findAllByPatientIdOrderByDateDesc(user.getId());
        return list.stream().map(a -> {
            Long doctorId = a.getDoctor() != null ? a.getDoctor().getId() : null;
            return new AppointmentResponse(a.getId(), a.getDate(), a.getTimeSlot(), a.getStatus().name(), a.getSymptomDescription(), a.getReason(), a.getPatient().getId(), doctorId);
        }).collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse updateStatus(Long appointmentId, String newStatusStr, String username) {
        // Only allow ADMIN or DOCTOR to update status - check caller role by loading user
        User caller = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User không thấy"));
        String callerRole = caller.getRole() != null ? caller.getRole().name() : "";
        if (!callerRole.equals("ADMIN") && !callerRole.equals("DOCTOR")) {
            throw new SecurityException("Chỉ admin hoặc bác sĩ mới có thể thay đổi trạng thái cuộc hẹn");
        }

        Appointment ap = appointmentRepository.findById(appointmentId).orElseThrow(() -> new RuntimeException("Appointment không thấy"));
        StatusEnum newStatus = StatusEnum.valueOf(newStatusStr);
        ap.setStatus(newStatus);
        Appointment saved = appointmentRepository.save(ap);
        Long doctorId = saved.getDoctor() != null ? saved.getDoctor().getId() : null;
        return new AppointmentResponse(saved.getId(), saved.getDate(), saved.getTimeSlot(), saved.getStatus().name(), saved.getSymptomDescription(), saved.getReason(), saved.getPatient().getId(), doctorId);
    }

    public List<AppointmentResponse> getAppointmentsForDoctor(
            String username) {

        User doctor = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy bác sĩ"));

        List<Appointment> appointments =
                appointmentRepository
                        .findAllByDoctorIdOrderByDateDesc(
                                doctor.getId()
                        );

        return appointments.stream()
                .map(a -> new AppointmentResponse(
                        a.getId(),
                        a.getDate(),
                        a.getTimeSlot(),
                        a.getStatus().name(),
                        a.getSymptomDescription(),
                        a.getReason(),
                        a.getPatient().getId(),
                        a.getDoctor() != null
                                ? a.getDoctor().getId()
                                : null
                ))
                .toList();
    }
}



