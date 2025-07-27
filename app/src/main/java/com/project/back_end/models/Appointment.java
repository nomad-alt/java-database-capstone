package com.project.back_end.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
public class Appointment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @NotNull(message = "Doctor is required")
  private Doctor doctor;

  @ManyToOne
  @NotNull(message = "Patient is required")
  private Patient patient;

  @Future(message = "Appointment time must be in the future")
  private LocalDateTime appointmentTime;

  @NotNull(message = "Status is required")
  private int status; // 0: Scheduled, 1: Completed

  @Size(max = 500)
  private String reasonForVisit;

  @Size(max = 1000)
  private String notes;

  // Default constructor (required by JPA)
  public Appointment() {
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Doctor getDoctor() {
    return doctor;
  }

  public void setDoctor(Doctor doctor) {
    this.doctor = doctor;
  }

  public Patient getPatient() {
    return patient;
  }

  public void setPatient(Patient patient) {
    this.patient = patient;
  }

  public LocalDateTime getAppointmentTime() {
    return appointmentTime;
  }

  public void setAppointmentTime(LocalDateTime appointmentTime) {
    this.appointmentTime = appointmentTime;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getReasonForVisit() {
    return reasonForVisit;
  }

  public void setReasonForVisit(String reasonForVisit) {
    this.reasonForVisit = reasonForVisit;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  // Helper Methods (transient - not persisted)
  @Transient
  public LocalDateTime getEndTime() {
    return appointmentTime != null ? appointmentTime.plusHours(1) : null;
  }

  @Transient
  public LocalDate getAppointmentDate() {
    return appointmentTime != null ? appointmentTime.toLocalDate() : null;
  }

  @Transient
  public LocalTime getAppointmentTimeOnly() {
    return appointmentTime != null ? appointmentTime.toLocalTime() : null;
  }
}