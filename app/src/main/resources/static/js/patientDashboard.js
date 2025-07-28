// patientDashboard.js
import { getDoctors, filterDoctors } from './services/doctorServices.js';
import { openModal, closeModal } from './components/modals.js';
import { createDoctorCard } from './components/doctorCard.js';
import { patientSignup, patientLogin } from './services/patientServices.js';
import { showNotification, debounce } from './util.js';

// Main Controller Class
class PatientDashboard {
  constructor() {
    // DOM Elements
    this.contentDiv = document.getElementById('content');
    this.searchBar = document.getElementById('searchBar');
    this.timeFilter = document.getElementById('filterTime');
    this.specialtyFilter = document.getElementById('filterSpecialty');
    this.signupBtn = document.getElementById('patientSignup');
    this.loginBtn = document.getElementById('patientLogin');
    this.signupForm = document.getElementById('patientSignupForm');
    this.loginForm = document.getElementById('patientLoginForm');
    
    // Initialize
    this.init();
  }
  
  init() {
    // Event Listeners
    this.setupEventListeners();
    
    // Initial Load
    this.loadDoctorCards();
  }
  
  setupEventListeners() {
    // Search and Filter
    this.searchBar.addEventListener('input', debounce(() => this.filterDoctorsOnChange(), 300));
    this.timeFilter.addEventListener('change', () => this.filterDoctorsOnChange());
    this.specialtyFilter.addEventListener('change', () => this.filterDoctorsOnChange());
    
    // Modals
    if (this.signupBtn) {
      this.signupBtn.addEventListener('click', () => openModal('patientSignup'));
    }
    if (this.loginBtn) {
      this.loginBtn.addEventListener('click', () => openModal('patientLogin'));
    }
    
    // Forms
    if (this.signupForm) {
      this.signupForm.addEventListener('submit', (e) => {
        e.preventDefault();
        this.handleSignup();
      });
    }
    if (this.loginForm) {
      this.loginForm.addEventListener('submit', (e) => {
        e.preventDefault();
        this.handleLogin();
      });
    }
  }
  
  /**
   * Load all doctors initially
   */
  async loadDoctorCards() {
    try {
      this.showLoading(true);
      const doctors = await getDoctors();
      this.renderDoctors(doctors);
    } catch (error) {
      console.error('Failed to load doctors:', error);
      showNotification('Failed to load doctors. Please try again later.', 'error');
    } finally {
      this.showLoading(false);
    }
  }
  
  /**
   * Filter doctors based on search/filter criteria
   */
  async filterDoctorsOnChange() {
    try {
      this.showLoading(true);
      
      const name = this.searchBar.value.trim() || null;
      const time = this.timeFilter.value || null;
      const specialty = this.specialtyFilter.value || null;
      
      const filteredDoctors = await filterDoctors(name, time, specialty);
      this.renderDoctors(filteredDoctors);
    } catch (error) {
      console.error('Failed to filter doctors:', error);
      showNotification('Failed to filter doctors. Please try again.', 'error');
    } finally {
      this.showLoading(false);
    }
  }
  
  /**
   * Render doctors to the content area
   */
  renderDoctors(doctors) {
    // Clear existing content
    this.contentDiv.innerHTML = '';
    
    if (!doctors || doctors.length === 0) {
      this.renderEmptyState();
      return;
    }
    
    // Use document fragment for better performance
    const fragment = document.createDocumentFragment();
    
    doctors.forEach(doctor => {
      const card = createDoctorCard(doctor);
      fragment.appendChild(card);
    });
    
    this.contentDiv.appendChild(fragment);
  }
  
  /**
   * Show empty state message
   */
  renderEmptyState() {
    this.contentDiv.innerHTML = `
      <div class="empty-state">
        <i class="fas fa-user-md"></i>
        <p>No doctors found with the current filters.</p>
        <button class="btn-clear-filters">Clear Filters</button>
      </div>
    `;
    
    // Add clear filters functionality
    this.contentDiv.querySelector('.btn-clear-filters').addEventListener('click', () => {
      this.searchBar.value = '';
      this.timeFilter.value = '';
      this.specialtyFilter.value = '';
      this.loadDoctorCards();
    });
  }
  
  /**
   * Handle patient signup
   */
  async handleSignup() {
    try {
      const formData = new FormData(this.signupForm);
      const data = {
        name: formData.get('name'),
        email: formData.get('email'),
        password: formData.get('password'),
        phone: formData.get('phone'),
        address: formData.get('address')
      };
      
      // Basic validation
      if (!data.name || !data.email || !data.password) {
        showNotification('Please fill all required fields', 'error');
        return;
      }
      
      const { success, message } = await patientSignup(data);
      
      if (success) {
        showNotification(message, 'success');
        closeModal('patientSignup');
        this.signupForm.reset();
      } else {
        showNotification(message, 'error');
      }
    } catch (error) {
      console.error('Signup failed:', error);
      showNotification('An error occurred during signup. Please try again.', 'error');
    }
  }
  
  /**
   * Handle patient login
   */
  async handleLogin() {
    try {
      const formData = new FormData(this.loginForm);
      const data = {
        email: formData.get('email'),
        password: formData.get('password')
      };
      
      const response = await patientLogin(data);
      
      if (response.ok) {
        const result = await response.json();
        localStorage.setItem('token', result.token);
        showNotification('Login successful!', 'success');
        window.location.href = '/pages/loggedPatientDashboard.html';
      } else {
        const error = await response.json();
        showNotification(error.message || 'Invalid credentials', 'error');
      }
    } catch (error) {
      console.error('Login failed:', error);
      showNotification('An error occurred during login. Please try again.', 'error');
    }
  }
  
  /**
   * Show/hide loading state
   */
  showLoading(show) {
    if (show) {
      this.contentDiv.innerHTML = `
        <div class="loading-state">
          <div class="spinner"></div>
          <p>Loading doctors...</p>
        </div>
      `;
    }
  }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
  new PatientDashboard();
});