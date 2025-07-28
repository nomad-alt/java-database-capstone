package com.project.back_end.controllers;

import com.project.back_end.models.Patient;
import com.project.back_end.DTO.Login;
import com.project.back_end.services.CentralService;
import com.project.back_end.services.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for patient-related endpoints.
 */
@RestController
@RequestMapping("${api.path}patient")
public class PatientController {

    private final PatientService patientService;
    private final CentralService service;

    public PatientController(PatientService patientService, CentralService service) {
        this.patientService = patientService;
        this.service = service;
    }

    /**
     * Registers a new patient.
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> createPatient(
            @RequestBody Patient patient) {
        int result = patientService.createPatient(patient);
        if (result == 1) {
            return ResponseEntity.ok(Map.of("message", "Signup successful"));
        } else {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to create patient"));
        }
    }

    /**
     * Logs in a patient and returns a JWT token on success.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Login login) {
        return service.validatePatientLogin(login);
    }

    /**
     * Retrieves patient details using a valid token.
     */
    @GetMapping("/details/{token}")
    public ResponseEntity<?> getPatientDetails(
            @PathVariable String token) {
        var validation = service.validateToken(token, "PATIENT");
        if (validation.getStatusCode().isError()) {
            return validation;
        }
        String email = service.extractIdentifier(token);
        Patient patient = patientService.findByEmail(email);
        if (patient == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(patient);
    }

    /**
     * Retrieves all appointments for a patient using a valid token.
     */
    @GetMapping("/appointments/{token}")
    public ResponseEntity<?> getAppointments(@PathVariable String token) {
        var validation = service.validateToken(token, "PATIENT");
        if (validation.getStatusCode().isError()) {
            return validation;
        }
        Long patientId = service.getUserIdFromToken(token);
        var resp = patientService.getPatientAppointment(patientId, token);
        return resp;
    }

    /**
     * Filters patient appointments by condition and/or doctor name.
     */
    @GetMapping("/filter/{token}")
    public ResponseEntity<?> filterAppointments(
            @PathVariable String token,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) String name) {
        var validation = service.validateToken(token, "PATIENT");
        if (validation.getStatusCode().isError()) {
            return validation;
        }
        return service.filterPatient(condition, name, token);
    }
}