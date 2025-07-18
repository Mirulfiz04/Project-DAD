# Project-DAD

<img width="256" height="151" alt="image_2025-07-18_202703266" src="https://github.com/user-attachments/assets/88a1de1e-bb60-4797-92b8-9e8e8c734f78" />

FACULTY OF INFORMATION TECHNLOGY AND COMMUNICATIONS

BITP - DISTRIBUTED APPLICATION 

2 BITS (S1G1)

GROUP PROJECT

DR. MOHD HARIZ NAIM

<img width="637" height="183" alt="image_2025-07-18_202932714" src="https://github.com/user-attachments/assets/c11b1dc8-0576-4699-9575-4a5e833293d5" />


INTRODUCTION
The Clinic Management System (CMS) is developed to address the operational challenges facedby small to medium-sized clinics. Traditionally, clinics rely on manual processes for patientregistration, appointment booking, medical recordkeeping, and billing. This often results ininefficiencies, human errors, and increased workload on clinic staff.The CMS automates and streamlines these processes by providing a centralized digital platform.It improves data accuracy, reduces paperwork, enhances patient service, and allows easier dataaccess for authorized personnel. The system is designed with scalability and user-friendliness inmind, ensuring it can adapt to the growing needs of healthcare facilities.The Clinic Management System (CMS) is an end-to-end digital healthcare platform designed tomodernize medical practice operations. Built with Java Swing for the frontend and a RESTful Javabackend with MySQL/JSON data persistence, this system solves critical inefficiencies intraditional clinic workflows.

PROBLEM STATEMENTS

1. Manual Appointment Scheduling
   Traditional paper-based appointment booking systems create numerous operationalchallenges for medical clinics. Using physical appointment books or basic computerspreadsheets often leads to scheduling conflicts when multiple staff members try to bookpatients simultaneously. Without a centralized system, double bookings frequently occurwhere two patients are accidentally scheduled for the same time slot. This createsfrustration for both patients and staff when appointment times need to be adjusted at thelast minute.
   Paper systems also make it difficult to track appointment attendance. Clinics have noeffective way to follow up with patients who miss their appointments, as there's noautomated reminder system. Staff must manually call each patient beforehand, whichconsumes considerable administrative time. Rescheduling appointments becomes a tediousprocess of erasing and rewriting entries in the appointment book, often leading to messy,hard-to-read schedules.

2. Fragmented Patient Records
   Physical paper records create significant inefficiencies in clinical workflows. When apatient arrives for an appointment, staff must locate their paper chart from storage, whichmay be filed among thousands of other records. During busy periods, this chart retrievalprocess can cause delays in seeing patients. Important health information is often scatteredacross different forms, sticky notes, and test result attachments within the paper file,making it difficult for providers to quickly find critical information.
   Paper records are vulnerable to damage from spills, tears, or normal wear-and-tear. Theyalso present security risks, as paper files can be lost, stolen, or viewed by unauthorizedpersonnel. When patients see multiple providers within a practice, their information maybe recorded differently in separate charts, leading to inconsistent medical histories.

3. Inefficient Clinic Workflows
   Traditional clinic operations require excessive manual coordination between different staffmembers and departments. Front desk personnel spend considerable time physicallyrouting paper charts to the appropriate providers and treatment rooms. Nurses and medicalassistants waste time searching for available equipment or preparing paperwork betweenpatient visits.
   The prescription process is particularly cumbersome, requiring providers to hand-writemedication orders that front desk staff must then call or fax to pharmacies. Generatingreports for practice analysis or patient referrals involves manually compiling data frommultiple sources, often resulting in incomplete or inconsistent information.

