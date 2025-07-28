package com.project.back_end.services;

import com.project.back_end.models.Doctor;
import com.project.back_end.models.Appointment;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.DTO.Login;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service layer for managing doctors, encapsulating operations such as
 * availability lookup, CRUD on doctors, and login validation.
 */
@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository,
            TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /**
     * Fetches available time slots for a doctor on a given date.
     */
    @Transactional(readOnly = true)
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        List<Appointment> booked = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);

        // assume doctor has fixed slots 09:00 to 17:00 each hour
        List<String> slots = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        for (int h = 9; h < 17; h++) {
            slots.add(String.format("%02d:00", h));
        }
        // remove booked slots
        for (Appointment appt : booked) {
            String time = appt.getAppointmentTime().format(fmt);
            slots.remove(time);
        }
        return slots;
    }

    /**
     * Saves a new doctor; checks for existing email first.
     */
    @Transactional
    public int saveDoctor(Doctor doctor) {
        if (doctorRepository.findByEmail(doctor.getEmail()) != null)
            return -1;
        try {
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Updates an existing doctor's details.
     */
    @Transactional
    public int updateDoctor(Doctor doctor) {
        if (!doctorRepository.existsById(doctor.getId()))
            return -1;
        try {
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Retrieves all doctors.
     */
    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    /**
     * Deletes a doctor and all their appointments.
     */
    @Transactional
    public int deleteDoctor(Long id) {
        if (!doctorRepository.existsById(id))
            return -1;
        try {
            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Validates doctor's credentials and returns a JWT token on success.
     */
    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> resp = new HashMap<>();
        Doctor doctor = doctorRepository.findByEmail(login.getIdentifier());
        if (doctor == null || !doctor.getPassword().equals(login.getPassword())) {
            resp.put("error", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
        }
        String token = tokenService.generateToken(doctor.getId().toString(), "DOCTOR");
        resp.put("token", token);
        return ResponseEntity.ok(resp);
    }

    /**
     * Finds doctors by partial name match.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> findDoctorByName(String name) {
        List<Doctor> list = doctorRepository.findByNameLike(name);
        return Collections.singletonMap("doctors", list);
    }

    /**
     * Filter doctors by name, specialty, and availability (AM/PM).
     */
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByNameSpecialtyAndTime(
            String name, String specialty, String amOrPm) {
        List<Doctor> base = doctorRepository
                .findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        List<Doctor> filtered = filterByTime(base, amOrPm);
        return Collections.singletonMap("doctors", filtered);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        List<Doctor> base = doctorRepository.findByNameLike(name);
        return Collections.singletonMap("doctors", filterByTime(base, amOrPm));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndSpecialty(String name, String specialty) {
        List<Doctor> list = doctorRepository
                .findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        return Collections.singletonMap("doctors", list);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByTimeAndSpecialty(String specialty, String amOrPm) {
        List<Doctor> base = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        return Collections.singletonMap("doctors", filterByTime(base, amOrPm));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorBySpecialty(String specialty) {
        List<Doctor> list = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        return Collections.singletonMap("doctors", list);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        List<Doctor> all = doctorRepository.findAll();
        return Collections.singletonMap("doctors", filterByTime(all, amOrPm));
    }

    /**
     * Helper to filter doctors by AM/PM availability based on availableTimes
     * strings.
     */
    private List<Doctor> filterByTime(List<Doctor> doctors, String amOrPm) {
        List<Doctor> result = new ArrayList<>();
        for (Doctor doc : doctors) {
            boolean ok = false;
            List<String> times = doc.getAvailableTimes();
            if (times != null) {
                for (String timeStr : times) {
                    LocalTime time = LocalTime.parse(timeStr);
                    int hour = time.getHour();
                    if ("AM".equalsIgnoreCase(amOrPm) && hour < 12)
                        ok = true;
                    if ("PM".equalsIgnoreCase(amOrPm) && hour >= 12)
                        ok = true;
                }
            }
            if (ok)
                result.add(doc);
        }
        return result;
    }
}
