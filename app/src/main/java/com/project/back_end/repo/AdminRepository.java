package com.project.back_end.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.project.back_end.models.Admin;

/**
 * Repository interface for Admin entity, providing CRUD operations
 * and custom finder methods.
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    /**
     * Find an Admin by their username.
     *
     * @param username the username of the admin
     * @return the Admin entity matching the given username, or null if none found
     */
    Admin findByUsername(String username);
}
