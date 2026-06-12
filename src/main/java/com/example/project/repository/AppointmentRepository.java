package com.example.project.repository;

import com.example.project.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findAllByPatientIdOrderByDateDesc(Long patientId);
    //không cho phép một bác sĩ có 2 lịch khám cùng ngày và cùng khung giờ
    boolean existsByDoctorIdAndDateAndTimeSlot(
            Long doctorId,
            LocalDate date,
            String timeSlot
    );
    // không được đặt 2 lịch cùng giờ
    boolean existsByPatientIdAndDateAndTimeSlot(
            Long patientId,
            LocalDate date,
            String timeSlot
    );

    List<Appointment> findAllByDoctorIdOrderByDateDesc(Long doctorId);
}