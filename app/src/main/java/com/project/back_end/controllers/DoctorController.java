package com.project.back_end.controllers;

import com.project.back_end.models.Doctor;
import com.project.back_end.services.CentralService;
import com.project.back_end.services.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * REST controller for doctor-related endpoints.
 */
@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final CentralService service;

    public DoctorController(DoctorService doctorService, CentralService service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable LocalDate date,
            @PathVariable String token) {

        var validation = service.validateToken(token, user);
        if (validation.getStatusCode().isError()) {
            return ResponseEntity.status(validation.getStatusCode())
                    .body(Map.of("error", validation.getBody().get("error")));
        }

        var slots = doctorService.getDoctorAvailability(doctorId, date);
        return ResponseEntity.ok(Map.of("availability", slots));
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> saveDoctor(
            @RequestBody Doctor doctor,
            @PathVariable String token) {

        var validation = service.validateToken(token, "ADMIN");
        if (validation.getStatusCode().isError()) {
            return validation;
        }

        int result = doctorService.saveDoctor(doctor);
        if (result == 1) {
            return ResponseEntity.ok(Map.of("message", "Doctor added to db"));
        } else if (result == -1) {
            return ResponseEntity.status(409).body(Map.of("error", "Doctor already exists"));
        } else {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to save doctor"));
        }
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(
            @RequestBody Doctor doctor,
            @PathVariable String token) {

        var validation = service.validateToken(token, "ADMIN");
        if (validation.getStatusCode().isError()) {
            return validation;
        }

        int result = doctorService.updateDoctor(doctor);
        if (result == 1) {
            return ResponseEntity.ok(Map.of("message", "Doctor updated"));
        } else if (result == -1) {
            return ResponseEntity.status(404).body(Map.of("error", "Doctor not found"));
        } else {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to update doctor"));
        }
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(
            @PathVariable Long id,
            @PathVariable String token) {

        var validation = service.validateToken(token, "ADMIN");
        if (validation.getStatusCode().isError()) {
            return validation;
        }

        int result = doctorService.deleteDoctor(id);
        if (result == 1) {
            return ResponseEntity.ok(Map.of("message", "Doctor deleted successfully"));
        } else if (result == -1) {
            return ResponseEntity.status(404).body(Map.of("error", "Doctor not found with id"));
        } else {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete doctor"));
        }
    }

    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filterDoctors(
            @PathVariable(required = false) String name,
            @PathVariable(required = false) String time,
            @PathVariable(required = false) String speciality) {

        var filtered = service.filterDoctor(name, speciality, time);
        return ResponseEntity.ok(filtered);
    }
}
