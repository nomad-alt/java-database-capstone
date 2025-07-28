// Import getAllAppointments to fetch appointments from the backend
import { getAllAppointments } from '../services/patientServices.js';
// Import createPatientRow to generate a table row for each patient appointment
import { createPatientRow } from '../components/patientRows.js';

// Get the table body where patient rows will be added
const tableBody = document.getElementById('patientTableBody');
// Initialize selectedDate with today's date in 'YYYY-MM-DD' format
const today = new Date();
const selectedDate = today.toISOString().split('T')[0];
// Get the saved token from localStorage (used for authenticated API calls)
const token = localStorage.getItem('token');
// Initialize patientName to null (used for filtering by name)
let patientName = null;

// DOM Elements
const searchBar = document.getElementById('searchBar');
const todayBtn = document.getElementById('todayBtn');
const datePicker = document.getElementById('datePicker');

// Set initial date picker value
datePicker.value = selectedDate;

// Add an 'input' event listener to the search bar
searchBar.addEventListener('input', (e) => {
    const searchValue = e.target.value.trim();
    
    if (searchValue) {
        patientName = searchValue;
    } else {
        patientName = null;
    }
    
    // Reload appointments with the new filter
    loadAppointments();
});

// Add a click listener to the "Today" button
todayBtn.addEventListener('click', () => {
    const today = new Date();
    const todayStr = today.toISOString().split('T')[0];
    datePicker.value = todayStr;
    selectedDate = todayStr;
    loadAppointments();
});

// Add a change event listener to the date picker
datePicker.addEventListener('change', (e) => {
    selectedDate = e.target.value;
    loadAppointments();
});

/**
 * Function: loadAppointments
 * Purpose: Fetch and display appointments based on selected date and optional patient name
 */
async function loadAppointments() {
    try {
        // Step 1: Call getAllAppointments with selectedDate, patientName, and token
        const appointments = await getAllAppointments(selectedDate, patientName, token);
        
        // Step 2: Clear the table body content before rendering new rows
        tableBody.innerHTML = '';
        
        // Step 3: If no appointments are returned
        if (!appointments || appointments.length === 0) {
            const noRecordsRow = document.createElement('tr');
            noRecordsRow.innerHTML = `
                <td colspan="5" class="noPatientRecord">
                    No appointments found for ${patientName ? `patients matching "${patientName}" on ` : ''}${selectedDate === new Date().toISOString().split('T')[0] ? 'today' : selectedDate}.
                </td>
            `;
            tableBody.appendChild(noRecordsRow);
            return;
        }
        
        // Step 4: If appointments exist
        appointments.forEach(appointment => {
            const patient = {
                id: appointment.patientId,
                name: `${appointment.patientFirstName} ${appointment.patientLastName}`,
                phone: appointment.patientPhone,
                email: appointment.patientEmail,
                appointmentId: appointment.id
            };
            
            const row = createPatientRow(patient);
            tableBody.appendChild(row);
        });
        
    } catch (error) {
        console.error('Error loading appointments:', error);
        // Step 5: Catch and handle any errors during fetch
        const errorRow = document.createElement('tr');
        errorRow.innerHTML = `
            <td colspan="5" class="noPatientRecord">
                Error loading appointments. Please try again later.
            </td>
        `;
        tableBody.appendChild(errorRow);
    }
}

// When the page is fully loaded
document.addEventListener('DOMContentLoaded', () => {
    // Call renderContent() (assumes it sets up the UI layout)
    if (typeof renderContent === 'function') {
        renderContent();
    }
    
    // Call loadAppointments() to display today's appointments by default
    loadAppointments();
});