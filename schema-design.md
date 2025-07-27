# Smart Clinic Database Design

## MySQL Database Design (Relational Data)

### New Table: admins

```bash
- id: INT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- email: VARCHAR(100), Unique, Not Null
- phone: VARCHAR(20)
- role: ENUM('super_admin', 'support_admin', 'billing_admin'), Not Null
- password_hash: VARCHAR(255), Not Null (bcrypt/scrypt)
- last_login: DATETIME
- is_active: BOOLEAN, Default True
- created_at: TIMESTAMP, Default CURRENT_TIMESTAMP
```

#### Justification:

- Role-Based Access Control (RBAC): role column enforces permission tiers (e.g., super_admin can delete doctors).
- Security: Stores only password hashes (never plaintext).
- Soft Delete: is_active allows deactivation without data loss.

### Table: patients

```bash
- id: INT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- email: VARCHAR(100), Unique, Not Null
- phone: VARCHAR(20), Not Null
- date_of_birth: DATE, Not Null
- address: TEXT
- created_at: TIMESTAMP, Default CURRENT_TIMESTAMP
```

#### Notes:

- Email is unique to prevent duplicate accounts.
- Phone validation (format/country code) handled in application logic.

### Table: doctors

```bash
- id: INT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- email: VARCHAR(100), Unique, Not Null
- phone: VARCHAR(20), Not Null
- specialization: VARCHAR(100), Not Null
- license_number: VARCHAR(50), Unique, Not Null
- is_active: BOOLEAN, Default True
```

#### Notes:

- is_active soft-deletes doctors instead of physical deletion.
- license_number ensures no duplicates.

### Table: appointments

```bash
- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id), Not Null
- patient_id: INT, Foreign Key → patients(id), Not Null
- appointment_time: DATETIME, Not Null
- duration_minutes: INT, Default 60 (1 hour)
- status: ENUM('Scheduled', 'Completed', 'Cancelled', 'No-Show'), Default 'Scheduled'
- notes: TEXT
- modified_by_admin: INT, Nullable, Foreign Key → admins(id)
- modification_notes: TEXT (e.g., "Rescheduled by admin due to clinic holiday")
```

#### Notes:

- status uses ENUM for strict validation.
- doctor_id and patient_id cascade delete if referenced records are deleted (business decision).
- Constraint: Application logic prevents overlapping appointments for doctors.

### Table: doctor_availability

```bash
- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id), Not Null
- day_of_week: ENUM('Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'), Not Null
- start_time: TIME, Not Null
- end_time: TIME, Not Null
- is_recurring: BOOLEAN, Default True
- exception_date: DATE (NULL for recurring slots)
```

#### Notes:

- Supports recurring weekly slots (e.g., Mon 9AM-5PM) + one-time exceptions.
- Used to validate appointment booking requests.

### Table: payments

```bash
- id: INT, Primary Key, Auto Increment
- appointment_id: INT, Foreign Key → appointments(id), Not Null
- amount: DECIMAL(10,2), Not Null
- payment_method: ENUM('Credit Card', 'Insurance', 'Cash'), Not Null
- status: ENUM('Pending', 'Completed', 'Refunded'), Default 'Pending'
- transaction_id: VARCHAR(100) (for gateways like Stripe)
```

#### Notes:

- Ties payments to specific appointments.
- transaction_id stores third-party gateway references.

### Table: admin_audit_logs (For Security Compliance)

```bash
- id: INT, Primary Key, Auto Increment
- admin_id: INT, Foreign Key → admins(id), Not Null
- action: VARCHAR(50), Not Null (e.g., "DELETE_DOCTOR", "UPDATE_PATIENT")
- target_id: INT (ID of affected doctor/patient)
- ip_address: VARCHAR(45)
- timestamp: TIMESTAMP, Default CURRENT_TIMESTAMP
```

#### Why?

- Mandatory for HIPAA/GDPR compliance.
- Tracks all sensitive admin actions.

### Example Workflow Integration

1. Admin Deletes a Doctor:

- Sets doctors.is_active = False (soft delete).
- Records action in admin_audit_logs with action = "DEACTIVATE_DOCTOR".

2. Admin Modifies Appointment:

- Updates appointments table with modified_by_admin and notes.
- Appointment history remains intact for accountability.

#### Key Relationships

