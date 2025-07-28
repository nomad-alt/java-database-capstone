package com.project.back_end.repo;

import com.project.back_end.models.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Doctor entity, providing CRUD operations
 * and custom query methods for searching by name, email, and specialty.
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

      /**
       * Find a doctor by their email address.
       *
       * @param email the email of the doctor
       * @return the Doctor entity matching the given email, or null if none found
       */
      Doctor findByEmail(String email);

      /**
       * Find doctors whose names contain the given string (case-sensitive).
       *
       * @param name the substring to match within doctor names
       * @return list of matching Doctor entities
       */
      @Query("SELECT d FROM Doctor d WHERE d.name LIKE CONCAT('%', :name, '%')")
      List<Doctor> findByNameLike(String name);

      /**
       * Find doctors by partial name match (case-insensitive) and exact specialty
       * (case-insensitive).
       *
       * @param name      substring to match within doctor names
       * @param specialty specialty to match exactly (ignore case)
       * @return list of matching Doctor entities
       */
      @Query("SELECT d FROM Doctor d " +
                  "WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
                  "AND LOWER(d.specialty) = LOWER(:specialty)")
      List<Doctor> findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(String name, String specialty);

      /**
       * Find doctors by specialty, ignoring case.
       *
       * @param specialty the specialty to match (ignore case)
       * @return list of matching Doctor entities
       */
      List<Doctor> findBySpecialtyIgnoreCase(String specialty);
}
