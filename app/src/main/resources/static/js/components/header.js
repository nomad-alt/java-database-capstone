/**
 * Renders the header based on user role and login state
 */
function renderHeader() {
    const headerDiv = document.getElementById("header");
    
    // Clear header on homepage
    if (window.location.pathname.endsWith("/")) {
        localStorage.removeItem("userRole");
        localStorage.removeItem("token");
        headerDiv.innerHTML = `
            <header class="header">
                <div class="logo-section">
                    <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
                    <span class="logo-title">Hospital CMS</span>
                </div>
            </header>`;
        return;
    }

    const role = localStorage.getItem("userRole");
    const token = localStorage.getItem("token");
    
    // Initialize header content with logo section
    let headerContent = `
        <header class="header">
            <div class="logo-section">
                <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
                <span class="logo-title">Hospital CMS</span>
            </div>
            <nav class="nav-links">`;

    // Check for invalid session
    if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
        localStorage.removeItem("userRole");
        alert("Session expired or invalid login. Please log in again.");
        window.location.href = "/";
        return;
    }

    // Add role-specific navigation links
    if (role === "admin") {
        headerContent += `
            <button id="addDocBtn" class="nav-btn">
                <i class="fas fa-user-plus"></i> Add Doctor
            </button>
            <a href="#" class="nav-link" id="logoutBtn">
                <i class="fas fa-sign-out-alt"></i> Logout
            </a>`;
    } 
    else if (role === "doctor") {
        headerContent += `
            <a href="/pages/doctorDashboard.html" class="nav-link">
                <i class="fas fa-home"></i> Home
            </a>
            <a href="#" class="nav-link" id="logoutBtn">
                <i class="fas fa-sign-out-alt"></i> Logout
            </a>`;
    } 
    else if (role === "patient") {
        headerContent += `
            <button id="patientLoginBtn" class="nav-btn">
                <i class="fas fa-sign-in-alt"></i> Login
            </button>
            <button id="patientSignupBtn" class="nav-btn">
                <i class="fas fa-user-plus"></i> Sign Up
            </button>`;
    } 
    else if (role === "loggedPatient") {
        headerContent += `
            <a href="/pages/loggedPatientDashboard.html" class="nav-link">
                <i class="fas fa-home"></i> Home
            </a>
            <a href="/pages/patientAppointments.html" class="nav-link">
                <i class="fas fa-calendar-check"></i> Appointments
            </a>
            <a href="#" class="nav-link" id="logoutPatientBtn">
                <i class="fas fa-sign-out-alt"></i> Logout
            </a>`;
    } 
    else {
        // Default header for unauthenticated users on other pages
        headerContent += `
            <a href="/" class="nav-link">
                <i class="fas fa-home"></i> Home
            </a>
            <button id="roleSelectorBtn" class="nav-btn">
                <i class="fas fa-user-tag"></i> Select Role
            </button>`;
    }

    // Close header tags
    headerContent += `</nav></header>`;

    // Inject header content
    headerDiv.innerHTML = headerContent;

    // Attach event listeners
    attachHeaderButtonListeners();
}

/**
 * Attaches event listeners to header buttons
 */
function attachHeaderButtonListeners() {
    // Add Doctor button (Admin)
    const addDocBtn = document.getElementById("addDocBtn");
    if (addDocBtn) {
        addDocBtn.addEventListener("click", () => openModal("addDoctor"));
    }

    // Patient Login button
    const patientLoginBtn = document.getElementById("patientLoginBtn");
    if (patientLoginBtn) {
        patientLoginBtn.addEventListener("click", () => openModal("patientLogin"));
    }

    // Patient Signup button
    const patientSignupBtn = document.getElementById("patientSignupBtn");
    if (patientSignupBtn) {
        patientSignupBtn.addEventListener("click", () => openModal("patientSignup"));
    }

    // Role Selector button
    const roleSelectorBtn = document.getElementById("roleSelectorBtn");
    if (roleSelectorBtn) {
        roleSelectorBtn.addEventListener("click", openRoleSelectorModal);
    }

    // Logout buttons
    const logoutBtn = document.getElementById("logoutBtn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", logout);
    }

    const logoutPatientBtn = document.getElementById("logoutPatientBtn");
    if (logoutPatientBtn) {
        logoutPatientBtn.addEventListener("click", logoutPatient);
    }
}

/**
 * Logs out admin/doctor users
 */
function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("userRole");
    window.location.href = "/";
}

/**
 * Logs out patient users (retains patient role)
 */
function logoutPatient() {
    localStorage.removeItem("token");
    localStorage.setItem("userRole", "patient");
    window.location.href = "/pages/patientDashboard.html";
}

/**
 * Opens role selector modal
 */
function openRoleSelectorModal() {
    // Implementation would open a modal with role selection options
    console.log("Opening role selector modal");
    // Example implementation:
    // openModal('roleSelector');
}

// Render header when DOM is loaded
document.addEventListener("DOMContentLoaded", renderHeader);