package com.project.back_end.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Data Transfer Object representing appointment details for communication
 * between backend services and frontend clients.
 */
public class AppointmentDTO {

    /**
     * Unique identifier for the appointment.
     */
    private Long id;

    /**
     * ID of the doctor associated with the appointment.
     */
    private Long doctorId;

    /**
     * Name of the doctor associated with the appointment.
     */
    private String doctorName;

    /**
     * ID of the patient associated with the appointment.
     */
    private Long patientId;

    /**
     * Name of the patient associated with the appointment.
     */
    private String patientName;

    /**
     * Email of the patient associated with the appointment.
     */
    private String patientEmail;

    /**
     * Phone number of the patient associated with the appointment.
     */
    private String patientPhone;

    /**
     * Address of the patient associated with the appointment.
     */
    private String patientAddress;

    /**
     * Scheduled date and time of the appointment.
     */
    private LocalDateTime appointmentTime;

    /**
     * Status of the appointment (e.g., Scheduled:0, Completed:1).
     */
    private int status;

    /**
     * Derived date part of the appointment (without time).
     */
    private LocalDate appointmentDate;

    /**
     * Derived time part of the appointment (without date).
     */
    private LocalTime appointmentTimeOnly;

    /**
     * Calculated end time of the appointment (appointmentTime + 1 hour).
     */
    private LocalDateTime endTime;

    /**
     * Constructs an AppointmentDTO and computes derived fields.
     *
     * @param id              Unique identifier for the appointment
     * @param doctorId        ID of the doctor assigned
     * @param doctorName      Name of the doctor
     * @param patientId       ID of the patient
     * @param patientName     Name of the patient
     * @param patientEmail    Email address of the patient
     * @param patientPhone    Phone number of the patient
     * @param patientAddress  Address of the patient
     * @param appointmentTime Scheduled date and time of the appointment
     * @param status          Appointment status code
     */
    public AppointmentDTO(
            Long id,
            Long doctorId,
            String doctorName,
            Long patientId,
            String patientName,
            String patientEmail,
            String patientPhone,
            String patientAddress,
            LocalDateTime appointmentTime,
            int status) {
        this.id = id;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.patientPhone = patientPhone;
        this.patientAddress = patientAddress;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.appointmentDate = appointmentTime.toLocalDate();
        this.appointmentTimeOnly = appointmentTime.toLocalTime();
        this.endTime = appointmentTime.plusHours(1);
    }

    public Long getId() {
        return id;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public String getPatientAddress() {
        return patientAddress;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public int getStatus() {
        return status;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public LocalTime getAppointmentTimeOnly() {
        return appointmentTimeOnly;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}