OBJECTIVES
1. To develop a real-time digital scheduling system with automated conflict detectionand multi-channel patient notifications. If a patient tries to book a slot that's alreadytaken, the system immediately suggests the next available time. Once confirmed, thesystem sends automated reminders through SMS, email, or app notifications.
2. To establish secure electronic health records with encrypted data storage and rolebased access controls. A doctor can view full medical histories and see appointmentrelated information.
3. To create structured clinical documentation tools with temporal visualization oftreatment histories. A visual timeline showing all the patient’s visits, treatmentsadministered, and lab results over months, aiding better clinical decision-making.
4. To create a secure and safe to use medical platform. All users will need proper authentication to access the application.

COMMERCIAL VALUE & THIRD-PARTY INTEGRATION
1. Market Potential
   Target :
   Private Clinics: Small-to-medium practices seeking affordable digital transformation.
   Multi-Specialty Hospitals: Large facilities needing centralized patient management.
   Telehealth Providers: Platforms requiring appointment scheduling and EHR integration.

3. Third-party integration
   1) Database System: MySQL (Relational Database)
      Justification:
      ACID Compliance: MySQL ensures Atomicity, Consistency, Isolation, and
      Durability, which are essential when dealing with medical data (e.g., patient records,
      appointments, prescriptions) to prevent data corruption or loss.
      
      Implementation:
      DBConnection.java
      This Java class is responsible for establishing a connection to the MySQL database.
      
      It likely includes methods for:
      • Connecting to the DB using JDBC
      • Executing SQL queries ( SELECT, INSERT, UPDATE, DELETE)
      • Managing prepared statements and result sets
      • Ensures reusability and separation of concerns by centralizing DB access logic in one place

   3) JSON Data Sync: org.json Library
      Justification:
      JSON (JavaScript Object Notation) is a lightweight format that is easy to parse and generate.
      It is useful for offline storage, data exchange, or syncing settings/configurations.
      Great for mobile apps or browser-based apps that need to sync with server data.
      
      Implementation:
      JSONHandler.java
      • Manages reading from and writing to JSON files.
      • Handles two-way synchronization, such as:
      • Exporting DB data into a JSON file (backup or sync to client)
      • Importing JSON data into the database (restore or sync from client)
      • Useful in distributed systems, remote backups, or interoperability with non-Java systems.

   4) HTTP Server: com.sun.net.httpserver
      Justification:
      A native Java HTTP server (no need for external web server like Apache or Tomcat).
      Ideal for lightweight RESTful APIs, internal tools, or simple services.
      
      Implementation:
      ClinicRestServer.java
      • Defines the REST API endpoints such as:
      • GET /patients → fetch all patients
      • POST /appointments → add an appointment
      • PUT /prescriptions/{id} → update prescription
      • Handles routing and response formatting ( JSON responses).
      • Useful for client-server communication (a front-end app or mobile app consuming this API).

SYSTEM ARCHITECTURE

<img width="614" height="474" alt="image_2025-07-18_204016423" src="https://github.com/user-attachments/assets/f60b138e-741e-48c3-8f48-7d2d9aa15f58" />

Sequence diagram

User login

<img width="605" height="390" alt="image_2025-07-18_204115031" src="https://github.com/user-attachments/assets/bbd9ba33-0a29-4967-ace0-124f3c3129df" />

Patient View Appointment

<img width="630" height="365" alt="image" src="https://github.com/user-attachments/assets/14704225-37ce-419a-a2f8-134879a49f6e" />

Doctor Update Treatment

<img width="641" height="352" alt="image" src="https://github.com/user-attachments/assets/06307e1d-44bb-4255-a8fc-cd31c8cf6814" />

BACKEND APPLICATION

<img width="601" height="780" alt="image" src="https://github.com/user-attachments/assets/9f192fbe-a455-47fd-82f7-23528b6e05ce" />
<img width="608" height="812" alt="image" src="https://github.com/user-attachments/assets/8de00a88-20fb-4354-b874-bb3ef732267e" />
<img width="646" height="536" alt="image" src="https://github.com/user-attachments/assets/dff8f67a-3f4a-41ea-b480-d15f43c06efc" />


