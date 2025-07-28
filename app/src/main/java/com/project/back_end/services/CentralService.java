package com.project.back_end.services;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Central service coordinating authentication, validation,
 * and cross-entity operations (admin, doctor, patient, appointment).
 */
@Service
public class CentralService {

    private TokenService tokenService = null;
    private AdminRepository adminRepository = null;
    private DoctorRepository doctorRepository = null;
    private PatientRepository patientRepository = null;
    private DoctorService doctorService = null;
    private PatientService patientService = null;

    public CentralService(TokenService tokenService,
            AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            DoctorService doctorService,
            PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    /**
     * Validates a JWT token for a given user role.
     */
    public ResponseEntity<Map<String, String>> validateToken(String token, String userRole) {
        Map<String, String> resp = new HashMap<>();
        try {
            if (!tokenService.validateToken(token, userRole)) {
                resp.put("error", "Invalid or expired token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
            }
            resp.put("message", "Token is valid");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("error", "Token validation failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    /**
     * Authenticates an admin and issues a token on success.
     */
    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> resp = new HashMap<>();
        try {
            Admin stored = adminRepository.findByUsername(receivedAdmin.getUsername());
            if (stored == null || !stored.getPassword().equals(receivedAdmin.getPassword())) {
                resp.put("error", "Invalid admin credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
            }
            String token = tokenService.generateToken(stored.getId().toString(), "ADMIN");
            resp.put("token", token);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("error", "Admin authentication failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    /**
     * Filters doctors by name, specialty, and available time.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctor(String name, String specialty, String timeSlot) {
        // if any filter is null or empty, delegate accordingly
        if (name != null && specialty != null && timeSlot != null) {
            return doctorService.filterDoctorsByNameSpecialtyAndTime(name, specialty, timeSlot);
        } else if (name != null && timeSlot != null) {
            return doctorService.filterDoctorByNameAndTime(name, timeSlot);
        } else if (specialty != null && timeSlot != null) {
            return doctorService.filterDoctorByTimeAndSpecialty(specialty, timeSlot);
        } else if (specialty != null) {
            return doctorService.filterDoctorBySpecialty(specialty);
        } else if (name != null) {
            return doctorService.findDoctorByName(name);
        } else if (timeSlot != null) {
            return doctorService.filterDoctorsByTime(timeSlot);
        } else {
            Map<String, Object> all = new HashMap<>();
            all.put("doctors", doctorService.getDoctors());
            return all;
        }
    }

    /**
     * Validates if an appointment time is available for a doctor.
     * 
     * @return 1 if valid, 0 if unavailable, -1 if doctor not found
     */
    @Transactional(readOnly = true)
    public int validateAppointment(Appointment appointment) {
        Long docId = appointment.getDoctor().getId();
        Optional<Doctor> docOpt = doctorRepository.findById(docId);
        if (!docOpt.isPresent())
            return -1;
        List<String> slots = doctorService.getDoctorAvailability(docId, appointment.getAppointmentTime().toLocalDate());
        String fmt = appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        return slots.contains(fmt) ? 1 : 0;
    }

    /**
     * Checks if a patient record is unique (by email or phone).
     * 
     * @return true if no existing record, false otherwise
     */
    public boolean validatePatient(Patient patient) {
        Patient existing = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());
        return existing == null;
    }

    /**
     * Authenticates a patient and issues a token on success.
     */
    public ResponseEntity<Map<String, String>> validatePatientLogin(
            com.project.back_end.DTO.Login login) {
        Map<String, String> resp = new HashMap<>();
        try {
            Patient stored = patientRepository.findByEmail(login.getIdentifier());
            if (stored == null || !stored.getPassword().equals(login.getPassword())) {
                resp.put("error", "Invalid patient credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
            }
            String token = tokenService.generateToken(stored.getId().toString(), "PATIENT");
            resp.put("token", token);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("error", "Patient authentication failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    /**
     * Filters patient appointments based on condition or doctor name.
     */
    public ResponseEntity<Map<String, Object>> filterPatient(
            String condition, String doctorName, String token) {
        if (condition != null && doctorName != null) {
            return patientService.filterByDoctorAndCondition(condition, doctorName,
                    tokenService.getUserIdFromToken(token));
        } else if (condition != null) {
            return patientService.filterByCondition(condition,
                    tokenService.getUserIdFromToken(token));
        } else if (doctorName != null) {
            return patientService.filterByDoctor(doctorName,
                    tokenService.getUserIdFromToken(token));
        } else {
            return patientService.getPatientAppointment(
                    tokenService.getUserIdFromToken(token), token);
        }
    }

    public String extractIdentifier(String token) {
        return tokenService.extractIdentifier(token);
    }

    public Long getUserIdFromToken(String token) {
        return tokenService.getUserIdFromToken(token);
    }
}