```bash
erDiagram
    admins ||--o{ admin_audit_logs : "logs"
    admins ||--o{ appointments : "modifies"
    admins {
        INT id PK
        VARCHAR(100) name
        VARCHAR(100) email
        ENUM role
    }
    admin_audit_logs {
        INT id PK
        INT admin_id FK
        VARCHAR(50) action
    }
```

#### Design Decisions:

- No Direct Patient/Doctor Links: Admins interact via logs and overrides, not ownership.
- Nullable Foreign Keys: Appointments can be modified by either doctors or admins.

## MongoDB Collection Design (Document-Based Data)

### Collection: prescriptions

```bash
{
  "_id": ObjectId("507f1f77bcf86cd799439011"),
  // Core References
  "appointment_id": 42,                         // MySQL appointments.id
  "patient_id": 101,                            // MySQL patients.id
  "doctor_id": 5,                               // MySQL doctors.id

  // Medical Content
  "issued_date": ISODate("2023-08-15T09:30:00Z"),
  "medications": [
    {
      "name": "Ibuprofen",
      "dosage": "400mg",
      "frequency": "Every 8 hours",
      "duration": "7 days",
      "instructions": "Take with food",
      "ndc_code": "0003-0105-01"               // National Drug Code for validation
    },
    {
      "name": "Amoxicillin",
      "dosage": "500mg",
      "frequency": "Twice daily",
      "duration": "10 days",
      "instructions": "Complete full course",
      "ndc_code": "0003-2465-05"
    }
  ],
  "diagnosis": {
    "icd11_code": "CA08.0",                    // Standardized diagnosis code
    "description": "Acute sinusitis"
  },

  // Audit & Security
  "access_control": [                          // Granular permissions
    { "user_id": 5, "role": "doctor", "permission": "edit" },
    { "user_id": 3, "role": "admin", "permission": "view" }
  ],
  "corrections": [
    {
      "admin_id": 3,                           // MySQL admins.id
      "reason": "Dosage typo fix",
      "field": "medications.0.dosage",
      "old_value": "50mg",
      "new_value": "500mg",
      "timestamp": ISODate("2023-08-20T08:15:00Z"),
      "approval_required": false               // For major changes
    }
  ],

  // Metadata
  "is_active": true,
  "attachments": [
    {
      "path": "s3://medical-reports/scan_20230815.pdf",
      "hash": "a1b2c3...",                    // File integrity check
      "uploaded_by": 5                         // Doctor who attached it
    }
  ],
  "metadata": {
    "created_by": "dr_smith@clinic.com",
    "last_updated": ISODate("2023-08-15T10:15:00Z"),
    "signature": "e-signature-abc123"         // Digital signature
  }
}
```

#### Design Choices:

- References vs Embedding: Stores only IDs for patients/doctors (not full objects) to avoid data duplication.
- Flexible Arrays: medications allows variable-length prescriptions.
- Attachments: AWS S3 paths for lab reports/scans (scalable storage).
- Metadata: Audit trail for compliance.

### Collection: patient_feedback

```bash
{
  "_id": ObjectId("607f1f77bcf86cd799439022"),
  "patient_id": 101, // Reference to MySQL
  "appointment_id": 42, // Optional reference
  "rating": 4,
  "comments": "Doctor was thorough but waiting time was long",
  "categories": ["professionalism", "wait_time"],
  "is_anonymous": false,
  "submitted_at": ISODate("2023-08-16T14:22:00Z"),
  "response": { // Clinic's reply
    "text": "Thank you! We're working to reduce wait times.",
    "responded_by": "admin_mary@clinic.com",
    "responded_at": ISODate("2023-08-17T09:05:00Z")
  }
}
```

#### Design Choices:

- Optional Fields: appointment_id not required (general feedback allowed).
- Tags: categories enables analytics.
- Nested Documents: response shows two-way communication.

### Collection: consultation_logs

```bash
{
  "_id": ObjectId("707f1f77bcf86cd799439033"),
  "patient_id": 101,
  "doctor_id": 5,
  "event_type": "video_consultation",
  "start_time": ISODate("2023-08-15T09:30:00Z"),
  "end_time": ISODate("2023-08-15T10:00:00Z"),
  "system_metadata": {
    "ip_address": "192.168.1.45",
    "device": "iPhone 13",
    "platform": "iOS"
  },
  "transcript_summary": "Discussed sinusitis symptoms...",
  "flags": ["prescription_issued", "followup_needed"]
}
```

#### Design Choices:

- Event Tracking: Timestamps for telemedicine compliance.
- System Data: Captures tech context for troubleshooting.
- Arrays: flags enables quick queries for common patterns.

