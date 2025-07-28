// adminDashboard.js
import { getDoctors, filterDoctors, saveDoctor } from './services/doctorServices.js';
import { createDoctorCard } from './components/doctorCard.js';
import { openModal, closeModal } from './components/modals.js';
import { showNotification } from './util.js';

// DOM Elements
const contentArea = document.getElementById('doctorCardsContainer');
const searchInput = document.getElementById('doctorSearch');
const timeFilter = document.getElementById('timeFilter');
const specialtyFilter = document.getElementById('specialtyFilter');
const addDoctorBtn = document.getElementById('addDoctorBtn');
const addDoctorForm = document.getElementById('addDoctorForm');

document.addEventListener('DOMContentLoaded', () => {
    // Initialize dashboard
    loadDoctorCards();
    
    // Event Listeners
    addDoctorBtn.addEventListener('click', () => openModal('addDoctorModal'));
    
    [searchInput, timeFilter, specialtyFilter].forEach(element => {
        element.addEventListener('input', filterDoctorsOnChange);
        element.addEventListener('change', filterDoctorsOnChange);
    });
    
    addDoctorForm.addEventListener('submit', adminAddDoctor);
});

async function loadDoctorCards() {
    try {
        const doctors = await getDoctors();
        renderDoctorCards(doctors);
    } catch (error) {
        console.error('Failed to load doctors:', error);
        showNotification('Failed to load doctors. Please try again later.', 'error');
    }
}

async function filterDoctorsOnChange() {
    try {
        const name = searchInput.value.trim() || null;
        const time = timeFilter.value || null;
        const specialty = specialtyFilter.value || null;
        
        const filteredDoctors = await filterDoctors(name, time, specialty);
        renderDoctorCards(filteredDoctors);
    } catch (error) {
        console.error('Filter error:', error);
        showNotification('Failed to filter doctors. Please try again.', 'error');
    }
}

function renderDoctorCards(doctors) {
    // Clear current content
    contentArea.innerHTML = '';
    
    if (doctors.length === 0) {
        contentArea.innerHTML = `
            <div class="no-results">
                <i class="fas fa-user-md"></i>
                <p>No doctors found with the given filters.</p>
            </div>
        `;
        return;
    }
    
    doctors.forEach(doctor => {
        const card = createDoctorCard(doctor);
        contentArea.appendChild(card);
    });
}

async function adminAddDoctor(event) {
    event.preventDefault();
    
    // Get form data
    const name = document.getElementById('doctorName').value.trim();
    const email = document.getElementById('doctorEmail').value.trim();
    const phone = document.getElementById('doctorPhone').value.trim();
    const password = document.getElementById('doctorPassword').value;
    const specialty = document.getElementById('doctorSpecialty').value;
    
    // Get available times
    const availableTimes = [];
    document.querySelectorAll('.time-slot').forEach(slot => {
        if (slot.value.trim()) {
            availableTimes.push(slot.value.trim());
        }
    });
    
    // Basic validation
    if (!name || !email || !phone || !password || !specialty || availableTimes.length === 0) {
        showNotification('Please fill all required fields', 'error');
        return;
    }
    
    // Get authentication token
    const token = localStorage.getItem('token');
    if (!token) {
        showNotification('Authentication required. Please log in again.', 'error');
        return;
    }
    
    // Create doctor object
    const doctor = {
        name,
        email,
        phone,
        password,
        specialty,
        availableTimes,
        yearsOfExperience: document.getElementById('yearsExperience').value || 0,
        clinicAddress: document.getElementById('clinicAddress').value || '',
        rating: document.getElementById('doctorRating').value || 0
    };
    
    try {
        // Save doctor
        const savedDoctor = await saveDoctor(doctor, token);
        
        // Show success and reset form
        showNotification(`Dr. ${savedDoctor.name} added successfully!`, 'success');
        addDoctorForm.reset();
        closeModal('addDoctorModal');
        
        // Refresh doctor list
        loadDoctorCards();
    } catch (error) {
        console.error('Failed to add doctor:', error);
        showNotification(`Failed to add doctor: ${error.message || 'Please try again'}`, 'error');
    }
}

// Add time slot functionality
document.getElementById('addTimeSlot').addEventListener('click', () => {
    const timeSlotsContainer = document.getElementById('timeSlotsContainer');
    const slotCount = timeSlotsContainer.querySelectorAll('.time-slot').length;
    
    if (slotCount >= 8) {
        showNotification('Maximum 8 time slots allowed', 'warning');
        return;
    }
    
    const slotDiv = document.createElement('div');
    slotDiv.className = 'time-slot-container';
    slotDiv.innerHTML = `
        <input type="text" class="time-slot" placeholder="HH:MM-HH:MM" 
               pattern="\\d{2}:\\d{2}-\\d{2}:\\d{2}">
        <button type="button" class="remove-slot">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    slotDiv.querySelector('.remove-slot').addEventListener('click', () => {
        slotDiv.remove();
    });
    
    timeSlotsContainer.appendChild(slotDiv);
});

// Initialize with one time slot
document.getElementById('addTimeSlot').click();