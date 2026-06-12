package com.example.project.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(columnList = "username", name = "idx_users_username")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    @NotBlank
    private String username;

    @Column(nullable = false, length = 200)
    @NotBlank
    @JsonIgnore
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleEnum role;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<TokenBlacklist> tokenBlacklists = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @OneToMany(mappedBy = "patient")
    @JsonIgnore
    private List<Appointment> appointmentsAsPatient = new ArrayList<>();

    @OneToMany(mappedBy = "doctor")
    @JsonIgnore
    private List<Appointment> appointmentsAsDoctor = new ArrayList<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<MedicalRecord> medicalRecords = new ArrayList<>();

    public User() {}

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = true;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public RoleEnum getRole() { return role; }
    public void setRole(RoleEnum role) { this.role = role; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<TokenBlacklist> getTokenBlacklists() { return tokenBlacklists; }
    public void setTokenBlacklists(List<TokenBlacklist> tokenBlacklists) { this.tokenBlacklists = tokenBlacklists; }
    public List<RefreshToken> getRefreshTokens() { return refreshTokens; }
    public void setRefreshTokens(List<RefreshToken> refreshTokens) { this.refreshTokens = refreshTokens; }
    public List<Appointment> getAppointmentsAsPatient() { return appointmentsAsPatient; }
    public void setAppointmentsAsPatient(List<Appointment> appointmentsAsPatient) { this.appointmentsAsPatient = appointmentsAsPatient; }
    public List<Appointment> getAppointmentsAsDoctor() { return appointmentsAsDoctor; }
    public void setAppointmentsAsDoctor(List<Appointment> appointmentsAsDoctor) { this.appointmentsAsDoctor = appointmentsAsDoctor; }
    public List<MedicalRecord> getMedicalRecords() { return medicalRecords; }
    public void setMedicalRecords(List<MedicalRecord> medicalRecords) { this.medicalRecords = medicalRecords; }
}