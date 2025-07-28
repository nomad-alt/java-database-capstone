package com.project.back_end.services;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Component for JWT token generation, extraction, and validation.
 */
@Component
public class TokenService {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey signingKey;

    public TokenService(AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    @PostConstruct
    private void init() {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generates a JWT token containing the identifier as subject and role claim,
     * valid for 7 days.
     *
     * @param identifier unique identifier (e.g., username or email)
     * @param role       user role (ADMIN, DOCTOR, PATIENT)
     * @return signed JWT token
     */
    public String generateToken(String identifier, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 7L * 24 * 60 * 60 * 1000);
        return Jwts.builder()
                .subject(identifier)
                .issuedAt(now)
                .expiration(expiry)
                .claim("role", role)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extracts the subject (identifier) from the JWT token.
     *
     * @param token JWT token
     * @return subject (identifier), or null if token invalid
     */
    /**
     * Extracts the subject (identifier) from the JWT token.
     *
     * @param token JWT token
     * @return subject (identifier), or null if token invalid
     */
    public String extractIdentifier(String token) {
        try {
            Claims claims = Jwts.parser()
                    .decryptWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Validates the token for the given role by checking expiration and user
     * existence.
     *
     * @param token    JWT token
     * @param userRole expected role (ADMIN, DOCTOR, PATIENT)
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token, String userRole) {
        try {
            Claims claims = ((JwtParserBuilder) Jwts.builder())
                    .decryptWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String subject = claims.getSubject();
            String role = claims.get("role", String.class);
            if (!userRole.equalsIgnoreCase(role)) {
                return false;
            }
            switch (role.toUpperCase()) {
                case "ADMIN":
                    return adminRepository.findByUsername(subject) != null;
                case "DOCTOR":
                    return doctorRepository.findByEmail(subject) != null;
                case "PATIENT":
                    return patientRepository.findByEmail(subject) != null;
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Retrieves the signing key used for JWT operations.
     *
     * @return SecretKey for signing/verifying tokens
     */
    /**
     * Retrieves the user ID (Long) encoded in the JWT token by extracting the
     * identifier
     * and looking up the associated user in the repositories.
     *
     * @param token the JWT token containing the user's identifier in the subject
     * @return the user ID if found, or null if not found or on error
     */
    public Long getUserIdFromToken(String token) {
        String identifier = extractIdentifier(token);
        if (identifier == null) {
            return null;
        }
        // Attempt to find as Patient
        try {
            var patient = patientRepository.findByEmail(identifier);
            if (patient != null) {
                return patient.getId();
            }
        } catch (Exception ignored) {
        }
        // Attempt to find as Doctor
        try {
            var doctor = doctorRepository.findByEmail(identifier);
            if (doctor != null) {
                return doctor.getId();
            }
        } catch (Exception ignored) {
        }
        // Attempt to find as Admin
        try {
            var admin = adminRepository.findByUsername(identifier);
            if (admin != null) {
                return admin.getId();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Retrieves the signing key used for JWT operations.
     *
     * @return SecretKey for signing/verifying tokens
     */
    public SecretKey getSigningKey() {
        return signingKey;
    }
}
