# User Story Template

**Title:**
_As a [user role], I want [feature/goal], so that [reason]._

**Acceptance Criteria:**

1. [Criteria 1]
2. [Criteria 2]
3. [Criteria 3]

**Priority:** [High/Medium/Low]
**Story Points:** [Estimated Effort in Points]
**Notes:**

- [Additional information or edge cases]

## Admin User stories

1. Admin Login
   Title:
   As an admin, I want to log into the portal with my username and password, so that I can manage the platform securely.

Acceptance Criteria:

Admin enters valid credentials (username/password).

System authenticates and grants access to the AdminDashboard.

Invalid credentials display an error message.

Priority: High
Story Points: 3
Notes:
Include password encryption and session timeout.

2. Admin Logout
   Title:
   As an admin, I want to log out of the portal, so that I can protect system access when inactive.

Acceptance Criteria:

Clicking "Logout" ends the session.

System redirects to the login page.

Session tokens are invalidated.

Priority: High
Story Points: 2
Notes:
Audit logs should record logout events.

3. Add Doctors
   Title:
   As an admin, I want to add doctors to the portal, so that they can be assigned to patients.

Acceptance Criteria:

Admin fills a form with doctor details (name, specialty, contact).

System validates and saves the data to MySQL via JPA.

Confirmation message is displayed.

Priority: Medium
Story Points: 5
Notes:
Prevent duplicate entries (e.g., same email/license number).

4. Delete Doctor Profiles
   Title:
   As an admin, I want to delete a doctor’s profile, so that outdated or inactive accounts are removed.

Acceptance Criteria:

Admin selects a doctor from a list.

System prompts for confirmation before deletion.

Doctor’s data is removed from MySQL, and dependent records (e.g., appointments) are handled (archive or cascade).

Priority: Medium
Story Points: 5
Notes:
Consider soft deletion for audit trails.

5. Generate Monthly Appointment Reports
   Title:
   As an admin, I want to run a stored procedure in MySQL CLI to get the number of appointments per month, so that I can track usage statistics.

Acceptance Criteria:

Admin executes a predefined stored procedure via CLI.

System returns a table of appointments grouped by month/year.

Results are exportable (e.g., CSV).

Priority: Low
Story Points: 8
Notes:
Stored procedure must aggregate data from the Appointment JPA entity.

## Patient user stories

1. Browse Doctors (Public)
   Title:
   As a patient, I want to view a list of doctors without logging in, so I can explore options before registering.

Acceptance Criteria:

Public page displays doctor names, specialties, and availability.

No authentication required.

Data is fetched from MySQL via REST API (Doctor JPA entity).

Priority: High
Story Points: 3
Notes:
Pagination or filters (e.g., by specialty) improve usability.

2. Patient Signup
   Title:
   As a patient, I want to sign up using my email and password, so I can book appointments.

Acceptance Criteria:

Form collects email, password, and basic details (name, phone).

System validates email uniqueness and password strength.

On success:

Saves data to MySQL (Patient JPA entity).

Sends confirmation email.

Priority: High
Story Points: 5
Notes:
Integrate email service (e.g., SendGrid).

3. Patient Login
   Title:
   As a patient, I want to log into the portal, so I can manage my bookings.

Acceptance Criteria:

Patient enters registered email/password.

System authenticates and redirects to PatientDashboard.

Invalid attempts show error messages.

Priority: High
Story Points: 3
Notes:
Use JWT or session cookies for security.

4. Patient Logout
   Title:
   As a patient, I want to log out, so my account remains secure when inactive.

Acceptance Criteria:

Clicking "Logout" ends the session.

Redirects to the public doctor list page.

Session tokens are invalidated.

Priority: Medium
Story Points: 2
Notes:
Audit logout events in logs.

5. Book Appointment
   Title:
   As a patient, I want to book an hour-long appointment with a doctor, so I can consult them.

Acceptance Criteria:

Patient selects a doctor and available slot (REST API fetches slots).

System checks for conflicts and saves to MySQL (Appointment JPA entity).

Confirmation email/SMS is sent.

Priority: High
Story Points: 8
Notes:
Integrate a calendar/date-picker UI.

6. View Upcoming Appointments
   Title:
   As a patient, I want to view my upcoming appointments, so I can prepare accordingly.

Acceptance Criteria:

PatientDashboard displays appointments (date, time, doctor) via REST API.

Data is filtered to show only future appointments.

Option to cancel/reschedule exists.

Priority: Medium
Story Points: 5
Notes:
Use pagination if >10 appointments.

## Doctor user stories

1. Doctor Login
   Title:
   As a doctor, I want to log into the portal, so I can manage my appointments securely.

Acceptance Criteria:

Doctor enters valid credentials (email/password).

System authenticates and grants access to DoctorDashboard.

Invalid attempts display an error message.

Priority: High
Story Points: 3
Notes:
Use JWT or session-based authentication.

2. Doctor Logout
   Title:
   As a doctor, I want to log out of the portal, so my data remains protected.

Acceptance Criteria:

Clicking "Logout" ends the session.

System redirects to the login page.

Session tokens are invalidated.

Priority: Medium
Story Points: 2
Notes:
Log logout events for security audits.

3. View Appointment Calendar
   Title:
   As a doctor, I want to view my appointment calendar, so I can stay organized.

Acceptance Criteria:

DoctorDashboard displays daily/weekly appointments (time, patient name).

Data is fetched from MySQL (Appointment JPA entity) via REST API.

Supports filtering by date range.

Priority: High
Story Points: 5
Notes:
Integrate a calendar UI (e.g., FullCalendar.js).

4. Mark Unavailability
   Title:
   As a doctor, I want to mark my unavailability, so patients only see available slots.

Acceptance Criteria:

Doctor selects dates/times they are unavailable.

System updates the doctor’s schedule in MySQL (Availability table).

Appointment booking API excludes blocked slots.

Priority: Medium
Story Points: 8
Notes:
Recurring unavailability (e.g., every Monday) should be supported.

5. Update Profile
   Title:
   As a doctor, I want to update my profile (specialization, contact info), so patients have accurate information.

Acceptance Criteria:

Edit form pre-fills current data (from Doctor JPA entity).

Validates new data (e.g., valid phone number).

Saves changes to MySQL and reflects updates system-wide.

Priority: Medium
Story Points: 5
Notes:
Changes should sync to the public doctor list.

6. View Patient Details
   Title:
   As a doctor, I want to view patient details for upcoming appointments, so I can prepare.

Acceptance Criteria:

Clicking an appointment shows patient info (name, medical history).

Data is fetched from MySQL (Patient and Appointment entities).

Sensitive data is masked (e.g., partial phone numbers).

Priority: High
Story Points: 5
Notes:
Ensure compliance with healthcare data regulations (e.g., HIPAA/GDPR).
