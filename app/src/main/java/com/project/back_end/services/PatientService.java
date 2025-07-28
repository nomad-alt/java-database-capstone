package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.DTO.AppointmentDTO;
//import com.project.back_end.DTO.Login;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for managing patients, including creation and
 * retrieval/filtering of patient appointments and details.
 */
@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /**
     * Creates a new patient record.
     * 
     * @param patient the patient to create
     * @return 1 on success, 0 on failure
     */
    @Transactional
    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Retrieves all appointments for a patient, ensuring token matches.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        Map<String, Object> resp = new HashMap<>();
        Long patientId = tokenService.getUserIdFromToken(token);
        if (!id.equals(patientId)) {
            resp.put("error", "Unauthorized access");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
        }
        List<AppointmentDTO> dtos = appointmentRepository
                .findByPatientId(id)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        resp.put("appointments", dtos);
        return ResponseEntity.ok(resp);
    }

    /**
     * Filters appointments by condition ('past' or 'future').
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        Map<String, Object> resp = new HashMap<>();
        int status;
        if ("past".equalsIgnoreCase(condition)) {
            status = 1;
        } else if ("future".equalsIgnoreCase(condition)) {
            status = 0;
        } else {
            resp.put("error", "Invalid condition");
            return ResponseEntity.badRequest().body(resp);
        }
        List<AppointmentDTO> dtos = appointmentRepository
                .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, status)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        resp.put("appointments", dtos);
        return ResponseEntity.ok(resp);
    }

    /**
     * Filters appointments by doctor's name for given patient.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        Map<String, Object> resp = new HashMap<>();
        List<AppointmentDTO> dtos = appointmentRepository
                .filterByDoctorNameAndPatientId(name, patientId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        resp.put("appointments", dtos);
        return ResponseEntity.ok(resp);
    }

    /**
     * Filters appointments by doctor's name and condition for given patient.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition,
            String name,
            Long patientId) {
        // first filter by doctor name
        List<Appointment> byDoctor = appointmentRepository
                .filterByDoctorNameAndPatientId(name, patientId);

        int status = "past".equalsIgnoreCase(condition) ? 1 : "future".equalsIgnoreCase(condition) ? 0 : -1;
        if (status < 0) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("error", "Invalid condition");
            return ResponseEntity.badRequest().body(resp);
        }
        List<AppointmentDTO> dtos = byDoctor.stream()
                .filter(a -> a.getStatus() == status)
                .map(this::toDto)
                .collect(Collectors.toList());
        Map<String, Object> resp = new HashMap<>();
        resp.put("appointments", dtos);
        return ResponseEntity.ok(resp);
    }

    /**
     * Retrieves patient details from token.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        String email = tokenService.extractIdentifier(token);
        Optional<Patient> opt = Optional.ofNullable(patientRepository.findByEmail(email));
        Map<String, Object> resp = new HashMap<>();
        if (!opt.isPresent()) {
            resp.put("error", "Patient not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }
        resp.put("patient", opt.get());
        return ResponseEntity.ok(resp);
    }

    @Transactional(readOnly = true)
    public Patient findByEmail(String email) {
        return patientRepository.findByEmail(email);
    }

    /**
     * Helper: map Appointment to AppointmentDTO.
     */
    private AppointmentDTO toDto(Appointment a) {
        return new AppointmentDTO(
                a.getId(),
                a.getDoctor().getId(),
                a.getDoctor().getName(),
                a.getPatient().getId(),
                a.getPatient().getName(),
                a.getPatient().getEmail(),
                a.getPatient().getPhone(),
                a.getPatient().getAddress(),
                a.getAppointmentTime(),
                a.getStatus());
    }
}
