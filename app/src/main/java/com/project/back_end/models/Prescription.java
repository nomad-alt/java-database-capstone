package com.project.back_end.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;

@Document(collection = "prescriptions")
public class Prescription {

  @Id
  private String id;

  @NotNull(message = "Patient name is required")
  @Size(min = 3, max = 100, message = "Patient name must be 3-100 characters")
  private String patientName;

  @NotNull(message = "Appointment ID is required")
  private Long appointmentId;

  @NotNull(message = "Medication is required")
  @Size(min = 3, max = 100, message = "Medication must be 3-100 characters")
  private String medication;

  @NotNull(message = "Dosage is required")
  @Size(min = 3, max = 20, message = "Dosage must be 3-20 characters")
  private String dosage;

  @Size(max = 200, message = "Doctor notes cannot exceed 200 characters")
  private String doctorNotes;

  @Min(0)
  @Max(12)
  private Integer refillCount = 0;

  @Size(max = 100)
  private String pharmacyName;

  // Default constructor (required by Spring Data)
  public Prescription() {
  }

  // Parameterized constructor for required fields
  public Prescription(String patientName, Long appointmentId, String medication, String dosage) {
    this.patientName = patientName;
    this.appointmentId = appointmentId;
    this.medication = medication;
    this.dosage = dosage;
  }

  // Getters and Setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPatientName() {
    return patientName;
  }

  public void setPatientName(String patientName) {
    this.patientName = patientName;
  }

  public Long getAppointmentId() {
    return appointmentId;
  }

  public void setAppointmentId(Long appointmentId) {
    this.appointmentId = appointmentId;
  }

  public String getMedication() {
    return medication;
  }

  public void setMedication(String medication) {
    this.medication = medication;
  }

  public String getDosage() {
    return dosage;
  }

  public void setDosage(String dosage) {
    this.dosage = dosage;
  }

  public String getDoctorNotes() {
    return doctorNotes;
  }

  public void setDoctorNotes(String doctorNotes) {
    this.doctorNotes = doctorNotes;
  }

  public Integer getRefillCount() {
    return refillCount;
  }

  public void setRefillCount(Integer refillCount) {
    this.refillCount = refillCount;
  }

  public String getPharmacyName() {
    return pharmacyName;
  }

  public void setPharmacyName(String pharmacyName) {
    this.pharmacyName = pharmacyName;
  }
}