// doctorDashboard.js
import { getAllAppointments } from '../services/patientServices.js';
import { createPatientRow } from '../components/patientRows.js';
import { debounce, showNotification } from '../util.js';

/**
 * Doctor Dashboard Controller
 * Manages appointment viewing and filtering for doctors
 */
class DoctorDashboard {
  constructor() {
    // DOM Elements
    this.tableBody = document.getElementById('patientTableBody');
    this.searchBar = document.getElementById('searchBar');
    this.todayBtn = document.getElementById('todayBtn');
    this.datePicker = document.getElementById('datePicker');
    this.loadingIndicator = document.createElement('div');
    
    // State
    this.state = {
      selectedDate: new Date().toISOString().split('T')[0],
      patientName: null,
      token: localStorage.getItem('token'),
      appointments: []
    };
    
    // Initialize
    this.init();
  }
  
  init() {
    this.setupLoadingIndicator();
    this.setupEventListeners();
    this.loadAppointments();
  }
  
  setupLoadingIndicator() {
    this.loadingIndicator.id = 'appointments-loading';
    this.loadingIndicator.className = 'loading-indicator';
    this.loadingIndicator.textContent = 'Loading appointments...';
    this.tableBody.parentNode.insertBefore(this.loadingIndicator, this.tableBody);
    this.loadingIndicator.style.display = 'none';
  }
  
  setupEventListeners() {
    // Debounced search (300ms delay)
    this.searchBar.addEventListener('input', debounce((e) => {
      this.state.patientName = e.target.value.trim() || null;
      this.loadAppointments();
    }, 300));
    
    this.todayBtn.addEventListener('click', () => {
      const today = new Date().toISOString().split('T')[0];
      this.state.selectedDate = today;
      this.datePicker.value = today;
      this.loadAppointments();
    });
    
    this.datePicker.addEventListener('change', (e) => {
      this.state.selectedDate = e.target.value;
      this.loadAppointments();
    });
  }
  
  /**
   * Load appointments based on current filters
   */
  async loadAppointments() {
    try {
      this.showLoading(true);
      
      const { selectedDate, patientName, token } = this.state;
      this.state.appointments = await getAllAppointments(selectedDate, patientName, token);
      
      this.renderAppointments();
    } catch (error) {
      console.error('Error loading appointments:', error);
      this.showError('Failed to load appointments. Please try again.');
    } finally {
      this.showLoading(false);
    }
  }
  
  /**
   * Render appointments to the table
   */
  renderAppointments() {
    // Clear existing content
    this.tableBody.innerHTML = '';
    
    const { appointments, selectedDate, patientName } = this.state;
    
    if (!appointments || appointments.length === 0) {
      this.renderEmptyState(selectedDate, patientName);
      return;
    }
    
    // Use document fragment for better performance
    const fragment = document.createDocumentFragment();
    
    appointments.forEach(appointment => {
      const patient = this.normalizeAppointmentData(appointment);
      const row = createPatientRow(patient);
      fragment.appendChild(row);
    });
    
    this.tableBody.appendChild(fragment);
  }
  
  /**
   * Normalize appointment data for consistent structure
   */
  normalizeAppointmentData(appointment) {
    return {
      id: appointment.patientId,
      name: `${appointment.patientFirstName} ${appointment.patientLastName}`,
      phone: appointment.patientPhone,
      email: appointment.patientEmail,
      appointmentId: appointment.id,
      time: appointment.appointmentTime,
      status: appointment.status
    };
  }
  
  /**
   * Show empty state message
   */
  renderEmptyState(date, searchTerm) {
    const isToday = date === new Date().toISOString().split('T')[0];
    const dateText = isToday ? 'today' : `on ${date}`;
    const searchText = searchTerm ? ` matching "${searchTerm}"` : '';
    
    const row = document.createElement('tr');
    row.innerHTML = `
      <td colspan="6" class="no-records">
        <i class="fas fa-calendar-check"></i>
        <p>No appointments found${searchText} ${dateText}.</p>
      </td>
    `;
    this.tableBody.appendChild(row);
  }
  
  /**
   * Show error state
   */
  showError(message) {
    this.tableBody.innerHTML = `
      <tr>
        <td colspan="6" class="error-state">
          <i class="fas fa-exclamation-triangle"></i>
          <p>${message}</p>
          <button class="retry-btn">Retry</button>
        </td>
      </tr>
    `;
    
    // Add retry functionality
    this.tableBody.querySelector('.retry-btn').addEventListener('click', () => {
      this.loadAppointments();
    });
  }
  
  /**
   * Toggle loading indicator
   */
  showLoading(show) {
    this.loadingIndicator.style.display = show ? 'block' : 'none';
    this.tableBody.style.opacity = show ? '0.5' : '1';
  }
}

// Initialize dashboard when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
  new DoctorDashboard();
});