API documentation
(1) A list of all API endpoints
(2) The HTTP method for each endpoint
(3) Required request parameters, headers, and body formats .
(4) Example success and error responses.
(5) Security: Detail the security measures implemented.

Appointment Management

<img width="599" height="719" alt="image" src="https://github.com/user-attachments/assets/18e6e5e7-4714-40e9-93e0-4724da9e1aee" />


Treatment Management

<img width="597" height="449" alt="image" src="https://github.com/user-attachments/assets/898afe19-c925-4af3-9725-94a420a91efb" />


Patient Data

<img width="599" height="299" alt="image" src="https://github.com/user-attachments/assets/d3787e44-0daa-456f-b396-fad0cec1ab09" />


System Operation

<img width="601" height="298" alt="image" src="https://github.com/user-attachments/assets/ac021fce-f15f-4659-9018-891e7887868b" />


JSON examples
1) Appointment management:
GET /api/appointments

Request:
GET /api/appointments?doctorId=5&date=2025-07-20
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Success Response (200):
{
 "data": [
 {
 "id": 1023,
 "patientId": 101,
 "patientName": "John Doe",
 "doctorId": 5,
 "date": "2025-07-20",
 "time": "09:30:00",
 "status": "confirmed",
 "notes": "Annual checkup"
 }
 ]
}
Error Response (401):
{
 "error": "Unauthorized",
 "message": "Invalid or expired token",
 "timestamp": "2025-07-20T08:15:00Z"
}
POST /api/appointments
Request:
POST /api/appointments
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
{
 "patientId": 101,
 "doctorId": 5,
 "date": "2025-07-21",
 "time": "14:00:00",
 "notes": "Follow-up visit"
}
Success Response (201):
{
 "id": 1024,
 "status": "confirmed",
 "location": "/api/appointments/1024",
 "confirmationNumber": "CLINIC-2025-1024"
}
Error Response (409):
{
 "error": "Conflict",
 "message": "Doctor not available at requested time",
 "nextAvailable": "2025-07-21T14:30:00"
}

2) Treatment management
POST /api/treatments
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
{
 "appointmentId": 1024,
 "diagnosis": "Hypertension (I10)",
 "treatmentType": "Medication",
 "medication": "Lisinopril 10mg",
 "dosage": "Once daily",
 "notes": "Monitor blood pressure weekly"
}
Success Response (201):
{
 "id": 789,
 "appointmentId": 1024,
 "diagnosisCode": "I10",
 "medications": [
 {
 "name": "Lisinopril",
 "strength": "10mg",
 "refills": 3,
 "instructions": "Take once daily in the morning"
 }
 ],
 "followUpDate": "2025-08-21"
}
Error Response (404):
{
 "error": "Not Found",
 "message": "Appointment 1024 not found",
 "suggestedAppointments": [1023, 1025]
}

3) Patient data
GET /api/patients/101/records
Request:
GET /api/patients/101/records?limit=5
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Success Response (200):
{
 "patientId": 101,
 "name": "John Doe",
 "records": [
 {
 "date": "2025-07-20",
 "doctor": "Dr. Smith",
 "diagnosis": "Hypertension",
 "treatment": "Prescribed Lisinopril"
 },
 {
 "date": "2025-06-15",
 "doctor": "Dr. Johnson",
 "diagnosis": "Annual physical",
 "treatment": "Lab tests ordered"
 }
 ]
}
Error Response (403):
{
 "error": "Forbidden",
 "message": "You don't have permission to access these records"
}

4) System operation
GET /api/health
Request:
GET /api/health
Success Response (200):
{
 "status": "healthy",
 "components": {
 "database": {
 "status": "connected",
 "latency": "12ms"
 },
 "memory": {
 "used": "45%",
 "total": "16GB"
 }
 },
 "uptime": "5d 7h 22m"
}
Error Response (503):
{
 "status": "unhealthy",
 "errors": [
 {
 "component": "database",
 "error": "Connection timeout",
 "timestamp": "2025-07-20T09:45:00Z"
 }
 ],
 "maintenanceWindow": "00:00-02:00 UTC"
}

