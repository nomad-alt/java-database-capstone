package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for managing appointments, encapsulating business logic
 * such as booking, updating, cancelling, and retrieving appointments.
 */
@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    // private final PatientRepository patientRepository;
    // private final DoctorRepository doctorRepository;
    private final TokenService tokenService;

    public AppointmentService(AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            TokenService tokenService) {
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /**
     * Books a new appointment. Returns 1 if successful, 0 on error.
     */
    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Updates an existing appointment. Returns a response indicating success or
     * failure.
     */
    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Map<String, String> response = new HashMap<>();
        Optional<Appointment> existingOpt = appointmentRepository.findById(appointment.getId());
        if (!existingOpt.isPresent()) {
            response.put("error", "Appointment not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        Appointment existing = existingOpt.get();
        if (!existing.getPatient().getId().equals(appointment.getPatient().getId())) {
            response.put("error", "Unauthorized to update this appointment");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        try {
            appointmentRepository.save(appointment);
            response.put("message", "Appointment updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to update appointment");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Cancels an appointment by ID, ensuring the requesting patient matches.
     */
    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(Long id, String token) {
        Map<String, String> response = new HashMap<>();
        Optional<Appointment> existingOpt = appointmentRepository.findById(id);
        if (!existingOpt.isPresent()) {
            response.put("error", "Appointment not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        Appointment existing = existingOpt.get();
        Long patientId = tokenService.getUserIdFromToken(token);
        if (!existing.getPatient().getId().equals(patientId)) {
            response.put("error", "Unauthorized to cancel this appointment");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        try {
            appointmentRepository.delete(existing);
            response.put("message", "Appointment canceled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to cancel appointment");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Retrieves appointments for a doctor on a specific date, optionally filtered
     * by patient name.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAppointments(String patientName, LocalDate date, String token) {
        Long doctorId = tokenService.getUserIdFromToken(token);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<Appointment> appointments;
        if (patientName != null && !patientName.isEmpty()) {
            appointments = appointmentRepository
                    .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                            doctorId, patientName, startOfDay, endOfDay);
        } else {
            appointments = appointmentRepository
                    .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("appointments", appointments);
        return result;
    }

    /**
     * Changes the status of an appointment.
     */
    @Transactional
    public ResponseEntity<Map<String, String>> changeStatus(Long id, int status) {
        Map<String, String> response = new HashMap<>();
        try {
            appointmentRepository.updateStatus(status, id);
            response.put("message", "Status updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to update status");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
