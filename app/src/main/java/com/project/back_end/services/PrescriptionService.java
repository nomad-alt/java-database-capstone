package com.project.back_end.services;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer for managing prescriptions, including saving and retrieving
 * prescriptions by appointment ID.
 */
@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    /**
     * Saves a new prescription. Prevents duplicate prescriptions for the same
     * appointment.
     *
     * @param prescription the prescription to save
     * @return a ResponseEntity containing a message and appropriate HTTP status
     */
    @Transactional
    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription) {
        Map<String, String> resp = new HashMap<>();
        try {
            List<Prescription> existing = prescriptionRepository.findByAppointmentId(prescription.getAppointmentId());
            if (existing != null && !existing.isEmpty()) {
                resp.put("error", "Prescription already exists for this appointment");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
            }
            prescriptionRepository.save(prescription);
            resp.put("message", "Prescription saved");
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (Exception e) {
            resp.put("error", "Failed to save prescription");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    /**
     * Retrieves prescriptions associated with a specific appointment ID.
     *
     * @param appointmentId the appointment ID
     * @return a ResponseEntity containing the prescription list or an error message
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId) {
        Map<String, Object> resp = new HashMap<>();
        try {
            List<Prescription> prescriptions = prescriptionRepository.findByAppointmentId(appointmentId);
            resp.put("prescriptions", prescriptions);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("error", "Failed to retrieve prescription");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }
}
