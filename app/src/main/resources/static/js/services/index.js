import { openModal } from '../components/modals.js';
import { API_BASE_URL } from '../config/config.js';

const ADMIN_API = API_BASE_URL + '/admin';
const DOCTOR_API = API_BASE_URL + '/doctor/login';

// Set up button event listeners when page loads
window.onload = function () {
    // Admin login button
    const adminBtn = document.getElementById('adminLogin');
    if (adminBtn) {
        adminBtn.addEventListener('click', () => {
            openModal('adminLogin');
        });
    }

    // Doctor login button
    const doctorBtn = document.getElementById('doctorLogin');
    if (doctorBtn) {
        doctorBtn.addEventListener('click', () => {
            openModal('doctorLogin');
        });
    }
};

// Admin login handler
window.adminLoginHandler = async function() {
    try {
        const username = document.getElementById('adminUsername').value;
        const password = document.getElementById('adminPassword').value;

        if (!username || !password) {
            alert('Please enter both username and password');
            return;
        }

        const admin = { username, password };

        const response = await fetch(ADMIN_API, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(admin)
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('token', data.token);
            selectRole('admin');
        } else {
            alert('Invalid admin credentials!');
        }
    } catch (error) {
        console.error('Admin login error:', error);
        alert('An error occurred during admin login');
    }
};

// Doctor login handler
window.doctorLoginHandler = async function() {
    try {
        const email = document.getElementById('doctorEmail').value;
        const password = document.getElementById('doctorPassword').value;

        if (!email || !password) {
            alert('Please enter both email and password');
            return;
        }

        const doctor = { email, password };

        const response = await fetch(DOCTOR_API, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(doctor)
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('token', data.token);
            selectRole('doctor');
        } else {
            alert('Invalid doctor credentials!');
        }
    } catch (error) {
        console.error('Doctor login error:', error);
        alert('An error occurred during doctor login');
    }
};

// Helper function to set role and redirect
function selectRole(role) {
    localStorage.setItem('userRole', role);
    window.location.href = role === 'admin' ? '/admin-dashboard.html' : '/doctor-dashboard.html';
}