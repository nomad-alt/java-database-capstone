package com.project.back_end.repo;

import com.project.back_end.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Patient entity, providing CRUD operations
 * and custom finder methods for email and phone identification.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Find a patient by their email address.
     *
     * @param email the email of the patient
     * @return the Patient entity matching the given email, or null if none found
     */
    Patient findByEmail(String email);

    /**
     * Find a patient by either their email address or phone number.
     *
     * @param email the email of the patient
     * @param phone the phone number of the patient
     * @return the Patient entity matching the given email or phone, or null if none
     *         found
     */
    Patient findByEmailOrPhone(String email, String phone);
}