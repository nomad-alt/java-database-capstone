package com.project.back_end.repo;

import com.project.back_end.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Appointment entity, providing CRUD operations
 * and advanced query methods for filtering and managing appointments.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

      /**
       * Retrieve appointments for a doctor within a given time range,
       * eagerly fetching doctor and availability data.
       */
      @Query("SELECT a FROM Appointment a " +
                  "LEFT JOIN FETCH a.doctor d " +
                  "LEFT JOIN FETCH d.availability av " +
                  "WHERE a.doctor.id = :doctorId " +
                  "AND a.appointmentTime BETWEEN :start AND :end")
      List<Appointment> findByDoctorIdAndAppointmentTimeBetween(Long doctorId,
                  LocalDateTime start,
                  LocalDateTime end);

      /**
       * Retrieve appointments by doctor ID, partial patient name (case-insensitive),
       * and time range,
       * fetching both doctor and patient details.
       */
      @Query("SELECT a FROM Appointment a " +
                  "LEFT JOIN FETCH a.doctor d " +
                  "LEFT JOIN FETCH a.patient p " +
                  "WHERE a.doctor.id = :doctorId " +
                  "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :patientName, '%')) " +
                  "AND a.appointmentTime BETWEEN :start AND :end")
      List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                  Long doctorId,
                  String patientName,
                  LocalDateTime start,
                  LocalDateTime end);

      /**
       * Delete all appointments related to a specific doctor.
       */
      @Modifying
      @Transactional
      void deleteAllByDoctorId(Long doctorId);

      /**
       * Find all appointments for a specific patient.
       */
      List<Appointment> findByPatientId(Long patientId);

      /**
       * Retrieve appointments for a patient by status, ordered by appointment time
       * ascending.
       */
      List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);

      /**
       * Filter appointments by partial doctor name (case-insensitive) and patient ID.
       */
      @Query("SELECT a FROM Appointment a " +
                  "WHERE LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) " +
                  "AND a.patient.id = :patientId")
      List<Appointment> filterByDoctorNameAndPatientId(String doctorName, Long patientId);

      /**
       * Filter appointments by partial doctor name, patient ID, and status.
       */
      @Query("SELECT a FROM Appointment a " +
                  "WHERE LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) " +
                  "AND a.patient.id = :patientId " +
                  "AND a.status = :status")
      List<Appointment> filterByDoctorNameAndPatientIdAndStatus(String doctorName, Long patientId, int status);

      /**
       * Update the status of a specific appointment.
       */
      @Modifying
      @Transactional
      @Query("UPDATE Appointment a SET a.status = :status WHERE a.id = :id")
      void updateStatus(int status, Long id);
}
