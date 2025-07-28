package com.project.back_end.repo;

import com.project.back_end.models.Prescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Prescription documents in MongoDB,
 * providing CRUD operations and custom finder methods.
 */
@Repository
public interface PrescriptionRepository extends MongoRepository<Prescription, String> {

    /**
     * Find all prescriptions associated with a specific appointment.
     *
     * @param appointmentId the ID of the appointment
     * @return list of Prescription documents matching the appointment ID
     */
    List<Prescription> findByAppointmentId(Long appointmentId);
}
