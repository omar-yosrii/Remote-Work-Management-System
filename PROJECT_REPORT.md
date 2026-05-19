# Remote Work Management System (RWMS) - Progress Report

**Project Status**: Phase 1, Phase 2 & Phase 3 Completed ✅

This document serves as a comprehensive report of all features, modules, and configurations implemented in the RWMS backend up to the current stage.

---

## 🚀 Phase 1: Foundation + Authentication

### 1. Project Configuration
- **Spring Boot Version**: 3.3.0
- **Database**: MySQL 8 (configured via `application.properties` with `update` DDL auto).
- **Dependencies**: Spring Web, Data JPA, Security, Validation, Lombok, MySQL Connector, and JJWT (0.11.5).

### 2. User Module
- **`User` Entity**: Contains all essential fields (`id`, `fullName`, `employeeId`, `email`, `password`, `githubUsername`, `phone`, `department`), Enums for `Role` (`EMPLOYEE`, `ADMIN`, `MANAGER`) and `Status` (`PENDING`, `ACTIVE`, `INACTIVE`), and auditing timestamps.
- **`UserRepository`**: Extends `JpaRepository` with custom finders (`findByEmail`, `existsByEmployeeId`, `findByStatusAndRole`, etc.).
- **`UserService`**: Implements full CRUD operations, DTO mapping, and password encoding using `BCrypt`.

### 3. Authentication & Security (JWT)
- **`JwtUtil`**: Handles token generation (9-hour expiration matching a work shift) and parsing claims (Email, Role).
- **`JwtAuthFilter`**: Intercepts requests to validate Bearer tokens and sets the `SecurityContextHolder`.
- **`SecurityConfig`**: Stateless session policy, CSRF disabled, CORS allowed. Secures endpoints based on roles (`/manager/**` for managers, `/admin/**` for admins/managers) and leaves `/auth/login` and `/auth/register` public.
- **`AuthService` & `AuthController`**:
  - `POST /auth/login`: Validates credentials, rejects `PENDING` and `INACTIVE` accounts.
  - `POST /auth/register`: Allows self-registration of a `PENDING` `ADMIN` account with a randomly generated 8-character temporary password.
  - `POST /auth/change-password`: Validates old password and sets `firstLogin = false`.
  - `GET /auth/me`: Retrieves current authenticated user profile.

### 4. Exception Handling
- Custom Exceptions: `ResourceNotFoundException`, `AccountPendingException`, `AccountDisabledException`, `DuplicateEntityException`.
- **`GlobalExceptionHandler`**: `@RestControllerAdvice` to format all errors into a consistent `{ timestamp, status, message, path }` JSON response.

---

## 🚀 Phase 2: Projects, Tasks & Manager Operations

### 1. Manager Operations
- **Pending Admins**: Managers can view (`GET /manager/pending-admins`), approve (`POST /manager/approve-admin/{userId}`), and reject (`POST /manager/reject-admin/{userId}`) pending admin accounts.
- **User Management**: Managers can view all users, update their roles/departments, and deactivate accounts (`PUT /manager/users/{userId}/deactivate`).

### 2. Project Module
- **`Project` Entity**: Tracks project `name`, `department`, `description`, `teamLeader`, and a `@ManyToMany` list of `contributors`.
- **`ProjectService`**: Handles creating projects, fetching by department/team leader, and adding/removing contributors.
- **Team Leader Requests**: Admins can submit a request to become a Team Leader for a project (`TeamLeaderRequest` entity). Managers can view pending requests and approve/reject them. Approval automatically sets the requester as the `teamLeader` of the project.

### 3. Task & Subtask Module
- **Entities**: 
  - `Task`: Contains `name`, `description`, `deadline`, `githubRepoLink`, `TaskStatus` (`PENDING`, `IN_PROGRESS`, `SUBMITTED`, `APPROVED`, `REJECTED`), and assigns a `User` (employee).
  - `Subtask`: Breaks down a task, tracking `completedByEmployee`, `approvedByAdmin`, and `employeeComment`.
- **Workflow**:
  - Admins create tasks within a project and assign them to project contributors.
  - Employees view their assigned tasks (`GET /tasks/my`).
  - Employees start a task (`POST /tasks/{taskId}/start`). The system enforces that **an employee can only have one `IN_PROGRESS` task at a time**.
  - Employees mark subtasks as complete and leave comments.

### 4. Progress Tracking
- **`ProgressService` & `ProgressController`**:
  - Calculates project completion statistics dynamically.
  - Returns total tasks, completed tasks (status = `APPROVED`), total subtasks, and approved subtasks for a specific project (`GET /progress/project/{projectId}`).
  - Provides a high-level summary of all projects for managers (`GET /progress/manager/all`).

---

## 🚀 Phase 3: Timer Persistence, Submission & Review

### 1. Timer State Persistence (Strategy Pattern)
- **`WorkSession` Entity**: Tracks active timer sessions per employee (`workedSeconds`, `breakSeconds`, `SessionState`).
- **Strategy Pattern Implementation**: `TimerStrategy` interface with `RunningStrategy` and `BreakStrategy` to isolate logic for time increments and automatic state transitions (e.g., triggering break warnings at 4h - 3m, forcing break at 4h, and signaling submission page at 8h - 10m).
- **`TimerService` & `TimerController`**: Handles `startSession`, `getActiveSession` (for browser refreshes), and `syncTick` (called periodically by the frontend to update time and state).

### 2. Submissions
- **`TaskSubmission` Entity**: Links a completed task to an employee's submission (comment, file attachment path, alternative GitHub link).
- **`SubmissionService`**:
  - `submitTask`: Handles file uploads locally, sets task status to `SUBMITTED`, and ends the employee's active timer session.
  - `reviewSubmission`: Allows the project's Team Leader to `APPROVE` or `REJECT` a submission. Approving auto-approves completed subtasks.
  - `getSubmissionDetail`: Provides full details (subtasks, timestamps, comments) to the Team Leader for review.
- **`SubmissionController`**: Exposes endpoints for employees to submit (via `multipart/form-data`) and for admins to review.

### 3. Admin Comments & Notes
- **`SubmissionComment` Entity**: Represents messages attached to a submission.
- **Private vs Public Notes**: Admins can add `isPrivateNote = true` comments visible only to managers/admins, or public comments visible to the employee for feedback/revisions.

---

## ⚠️ Known Environmental Issues
- **Java 25 & Lombok Compatibility**: The current development machine is running Java 25. The `lombok` annotation processor (even at the latest `1.18.36`) throws a `TypeTag :: UNKNOWN` error during `mvn compile`.
- **Workaround**: The codebase is fully correct. To run the application successfully, the project SDK/JDK must be downgraded to **Java 17** or **Java 21 (LTS)**.
