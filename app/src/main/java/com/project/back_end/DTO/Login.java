package com.project.back_end.DTO;

/**
 * Data Transfer Object for handling login requests from clients.
 * Encapsulates user credentials (identifier and password).
 */
public class Login {

    /**
     * Unique identifier of the user attempting to log in.
     * Can be an email (Doctor/Patient) or username (Admin).
     */
    private String identifier;

    /**
     * Password provided by the user for authentication.
     */
    private String password;

    /**
     * Default no-argument constructor.
     */
    public Login() {
    }

    /**
     * Returns the user identifier.
     * 
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the user identifier.
     * 
     * @param identifier the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Returns the user password.
     * 
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user password.
     * 
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
