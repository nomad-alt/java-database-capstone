// patientServices.js
import { API_BASE_URL } from "../config/config.js";
const PATIENT_API = `${API_BASE_URL}/patient`;

/**
 * Registers a new patient
 * @param {Object} data - Patient registration data
 * @returns {Promise<{success: boolean, message: string}>} 
 */
export async function patientSignup(data) {
  try {
    const response = await fetch(PATIENT_API, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(data)
    });
    
    const result = await response.json();
    
    if (!response.ok) {
      throw new Error(result.message || "Registration failed");
    }
    
    return { 
      success: true, 
      message: result.message,
      patient: result.patient // Include patient data if available
    };
  } catch (error) {
    console.error("patientSignup error:", error.message, "Data:", data);
    return { 
      success: false, 
      message: error.message || "An error occurred during registration" 
    };
  }
}

/**
 * Authenticates a patient
 * @param {Object} data - Login credentials {email, password}
 * @returns {Promise<Response>}
 */
export async function patientLogin(data) {
  try {
    return await fetch(`${PATIENT_API}/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(data)
    });
  } catch (error) {
    console.error("patientLogin error:", error.message, "Data:", data);
    throw error; // Let the caller handle
  }
}

/**
 * Fetches patient data
 * @param {string} token - Authentication token
 * @returns {Promise<Object|null>} - Patient data or null if error
 */
export async function getPatientData(token) {
  try {
    const response = await fetch(`${PATIENT_API}/data`, {
      headers: {
        "Authorization": `Bearer ${token}`
      }
    });
    
    if (!response.ok) {
      throw new Error("Failed to fetch patient data");
    }
    
    const data = await response.json();
    return data.patient || null;
    
  } catch (error) {
    console.error("getPatientData error:", error.message, "Token:", token);
    return null;
  }
}

/**
 * Fetches patient appointments
 * @param {string} id - Patient ID
 * @param {string} token - Authentication token
 * @param {string} user - User type ('patient' or 'doctor')
 * @returns {Promise<Array|null>} - Appointments array or null if error
 */
export async function getPatientAppointments(id, token, user) {
  try {
    const response = await fetch(`${PATIENT_API}/appointments/${id}`, {
      headers: {
        "Authorization": `Bearer ${token}`,
        "X-User-Role": user
      }
    });
    
    if (!response.ok) {
      throw new Error("Failed to fetch appointments");
    }
    
    const data = await response.json();
    return data.appointments || [];
    
  } catch (error) {
    console.error("getPatientAppointments error:", error.message, 
                 "ID:", id, "User:", user);
    return null;
  }
}

/**
 * Filters appointments by condition
 * @param {string} condition - Filter condition
 * @param {string} name - Patient name
 * @param {string} token - Authentication token
 * @returns {Promise<Array>} - Filtered appointments (empty array if error)
 */
export async function filterAppointments(condition, name, token) {
  try {
    const response = await fetch(
      `${PATIENT_API}/filter?condition=${encodeURIComponent(condition)}&name=${encodeURIComponent(name)}`, 
      {
        headers: {
          "Authorization": `Bearer ${token}`
        }
      }
    );
    
    if (!response.ok) {
      throw new Error("Failed to filter appointments");
    }
    
    const data = await response.json();
    return data.appointments || [];
    
  } catch (error) {
    console.error("filterAppointments error:", error.message, 
                 "Condition:", condition, "Name:", name);
    return [];
  }
}