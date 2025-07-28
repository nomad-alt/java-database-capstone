import { deleteDoctor } from './doctorServices.js';
import { showBookingOverlay } from './loggedPatient.js';
import { getPatientDetails } from './patientServices.js';

export function createDoctorCard(doctor) {
  // Create the main card container
  const card = document.createElement("div");
  card.classList.add("doctor-card");

  // Get user role from localStorage
  const role = localStorage.getItem("userRole");

  // Create doctor info section
  const infoDiv = document.createElement("div");
  infoDiv.classList.add("doctor-info");

  // Doctor name
  const name = document.createElement("h3");
  name.textContent = doctor.name;

  // Doctor specialization
  const specialization = document.createElement("p");
  specialization.textContent = `Specialty: ${doctor.specialization}`;

  // Doctor email
  const email = document.createElement("p");
  email.textContent = `Email: ${doctor.email}`;

  // Doctor availability
  const availability = document.createElement("p");
  availability.textContent = `Available: ${doctor.availability.join(", ")}`;

  // Append all info elements
  infoDiv.appendChild(name);
  infoDiv.appendChild(specialization);
  infoDiv.appendChild(email);
  infoDiv.appendChild(availability);

  // Create actions container
  const actionsDiv = document.createElement("div");
  actionsDiv.classList.add("card-actions");

  // Conditionally add buttons based on user role
  if (role === 'admin') {
    // Delete button for admin
    const deleteBtn = document.createElement("button");
    deleteBtn.textContent = "Delete";
    deleteBtn.addEventListener('click', async () => {
      try {
        const token = localStorage.getItem('token');
        if (!token) {
          alert('Admin authorization required');
          return;
        }
        
        const confirmDelete = confirm(`Are you sure you want to delete Dr. ${doctor.name}?`);
        if (confirmDelete) {
          await deleteDoctor(doctor._id, token);
          card.remove();
          alert('Doctor deleted successfully');
        }
      } catch (error) {
        console.error('Error deleting doctor:', error);
        alert('Failed to delete doctor');
      }
    });
    actionsDiv.appendChild(deleteBtn);

  } else if (role === 'loggedPatient') {
    // Book appointment button for logged-in patients
    const bookBtn = document.createElement("button");
    bookBtn.textContent = "Book Appointment";
    bookBtn.addEventListener('click', async () => {
      try {
        const token = localStorage.getItem('token');
        if (!token) {
          alert('Please log in to book an appointment');
          return;
        }

        const patient = await getPatientDetails(token);
        showBookingOverlay(doctor, patient);
      } catch (error) {
        console.error('Error booking appointment:', error);
        alert('Failed to book appointment');
      }
    });
    actionsDiv.appendChild(bookBtn);

  } else {
    // Button for non-logged-in patients
    const bookBtn = document.createElement("button");
    bookBtn.textContent = "Book Now";
    bookBtn.addEventListener('click', () => {
      alert('Please log in to book an appointment');
    });
    actionsDiv.appendChild(bookBtn);
  }

  // Append all sections to the card
  card.appendChild(infoDiv);
  card.appendChild(actionsDiv);

  return card;
}