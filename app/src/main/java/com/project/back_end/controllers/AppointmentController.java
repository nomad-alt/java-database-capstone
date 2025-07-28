package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.CentralService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final CentralService service;

    public AppointmentController(AppointmentService appointmentService, CentralService service) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<?> getAppointments(
            @PathVariable LocalDate date,
            @PathVariable String patientName,
            @PathVariable String token) {

        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "doctor");
        if (validationResponse.getStatusCode().isError()) {
            return validationResponse;
        }

        Map<String, Object> appointments = appointmentService.getAppointments(patientName, date, token);
        return ResponseEntity.ok(appointments);
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(
            @RequestBody Appointment appointment,
            @PathVariable String token) {

        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "patient");
        if (validationResponse.getStatusCode().isError()) {
            return validationResponse;
        }

        int validationResult = service.validateAppointment(appointment);
        if (validationResult == -1) {
            return ResponseEntity.badRequest().body(Map.of("error", "Doctor does not exist"));
        } else if (validationResult == 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Appointment time unavailable"));
        }

        int bookingResult = appointmentService.bookAppointment(appointment);
        if (bookingResult == 1) {
            return ResponseEntity.status(201).body(Map.of("message", "Appointment booked successfully"));
        } else {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to book appointment"));
        }
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(
            @RequestBody Appointment appointment,
            @PathVariable String token) {

        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "patient");
        if (validationResponse.getStatusCode().isError()) {
            return validationResponse;
        }

        ResponseEntity<Map<String, String>> updateResponse = appointmentService.updateAppointment(appointment);
        return updateResponse;
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(
            @PathVariable Long id,
            @PathVariable String token) {

        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "patient");
        if (validationResponse.getStatusCode().isError()) {
            return validationResponse;
        }

        ResponseEntity<Map<String, String>> cancelResponse = appointmentService.cancelAppointment(id, token);
        return cancelResponse;
    }
}