### Schema Evolution Strategy:

#### Backward Compatibility: New fields are always optional initially.

```bash
// Future version can add:
"new_field": { "type": "String", "required": false }
```

#### Aliases: Rename fields without breaking old data:

```bash
// Application handles both:
const value = doc.new_field || doc.old_field;
```

#### Migration Scripts: Batch updates for critical changes.

#### Key Decisions:

- No Full Embedding: References to MySQL keep data consistent.
- Indexes: Ensure performance on patient_id, appointment_id.
- TTL Indexes: Auto-expire logs after 5 years (configurable).

## MongoDB Collection Additions

### Collection: chat_messages

```bash
{
  "_id": ObjectId("889f1f77bcf86cd799439044"),
  "thread_id": "pat101_dr5_202308", // Unique conversation ID
  "participants": [
    { "user_id": 101, "role": "patient", "name": "John Smith" },
    { "user_id": 5, "role": "doctor", "name": "Dr. Sarah Lee" }
  ],
  "messages": [
    {
      "sender_id": 101,
      "sender_role": "patient",
      "content": "Is it normal to have headaches after taking the medication?",
      "timestamp": ISODate("2023-08-18T14:30:00Z"),
      "read_by": [5], // Array of user_ids who read this
      "attachments": ["s3://chats/headache_photo.jpg"]
    },
    {
      "sender_id": 5,
      "sender_role": "doctor",
      "content": "Mild headaches can occur. Drink water and rest. If persistent >24h, contact me.",
      "timestamp": ISODate("2023-08-18T15:12:00Z"),
      "read_by": [101]
    }
  ],
  "status": "active", // active/archived
  "metadata": {
    "last_updated": ISODate("2023-08-18T15:12:00Z"),
    "auto_archive_date": ISODate("2023-09-18T00:00:00Z") // TTL for GDPR
  }
}
```

#### Design Choices:

- Thread-Based: All messages in a conversation are nested in one document for efficient retrieval.
- Read Receipts: read_by tracks who viewed each message.
- TTL Automation: auto_archive_date triggers cleanup via MongoDB TTL index.
- Hybrid References: Embeds minimal user info (name/role) while referencing user_id for consistency.

### Collection: file_storage

```bash
{
  "_id": ObjectId("99af1f77bcf86cd799439055"),
  "file_name": "xray_chest_20230815.jpg",
  "storage_path": "s3://medical-files/pat101/xray_chest_20230815.jpg",
  "uploaded_by": {
    "user_id": 5,
    "user_role": "doctor",
    "name": "Dr. Sarah Lee"
  },
  "patient_id": 101, // Reference to MySQL patients.id
  "appointment_id": 42, // Optional reference
  "file_type": "image/jpeg",
  "file_size": 4500000, // 4.5MB
  "description": "Chest X-ray showing mild inflammation",
  "tags": ["x-ray", "chest", "followup"],
  "access_control": [
    { "user_id": 5, "role": "doctor", "permission": "read/write" },
    { "user_id": 101, "role": "patient", "permission": "read" }
  ],
  "created_at": ISODate("2023-08-15T11:20:00Z"),
  "audit_log": [
    {
      "action": "download",
      "user_id": 101,
      "timestamp": ISODate("2023-08-16T09:15:00Z")
    }
  ]
}
```

#### Design Choices:

- Secure References: Stores only S3 paths (not raw files) with access control lists.
- Rich Metadata: tags enable search; file_type/size support UI previews.
- Audit Trail: Tracks all access for compliance (HIPAA/GDPR).
- Optional Appointment Link: Files can be tied to specific visits.

### Key Technical Considerations:

1. Indexes for Performance:

```bash
// For chat_messages
db.chat_messages.createIndex({ "thread_id": 1, "metadata.last_updated": -1 });

// For file_storage
db.file_storage.createIndex({ "patient_id": 1, "tags": 1 });
```

2. Schema Evolution Examples:

- Adding Encryption Flags:

```bash
// Future-proofing:
"is_encrypted": false // Default for backward compatibility
```

- Expanding Attachment Types:

```bash
"attachments": [
  {
    "type": "image",
    "url": "s3://...",
    "thumbnail": "s3://..._thumb.jpg"
  }
]
```

3. Data Lifecycle Management:

- Chat messages auto-archive after 1 year (GDPR).
- Files remain accessible but move to cold storage after 3 years.
