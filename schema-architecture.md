## Architecture summary

This Spring Boot application follows a layered architecture combining MVC and REST controllers to handle different types of requests. The Admin and Doctor dashboards are rendered using Thymeleaf templates, which are served by MVC controllers. For other functionalities like appointments, patient dashboards, and patient records, the application exposes REST APIs that return JSON responses.

The backend logic is structured with a clear separation of concerns: controllers handle incoming requests, the service layer contains business logic, and repositories manage data access. The application integrates two databases—MySQL and MongoDB—each serving distinct purposes. MySQL, accessed via JPA repositories, stores structured relational data such as patients, doctors, appointments, and admins. MongoDB, on the other hand, is used for document-based storage, specifically for prescriptions, leveraging its flexibility for unstructured data.

This hybrid approach ensures efficient data management while maintaining scalability and modularity across the application.

## Numbered flow of data and control

```bash
1. **User Interaction**: A user accesses either the **AdminDashboard**, **DoctorDashboard**, or interacts with modules like **Appointments** or **PatientRecord** via the web interface.
2. **Request Routing**:
   - For dashboard pages (Thymeleaf), the request is handled by **MVC controllers**.
   - For API-driven modules (e.g., appointments), the request is processed by **REST controllers** returning JSON.
3. **Service Layer**: Controllers delegate business logic to the **Service Layer**, which orchestrates operations like validation or data aggregation.
4. **Database Interaction**:
   - For structured data (e.g., patients, doctors), the service calls **MySQL Repositories** using **JPA Entities**.
   - For document-based data (e.g., prescriptions), it uses **MongoDB Repositories** with **MongoDB Models**.
5. **Data Retrieval/Update**:
   - MySQL repositories interact with the **MySQL Database** for relational data.
   - MongoDB repositories access the **MongoDB Database** for flexible document storage.
6. **Response Formation**: The service layer processes retrieved data and returns it to the controller.
7. **User Feedback**:
   - MVC controllers render Thymeleaf templates (dashboards).
   - REST controllers send JSON responses (APIs) back to the client.
```
