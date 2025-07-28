// adminDashboard.js
import { getDoctors, filterDoctors, saveDoctor } from './services/doctorServices.js';
import { createDoctorCard } from './components/doctorCard.js';
import { openModal, closeModal } from './components/modals.js';
import { showNotification, debounce } from './util.js';

// Initialize dashboard
document.addEventListener('DOMContentLoaded', initializeDashboard);

function initializeDashboard() {
    // DOM Elements
    const contentArea = document.getElementById('doctorCardsContainer');
    const searchInput = document.getElementById('doctorSearch');
    const timeFilter = document.getElementById('timeFilter');
    const specialtyFilter = document.getElementById('specialtyFilter');
    const addDoctorBtn = document.getElementById('addDoctorBtn');
    const addDoctorForm = document.getElementById('addDoctorForm');
    
    // State
    let currentDoctors = [];
    
    // Event Listeners
    addDoctorBtn.addEventListener('click', () => openModal('addDoctorModal'));
    
    // Debounce search input (300ms delay)
    searchInput.addEventListener('input', debounce(filterDoctorsOnChange, 300));
    timeFilter.addEventListener('change', filterDoctorsOnChange);
    specialtyFilter.addEventListener('change', filterDoctorsOnChange);
    
    addDoctorForm.addEventListener('submit', adminAddDoctor);
    document.getElementById('addTimeSlot').addEventListener('click', addTimeSlot);
    
    // Initialize with one time slot
    addTimeSlot();
    
    // Load initial data
    loadDoctorCards();
    
    // Functions
    async function loadDoctorCards() {
        try {
            showLoadingState(true);
            currentDoctors = await getDoctors();
            renderDoctorCards(currentDoctors);
        } catch (error) {
            console.error('Failed to load doctors:', error);
            showNotification('Failed to load doctors. Please try again later.', 'error');
        } finally {
            showLoadingState(false);
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
        contentArea.textContent = '';
        
        if (!doctors || doctors.length === 0) {
            const noResults = document.createElement('div');
            noResults.className = 'no-results';
            noResults.innerHTML = `
                <i class="fas fa-user-md" aria-hidden="true"></i>
                <p>No doctors found with the given filters.</p>
            `;
            contentArea.appendChild(noResults);
            return;
        }
        
        const fragment = document.createDocumentFragment();
        doctors.forEach(doctor => {
            const card = createDoctorCard(doctor);
            fragment.appendChild(card);
        });
        contentArea.appendChild(fragment);
    }
    
    async function adminAddDoctor(event) {
        event.preventDefault();
        
        // Get form data
        const formData = new FormData(addDoctorForm);
        const name = formData.get('name').trim();
        const email = formData.get('email').trim();
        const phone = formData.get('phone').trim();
        const password = formData.get('password');
        const specialty = formData.get('specialty');
        
        // Validate email format
        if (!validateEmail(email)) {
            showNotification('Please enter a valid email address', 'error');
            return;
        }
        
        // Validate phone format
        if (!validatePhone(phone)) {
            showNotification('Please enter a valid phone number', 'error');
            return;
        }
        
        // Get available times
        const availableTimes = [];
        document.querySelectorAll('.time-slot').forEach(slot => {
            const time = slot.value.trim();
            if (time && validateTimeSlot(time)) {
                availableTimes.push(time);
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
            yearsOfExperience: formData.get('yearsExperience') || 0,
            clinicAddress: formData.get('clinicAddress') || '',
            rating: formData.get('rating') || 0
        };
        
        try {
            // Save doctor
            const savedDoctor = await saveDoctor(doctor, token);
            
            // Update local state
            currentDoctors = [...currentDoctors, savedDoctor];
            
            // Show success and reset form
            showNotification(`Dr. ${savedDoctor.name} added successfully!`, 'success');
            addDoctorForm.reset();
            closeModal('addDoctorModal');
            
            // Refresh doctor list
            renderDoctorCards(currentDoctors);
        } catch (error) {
            console.error('Failed to add doctor:', error);
            showNotification(`Failed to add doctor: ${error.message || 'Please try again'}`, 'error');
        }
    }
    
    function addTimeSlot() {
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
                   pattern="\\d{2}:\\d{2}-\\d{2}:\\d{2}" required
                   aria-label="Available time slot">
            <button type="button" class="remove-slot" aria-label="Remove time slot">
                <i class="fas fa-times"></i>
            </button>
        `;
        
        slotDiv.querySelector('.remove-slot').addEventListener('click', () => {
            slotDiv.remove();
        });
        
        timeSlotsContainer.appendChild(slotDiv);
    }
    
    function showLoadingState(show) {
        const loader = document.getElementById('loadingIndicator') || createLoader();
        loader.style.display = show ? 'block' : 'none';
        
        function createLoader() {
            const loader = document.createElement('div');
            loader.id = 'loadingIndicator';
            loader.className = 'loader';
            loader.style.display = 'none';
            loader.textContent = 'Loading...';
            contentArea.parentNode.insertBefore(loader, contentArea);
            return loader;
        }
    }
    
    // Validation helpers
    function validateEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }
    
    function validatePhone(phone) {
        return /^[\d\s\-()+]{6,}$/.test(phone);
    }
    
    function validateTimeSlot(time) {
        return /^\d{2}:\d{2}-\d{2}:\d{2}$/.test(time);
    }
}