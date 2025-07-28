import { API_BASE_URL } from "../config/config.js";

const DOCTOR_API = API_BASE_URL + '/doctor';

/**
 * Fetches all doctors from the API
 * @returns {Promise<Array>} Array of doctor objects or empty array on error
 */
export async function getDoctors() {
    try {
        const response = await fetch(DOCTOR_API);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        return data.doctors || [];
    } catch (error) {
        console.error('Error fetching doctors:', error);
        return [];
    }
}

/**
 * Deletes a specific doctor
 * @param {string} id Doctor ID to delete
 * @param {string} token Authentication token
 * @returns {Promise<Object>} Object with success status and message
 */
export async function deleteDoctor(id, token) {
    try {
        const response = await fetch(`${DOCTOR_API}/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        const result = await response.json();
        return {
            success: response.ok,
            message: result.message || (response.ok ? 'Doctor deleted successfully' : 'Failed to delete doctor')
        };
    } catch (error) {
        console.error('Error deleting doctor:', error);
        return {
            success: false,
            message: 'An error occurred while deleting the doctor'
        };
    }
}

/**
 * Saves a new doctor to the system
 * @param {Object} doctor Doctor data to save
 * @param {string} token Authentication token
 * @returns {Promise<Object>} Object with success status and message
 */
export async function saveDoctor(doctor, token) {
    try {
        const response = await fetch(DOCTOR_API, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(doctor)
        });

        const result = await response.json();
        return {
            success: response.ok,
            message: result.message || (response.ok ? 'Doctor saved successfully' : 'Failed to save doctor'),
            data: result.doctor || null
        };
    } catch (error) {
        console.error('Error saving doctor:', error);
        return {
            success: false,
            message: 'An error occurred while saving the doctor'
        };
    }
}

/**
 * Filters doctors based on criteria
 * @param {string} name Doctor name to filter by
 * @param {string} time Availability time to filter by
 * @param {string} specialty Specialty to filter by
 * @returns {Promise<Array>} Filtered array of doctors or empty array on error
 */
export async function filterDoctors(name, time, specialty) {
    try {
        const params = new URLSearchParams();
        if (name) params.append('name', name);
        if (time) params.append('time', time);
        if (specialty) params.append('specialty', specialty);

        const response = await fetch(`${DOCTOR_API}/filter?${params.toString()}`);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        return data.doctors || [];
    } catch (error) {
        console.error('Error filtering doctors:', error);
        alert('Failed to filter doctors. Please try again.');
        return [];
    }
}