Security Implementation
1. Authentication
   Method: Direct database credential validation
   Location: LoginGUI.java
   // Plaintext credential verificationString sql = "SELECT id FROM patients WHERE email = ? AND password = ?";
   try (PreparedStatement stmt = conn.prepareStatement(sql)) {
    stmt.setString(1, email);
   stmt.setString(2, password); // Stored and compared in plaintext
   ResultSet rs = stmt.executeQuery();
    return rs.next() ? rs.getInt("id") : -1;
   }
   
3. Database Security
   Location: DBConnection.java
   // SSL-enabled connection
   String url = "jdbc:mysql://localhost:3306/clinicdb?useSSL=true";
   Protections:
   • Forces TLS for MySQL connections
   • Uses parameterized queries to prevent SQL injection

4. Input Sanitization
   Location: Various handlers
   // Basic string cleaning
   String cleanInput = input.replaceAll("[^a-zA-Z0-9]", "");

5. JSON Data Security
   Location: JSONHandler.java
   // No encryption for JSON files
   Files.write(Paths.get("patients.json"), jsonArray.toString().getBytes());

FRONTEND APPLICATION
Basically, there are three GUI in the project, which is LoginGUI, DocterGUI and PatientGUI.

1. Doctor GUI
   Purpose: Designed for medical professionals to manage clinical operations.
   Key functions:
   • Viewing/filtering appointments by status (confirmed, pending, completed).
   • Recording diagnoses, treatments, and medications.
   • Updating appointment statuses.
   • Accessing/exporting patient medical records.
   Target User
   Medical professionals (doctors) in a clinic.
   Technology Stack

   <img width="656" height="156" alt="image" src="https://github.com/user-attachments/assets/2e955473-92fe-4442-b6c3-ca779787854c" />


   API INTEGRATION
   Communicates with backend REST API at http://localhost:8000:
   1) Data Retrieval
      GET /api/appointments?doctorId={id}: Loads doctor's appointments
      GET /api/all-records?search={term}: Searches patient records

   2) Data Submission
      POST /api/treatments: Submits diagnosis/treatment details
      PUT /api/appointments/{id}/status: Updates appointment status

   3) Data Format
      Requests: JSON payloads
      Responses: JSON arrays (appointments) or objects (patient details)

   4) Authentication
      Uses doctorId from login session in all API requests

2. Patient GUI
   Purpose: Enables patients to manage personal healthcare interactions.
   Key functions:
   • Booking/rescheduling/canceling appointments
   • Viewing medical records and appointment history
   • Exporting health data
   • Locating pharmacies via Google Maps
   Target User:
   Patients registered with the clinic.
   Technology Stack

   
   <img width="636" height="140" alt="image" src="https://github.com/user-attachments/assets/7df3e05e-e599-4b1d-8475-9235b5e45e7e" />


   API Integration:
   Communicates with backend REST API at http://localhost:8000:
   1) Appointment Management
      POST /api/appointments: Books new appointments
      PUT /api/appointments/{id}/reschedule: Changes appointment time
      DELETE /api/appointments/{id}: Cancels appointments
   2) Data Access
      GET /api/records?patientId={id}: Retrieves medical records
      GET /api/patients?id={id}: Fetches patient profile
   3) Profile Updates
      PUT /api/patients/{id}/profile: Saves profile changes
   4) Authentication
      Uses patientId from login session in all requests

3. Login GUI
   Purpose: Central authentication gateway for both patients and doctors.
   Features:
   • Role-based login (Patient/Doctor)
   • Secure password field with visibility toggle
   • Input validation (email format)
   • Redirect to appropriate portal
   Target User:
   Patient and Medical Staff
   Technology Stack


   <img width="639" height="128" alt="image" src="https://github.com/user-attachments/assets/fd0701cc-53c3-4552-8f58-e2ee3871ccac" />


   API INTEGRATION
   Direct Databasse Access:
   1) Authentication Flow
      Runs SQL queries against patients/doctors tables:
      SELECT id FROM patients WHERE email=? AND password=?
      SELECT id FROM doctors WHERE email=? AND password=?
   2) Session Initialization
      Launches PatientGUI or DoctorGUI with user ID on success
   3) Security
      Uses parameterized SQL queries to prevent injection

