package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.CentralService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for prescription-related endpoints.
 */
@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final AppointmentService appointmentService;
    private final CentralService service;

    public PrescriptionController(PrescriptionService prescriptionService,
            AppointmentService appointmentService,
            CentralService service) {
        this.prescriptionService = prescriptionService;
        this.appointmentService = appointmentService;
        this.service = service;
    }

    /**
     * Saves a new prescription, updates appointment status, and returns a message.
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(
            @PathVariable String token,
            @RequestBody Prescription prescription) {
        // Validate doctor token
        ResponseEntity<Map<String, String>> auth = service.validateToken(token, "DOCTOR");
        if (auth.getStatusCode().isError()) {
            return auth;
        }
        // Update appointment status (1 = prescribed)
        ResponseEntity<Map<String, String>> statusResp = appointmentService
                .changeStatus(prescription.getAppointmentId(), 1);
        if (statusResp.getStatusCode().isError()) {
            return ResponseEntity.status(statusResp.getStatusCode())
                    .body(Map.of("error", "Failed to update appointment status"));
        }
        // Save prescription
        return prescriptionService.savePrescription(prescription);
    }

    /**
     * Retrieves prescription(s) by appointment ID.
     */
    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(
            @PathVariable Long appointmentId,
            @PathVariable String token) {
        // Validate doctor token
        ResponseEntity<Map<String, String>> auth = service.validateToken(token, "DOCTOR");
        if (auth.getStatusCode().isError()) {
            return ResponseEntity.status(auth.getStatusCode())
                    .body(Map.of("error", auth.getBody().get("error")));
        }
        // Fetch prescription data
        return prescriptionService.getPrescription(appointmentId);
    }
}