Key Architectural Notes
1) Shared Backend
Both portals consume the same REST API (http://localhost:8000) but access role-specific
endpoints.
2) Stateless Design
User IDs (doctorId/patientId) are passed in all API requests instead of sessions.
3) Error Handling
API errors shown in Swing JOptionPane dialogs
HTTP status codes (200, 201, 400) drive UI feedback

Database Design 
Entity Relationship Diagram (ERD)

<img width="712" height="800" alt="Screenshot 2025-07-18 192000" src="https://github.com/user-attachments/assets/3b1f2dc5-c5ad-4160-8aca-641f7f5068d4" />


Schema Justification

   Normalization:
   • The database schema is designed with a degree of normalization to reduce data
   redundancy and improve data integrity. Each entity (Doctors, Patients, Appointments,
   Treatments) has its own table.
   Primary Keys:
   • Each table has a unique id column serving as its primary key, ensuring that each record
   can be uniquely identified.
   Foreign Keys:
   • patient_id in appointments links to patients.id, establishing that an appointment must be
   associated with an existing patient.
   • doctor_id in appointments links to doctors.id, establishing that an appointment must be
   associated with an existing doctor.
   • appointment_id in treatments links to appointments.id, ensuring that a treatment record
   corresponds to a specific appointment.
   Data Types:
   • Appropriate data types are used for each column (e.g., INT for IDs, VARCHAR for
   names and emails, DATE for dates, TIME for times, TEXT for longer descriptions like
   notes and diagnosis, ENUM for predefined statuses).
   Constraints:
   • NOT NULL constraints ensure that essential information (like names, emails, and core
   appointment details) is always present.
   • UNIQUE constraints on email in both doctors and patients tables prevent duplicate email
   addresses, which are crucial for user authentication.
   • The status ENUM in appointments provides a controlled set of values for appointment
   states, ensuring consistency.
   Separation of Concerns:
   Doctors and Patients:
   • Separate tables for doctors and patients allow for distinct attributes and roles within the
   system.
   Appointments:
   • The appointments table acts as a central linking entity between patients and doctors,
   capturing the scheduling aspect.
   Treatments:
   • The treatments table is separate from appointments to store detailed medical information
   that is only relevant once an appointment has occurred and a diagnosis/treatment has
   been made. This keeps the appointments table cleaner and focused on scheduling

Business Logic
  Use Case Diagram


  <img width="653" height="814" alt="Screenshot 2025-07-18 194434" src="https://github.com/user-attachments/assets/16692819-387d-4e21-925f-559cc27ecef3" />


  Flowchart

  User Registration
  

  <img width="393" height="497" alt="Screenshot 2025-07-18 194530" src="https://github.com/user-attachments/assets/469a13c3-ef78-4c7d-81fe-713d4fec35f3" />


  User Login


  <img width="395" height="498" alt="Screenshot 2025-07-18 194724" src="https://github.com/user-attachments/assets/cae1370c-cfee-454c-ada7-dd9894fca7e8" />


Doctor Treatment


<img width="218" height="674" alt="Screenshot 2025-07-18 194840" src="https://github.com/user-attachments/assets/f3e5c37e-542c-495a-ac79-9bea36e392e7" />



Patient Appointment Management


<img width="469" height="625" alt="Screenshot 2025-07-18 195039" src="https://github.com/user-attachments/assets/16e56e90-ad4c-4862-9c4f-ee3eed212e97" />



Doctor Record Patient


<img width="381" height="562" alt="Screenshot 2025-07-18 195103" src="https://github.com/user-attachments/assets/e84d0467-1794-42f9-8433-f3f54678ae98" />



Patient Export Medication


<img width="201" height="536" alt="Screenshot 2025-07-18 195202" src="https://github.com/user-attachments/assets/16dbfe02-9966-4ff2-970f-989072571df8" />


Patient Open Map


<img width="181" height="400" alt="image" src="https://github.com/user-attachments/assets/846a09f9-0c5a-476c-ab2f-5e49e824e843" />


Patient Edit Profile


<img width="284" height="544" alt="Screenshot 2025-07-18 195246" src="https://github.com/user-attachments/assets/edfc3a1c-a4e0-42fc-b192-a8f37410e255" />

Data Validation
1. Frontend Validation (GUI Level)
   • Empty Field Checks:
   • Login: Ensures "Email" and "Password" fields are not empty before attempting
   authentication.
   • Appointment Booking: Ensures a doctor is selected.
   • Treatment Submission: Ensures "Diagnosis" field is not empty.
   Format Validation:
   • Login: Checks if the entered "Email" adheres to a standard email format (e.g., using
   a regex pattern).
   • Logical Checks (Pre-API Call):
   • Appointment Reschedule/Cancel: Prevents rescheduling or canceling
   appointments that are already 'completed' or 'cancelled'.
   • Appointment Booking: Displays a message if no doctors are loaded.
2. Backend Validation (REST Server/DBConnection Level)
   • Missing Parameter Checks (ClinicRestServer Handlers):
   • AppointmentsHandler (GET): Checks for the presence of patientId or doctorId
   parameters.
   • AppointmentsHandler (POST): Validates that patientId, doctorId, date, and time are
   present in the request body.
   • TreatmentsHandler (POST): Validates that appointmentId, diagnosis,
   treatmentType, and medication are present.
   • RescheduleAppointmentHandler: Checks for valid appointmentId in the path and
   date, time in the request body.
   • AppointmentStatusHandler: Checks for valid appointmentId in the path and status
   in the request body.
   
   Data Integrity (DBConnection):
   • Foreign Key Constraints: The database schema enforces that patient_id, doctor_id,
   and appointment_id must refer to existing records in their respective parent tables.
   This prevents orphaned records.
   • Unique Constraints: The email columns in doctors and patients tables have
   UNIQUE constraints, preventing the creation of multiple user accounts with the
   same email address.
  • ENUM Type: The status column in appointments only allows predefined values
  ('pending', 'confirmed', 'completed', 'cancelled', 'rescheduled'), ensuring data
  consistency.
  • ON DUPLICATE KEY UPDATE (during initialization/sync): When populating the
  database from JSON files, this SQL clause is used to prevent inserting duplicate
  records based on unique keys (like email for doctors/patients, or a combination of
  patient/doctor/date/time for appointments), instead updating existing ones.

  Business Logic Validation (DBConnection):
  • Appointment Conflict Check (checkAppointmentConflict): Before booking a new
  appointment, the backend explicitly checks if the selected doctor already has an
  appointment at the exact date and time that is not 'cancelled' or 'completed'. This
  prevents double-booking.
  • Treatment Existence Check (insertTreatment): Before inserting a new treatment, the
  backend checks if a treatment record already exists for that appointment_id. If it
  does, it updates the existing record instead of creating a new one, ensuring a oneto-one relationship between completed appointments and treatments.
  • Status Updates: The insertTreatment method automatically updates the
  appointment status to 'completed' after a treatment is recorded, reflecting the
  business process.
  • Reschedule/Cancel Logic: While some checks are on the frontend, the backend
  methods (rescheduleAppointment, cancelAppointment,
  updateAppointmentStatus) are designed to handle the actual database updates,
  ensuring that the status changes are correctly applied and persisted.
  • Error Handling: Both frontend and backend include robust error handling (try-catch
  blocks, JOptionPane messages, HTTP status codes) to inform users and developers
  about issues, whether they are network errors, database errors, or validation
  failures.
 


