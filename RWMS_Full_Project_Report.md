# Remote Work Management System (RWMS) — Full Technical Report

> **Generated:** 2026-05-25 | **Role:** Senior Software Engineer Analysis  
> **Codebase location:** `e:\3 Work space\Java\Remote Work Management System\rwms`

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [Project Structure](#3-project-structure)
4. [Architecture](#4-architecture)
5. [Database Entities & Relationships](#5-database-entities--relationships)
6. [Security Model](#6-security-model)
7. [Complete API Endpoint Reference](#7-complete-api-endpoint-reference)
   - 7.1 [Auth](#71-auth---base-auth)
   - 7.2 [Users](#72-users---base-users)
   - 7.3 [Manager](#73-manager---base-manager)
   - 7.4 [Projects](#74-projects---base-projects)
   - 7.5 [Progress](#75-progress---base-progress)
   - 7.6 [Tasks](#76-tasks---base-tasks)
   - 7.7 [Submissions](#77-submissions---base-submissions)
   - 7.8 [Timer / Work Sessions](#78-timer--work-sessions---base-timer)
   - 7.9 [Notifications](#79-notifications---base-apinotifications)
   - 7.10 [Audit Logs](#710-audit-logs---base-apiaudit)
8. [Design Patterns Used](#8-design-patterns-used)
9. [Error Handling](#9-error-handling)
10. [Frontend Integration Guide](#10-frontend-integration-guide)
11. [CORS Configuration](#11-cors-configuration)
12. [Module Summary Table](#12-module-summary-table)

---

## 1. Project Overview

The **Remote Work Management System (RWMS)** is a RESTful backend application built with **Spring Boot 3.3.0** that enables organizations to manage remote employees, projects, tasks, work tracking, and submissions. It supports three user roles — `EMPLOYEE`, `ADMIN`, and `MANAGER` — each with distinct capabilities.

### Core Features

| Feature | Description |
|---|---|
| Authentication | JWT-based stateless auth with role-gated access |
| User Management | Full CRUD, role/department assignment, approval workflows |
| Project Management | Create projects, manage contributors, team leader requests |
| Task Management | Create/assign tasks & subtasks with lifecycle tracking |
| Task Submissions | Employees submit work (with file upload), admins review |
| Work Timer | Real-time session tracking (start / sync / break / end) |
| Notifications | In-app notifications with read/unread state |
| Audit Logs | Full activity log with CSV export for managers |
| Progress Tracking | Per-project and global task completion dashboards |

---

## 2. Technology Stack

| Component | Technology | Version |
|---|---|---|
| Language | Java | 21 |
| Framework | Spring Boot | 3.3.0 |
| Security | Spring Security + JWT (JJWT) | 0.11.5 |
| Persistence | Spring Data JPA / Hibernate | (Boot-managed) |
| Database | MySQL | 8.x (schema: `rwms_db`) |
| Validation | Jakarta Validation (Bean Validation) | (Boot-managed) |
| Boilerplate | Lombok | 1.18.36 |
| Report Export | Apache POI (Excel) + OpenPDF | 5.2.3 / 1.3.30 |
| Build Tool | Maven | 3.x |
| Server Port | Embedded Tomcat | **8080** |

---

## 3. Project Structure

```
rwms/
├── pom.xml                          # Maven build descriptor
├── src/
│   └── main/
│       ├── java/com/rwms/
│       │   ├── RwmsApplication.java            # Entry point
│       │   ├── auth/                           # Authentication module
│       │   │   ├── controller/AuthController.java
│       │   │   ├── dto/                        # LoginRequest, LoginResponse,
│       │   │   │                                 RegisterAdminRequest, ChangePasswordRequest
│       │   │   ├── entity/                     # (token/session entities if any)
│       │   │   ├── filter/JwtAuthFilter.java   # JWT request filter
│       │   │   ├── mapper/
│       │   │   ├── repository/
│       │   │   ├── service/AuthService.java
│       │   │   └── util/                       # JWT utility helpers
│       │   │
│       │   ├── user/                           # User management module
│       │   │   ├── controller/
│       │   │   │   ├── UserController.java
│       │   │   │   └── ManagerController.java
│       │   │   ├── dto/                        # CreateUserRequest, UpdateUserRequest,
│       │   │   │                                 UserResponse
│       │   │   ├── entity/User.java
│       │   │   ├── mapper/UserMapper.java
│       │   │   ├── repository/UserRepository.java
│       │   │   └── service/
│       │   │       ├── IUserService.java
│       │   │       └── UserService.java
│       │   │
│       │   ├── project/                        # Project management module
│       │   │   ├── controller/
│       │   │   │   ├── ProjectController.java
│       │   │   │   └── ProgressController.java
│       │   │   ├── dto/                        # CreateProjectRequest, ProjectResponse,
│       │   │   │                                 AddContributorsRequest, ProjectProgressResponse,
│       │   │   │                                 TaskProgressItem, TeamLeaderRequestResponse
│       │   │   ├── entity/
│       │   │   │   ├── Project.java
│       │   │   │   └── TeamLeaderRequest.java
│       │   │   ├── repository/
│       │   │   │   ├── ProjectRepository.java
│       │   │   │   └── TeamLeaderRequestRepository.java
│       │   │   └── service/
│       │   │       ├── IProjectService.java
│       │   │       ├── ProjectService.java
│       │   │       └── ProgressService.java
│       │   │
│       │   ├── task/                           # Task & subtask module
│       │   │   ├── controller/TaskController.java
│       │   │   ├── dto/                        # CreateTaskRequest, UpdateTaskRequest,
│       │   │   │                                 AssignTaskRequest, TaskResponse,
│       │   │   │                                 CreateSubtaskRequest, SubtaskResponse
│       │   │   ├── entity/
│       │   │   │   ├── Task.java
│       │   │   │   └── Subtask.java
│       │   │   ├── mapper/
│       │   │   ├── repository/
│       │   │   │   ├── TaskRepository.java
│       │   │   │   └── SubtaskRepository.java
│       │   │   └── service/
│       │   │       ├── ITaskService.java
│       │   │       └── TaskService.java
│       │   │
│       │   ├── submission/                     # Task submission & review module
│       │   │   ├── controller/SubmissionController.java
│       │   │   ├── dto/                        # SubmitTaskRequest, SubmissionResponse,
│       │   │   │                                 SubmissionDetailResponse, ReviewRequest,
│       │   │   │                                 CommentRequest, CommentResponse
│       │   │   ├── entity/
│       │   │   │   ├── TaskSubmission.java
│       │   │   │   └── SubmissionComment.java
│       │   │   ├── repository/
│       │   │   │   ├── TaskSubmissionRepository.java
│       │   │   │   └── SubmissionCommentRepository.java
│       │   │   └── service/SubmissionService.java
│       │   │
│       │   ├── timer/                          # Work session timer module
│       │   │   ├── controller/TimerController.java
│       │   │   ├── dto/WorkSessionResponse.java
│       │   │   ├── entity/WorkSession.java
│       │   │   ├── repository/WorkSessionRepository.java
│       │   │   ├── service/TimerService.java
│       │   │   └── strategy/                   # Strategy pattern
│       │   │       ├── TimerStrategy.java      # interface
│       │   │       ├── TimerContext.java
│       │   │       ├── RunningStrategy.java
│       │   │       └── BreakStrategy.java
│       │   │
│       │   ├── notification/                   # In-app notification module
│       │   │   ├── controller/NotificationController.java
│       │   │   ├── dto/NotificationResponse.java
│       │   │   ├── entity/
│       │   │   │   ├── Notification.java
│       │   │   │   └── NotificationType.java   # enum
│       │   │   ├── mapper/
│       │   │   ├── observer/                   # Observer pattern
│       │   │   ├── repository/
│       │   │   └── service/
│       │   │       ├── INotificationService.java
│       │   │       └── NotificationService.java
│       │   │
│       │   ├── audit/                          # Audit log module
│       │   │   ├── command/                    # Command pattern
│       │   │   ├── controller/AuditController.java
│       │   │   ├── dto/
│       │   │   │   ├── AuditLogFilter.java
│       │   │   │   └── AuditLogResponse.java
│       │   │   ├── entity/AuditLog.java
│       │   │   ├── repository/
│       │   │   └── service/AuditLogService.java
│       │   │
│       │   ├── attendance/                     # Attendance module (scaffolded)
│       │   │   ├── controller/  (no endpoints yet)
│       │   │   ├── dto/
│       │   │   ├── entity/
│       │   │   ├── mapper/
│       │   │   ├── repository/
│       │   │   └── service/
│       │   │
│       │   ├── timeoff/                        # Time-off module (scaffolded)
│       │   │   ├── controller/  (no endpoints yet)
│       │   │   ├── dto/
│       │   │   ├── entity/
│       │   │   ├── mapper/
│       │   │   ├── repository/
│       │   │   └── service/
│       │   │
│       │   ├── common/                         # Shared utilities
│       │   │   └── exception/
│       │   │       ├── GlobalExceptionHandler.java
│       │   │       ├── ResourceNotFoundException.java
│       │   │       ├── AccountPendingException.java
│       │   │       ├── AccountDisabledException.java
│       │   │       └── DuplicateEntityException.java
│       │   │
│       │   └── security/
│       │       └── SecurityConfig.java         # Spring Security + CORS
│       │
│       └── resources/
│           └── application.properties          # App configuration
```

---

## 4. Architecture

### Architectural Style

The application follows a **layered monolithic architecture** organized by **domain module** (vertical slicing by feature), where each module owns its own:

```
Controller → Service (Interface + Impl) → Repository → Entity
                    ↕ DTO Mapper
```

### Layer Responsibilities

| Layer | Responsibility |
|---|---|
| **Controller** | HTTP endpoint mapping, request validation, auth principal extraction |
| **Service (Interface)** | Business logic contracts (abstraction) |
| **Service (Impl)** | Actual business logic, cross-module calls |
| **Repository** | Spring Data JPA repositories — database access |
| **Entity** | JPA-mapped database tables |
| **DTO** | Request/Response data transfer objects (no entity exposure) |
| **Mapper** | Entity ↔ DTO transformation |
| **Filter** | JWT authentication filter (pre-request processing) |

### Request Flow

```
HTTP Request
    ↓
JwtAuthFilter (validates Bearer token, sets SecurityContext)
    ↓
Spring Security (checks route permissions)
    ↓
@RestController (validates @RequestBody, extracts @AuthenticationPrincipal)
    ↓
Service Layer (business logic)
    ↓
Repository (JPA → MySQL)
    ↓
DTO Mapper → ResponseEntity
    ↓
HTTP Response (JSON)
```

---

## 5. Database Entities & Relationships

### Entity Overview

#### `users` table — `User.java`

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | — |
| `full_name` | VARCHAR | NOT NULL | — |
| `employee_id` | VARCHAR | UNIQUE, NOT NULL | Company employee ID |
| `email` | VARCHAR | UNIQUE, NOT NULL | Used as login username |
| `password` | VARCHAR | NOT NULL | BCrypt hashed |
| `github_username` | VARCHAR | nullable | — |
| `phone` | VARCHAR | nullable | — |
| `department` | VARCHAR | nullable | — |
| `role` | ENUM | NOT NULL, default `EMPLOYEE` | `EMPLOYEE`, `ADMIN`, `MANAGER` |
| `status` | ENUM | NOT NULL, default `PENDING` | `PENDING`, `ACTIVE`, `INACTIVE`, `REJECTED` |
| `first_login` | BOOLEAN | default `true` | Forces password change on first login |
| `created_at` | DATETIME | auto, immutable | — |
| `updated_at` | DATETIME | auto-updated | — |

#### `projects` table — `Project.java`

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | BIGINT | PK | — |
| `name` | VARCHAR | NOT NULL | — |
| `department` | VARCHAR | NOT NULL | — |
| `description` | TEXT | nullable | — |
| `description_pdf_path` | VARCHAR | nullable | Path to uploaded PDF |
| `team_leader_id` | BIGINT | FK → users, nullable | `@ManyToOne` |
| `created_at` | DATETIME | auto, immutable | — |

**Junction table:** `project_contributors (project_id, user_id)` — `@ManyToMany` with `users`

#### `tasks` table — `Task.java`

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | BIGINT | PK | — |
| `name` | VARCHAR | NOT NULL | — |
| `description` | TEXT | nullable | — |
| `deadline` | DATE | NOT NULL | — |
| `github_repo_link` | VARCHAR | nullable | — |
| `project_id` | BIGINT | FK → projects, NOT NULL | `@ManyToOne` |
| `assigned_employee_id` | BIGINT | FK → users, nullable | `@ManyToOne` |
| `status` | ENUM | NOT NULL, default `PENDING` | `PENDING`, `IN_PROGRESS`, `SUBMITTED`, `APPROVED`, `REJECTED` |
| `created_at` | DATETIME | auto, immutable | — |
| `started_at` | DATETIME | nullable | Set when employee starts task |

#### `subtasks` table — `Subtask.java`

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | BIGINT | PK | — |
| `name` | VARCHAR | NOT NULL | — |
| `description` | VARCHAR | nullable | — |
| `task_id` | BIGINT | FK → tasks, NOT NULL | `@ManyToOne` |
| `completed_by_employee` | BOOLEAN | default `false` | Employee marks complete |
| `approved_by_admin` | BOOLEAN | default `false` | Admin confirms completion |
| `completed_at` | DATETIME | nullable | — |
| `employee_comment` | VARCHAR | nullable | — |

#### `task_submissions` table — `TaskSubmission.java`

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | BIGINT | PK | — |
| `task_id` | BIGINT | FK → tasks, NOT NULL, UNIQUE | `@OneToOne` |
| `employee_id` | BIGINT | FK → users, NOT NULL | `@ManyToOne` |
| `accomplishment_comment` | TEXT | NOT NULL | Employee's work description |
| `attachment_path` | VARCHAR | nullable | Uploaded file path |
| `alternative_github_link` | VARCHAR | nullable | Override github link |
| `submitted_at` | DATETIME | nullable | — |
| `review_status` | ENUM | NOT NULL, default `PENDING` | `PENDING`, `APPROVED`, `REJECTED` |
| `admin_note` | TEXT | nullable | Admin comment on approval |
| `rejection_reason` | TEXT | nullable | Admin comment on rejection |

#### `work_sessions` table — `WorkSession.java`

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | BIGINT | PK | — |
| `employee_id` | BIGINT | FK → users, NOT NULL | `@ManyToOne` |
| `task_id` | BIGINT | FK → tasks, NOT NULL | `@ManyToOne` |
| `state` | ENUM | NOT NULL, default `RUNNING` | `RUNNING`, `ON_BREAK`, `COMPLETED` |
| `worked_seconds` | BIGINT | default `0` | — |
| `break_seconds` | BIGINT | default `0` | — |
| `break_taken` | BOOLEAN | default `false` | One break per session |
| `session_started_at` | DATETIME | — | — |
| `break_started_at` | DATETIME | nullable | — |
| `last_synced_at` | DATETIME | nullable | Updated on each `/sync` call |
| `submission_page_triggered` | BOOLEAN | default `false` | Triggers UI redirect |
| `break_warning_sent` | BOOLEAN | default `false` | — |
| `break_ending_warning_sent` | BOOLEAN | default `false` | — |

#### `notifications` table — `Notification.java`

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | BIGINT | PK | — |
| `recipient_id` | BIGINT | FK → users, NOT NULL | `@ManyToOne` |
| `title` | VARCHAR | NOT NULL | — |
| `message` | VARCHAR(1000) | NOT NULL | — |
| `type` | ENUM | NOT NULL | See `NotificationType` below |
| `is_read` | BOOLEAN | NOT NULL, default `false` | — |
| `created_at` | DATETIME | auto, immutable | — |

**Notification Types (enum `NotificationType`):**
`TASK_ASSIGNED`, `SUBMISSION_APPROVED`, `SUBMISSION_REJECTED`, `ADMIN_COMMENT`, `NEW_ADMIN_REGISTRATION`, `TL_ASSIGNMENT_REQUEST`, `BREAK_WARNING`, `BREAK_ENDING_WARNING`

#### `audit_logs` table — `AuditLog.java`

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | BIGINT | PK | — |
| `action_name` | VARCHAR | NOT NULL | e.g. `USER_LOGIN`, `TASK_ASSIGNED` |
| `user_email` | VARCHAR | NOT NULL | Subject of the action |
| `performed_by_id` | BIGINT | FK → users, nullable | Who triggered the action |
| `details` | TEXT | nullable | JSON or plain text details |
| `timestamp` | DATETIME | auto, immutable | — |

#### `team_leader_requests` table — `TeamLeaderRequest.java`

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | BIGINT | PK | — |
| `requester_id` | BIGINT | FK → users, NOT NULL | `@ManyToOne` |
| `project_id` | BIGINT | FK → projects, NOT NULL | `@ManyToOne` |
| `status` | ENUM | NOT NULL, default `PENDING` | `PENDING`, `APPROVED`, `REJECTED` |
| `submitted_at` | DATETIME | auto, immutable | — |

### Entity Relationship Diagram

```
users ──────────────────────────────────────────────────────┐
  │                                                          │
  │ (team_leader)           (assigned_employee)              │ (recipient)
  ▼                         ▼                               ▼
projects ──────────── tasks ──────────── task_submissions  notifications
  │                    │
  │ M:M                │ 1:M
  ▼                    ▼
project_contributors  subtasks
  │
  ▼
users (contributors)

tasks ──────────── work_sessions ──── users (employee)

projects ──── team_leader_requests ──── users (requester)

audit_logs ──── users (performed_by)
```

---

## 6. Security Model

### Authentication

- **Mechanism:** JWT (JSON Web Token) via `Authorization: Bearer <token>` header
- **Algorithm:** HMAC-SHA256 (configured via `jwt.secret` property)
- **Expiration:** `32400000` ms = **9 hours**
- **Filter:** `JwtAuthFilter` intercepts every request, validates token, and sets `SecurityContextHolder`
- **Session Policy:** `STATELESS` — no server-side session

### Role Hierarchy

| Role | Description | Permissions |
|---|---|---|
| `EMPLOYEE` | Regular remote worker | Access own tasks, submissions, timer, notifications |
| `ADMIN` | Team administrator | Manage tasks, review submissions, manage contributors, access `/admin/**` |
| `MANAGER` | Top-level manager | All ADMIN rights + manage users, approve admins/TL requests, view audit logs, access `/manager/**` |

### Route-Level Security (from `SecurityConfig.java`)

| Pattern | Access Rule |
|---|---|
| `POST /auth/login` | **Public** (no auth required) |
| `POST /auth/register` | **Public** (no auth required) |
| `/admin/**` | `ADMIN` or `MANAGER` role |
| `/manager/**` | `MANAGER` role only |
| Everything else | Any **authenticated** user |

> **Note:** Additional method-level security is applied via `@PreAuthorize` annotations (e.g., `hasRole('MANAGER')` on audit log endpoints, `isAuthenticated()` on notifications).

### Password Management

- Passwords hashed with `BCryptPasswordEncoder`
- On first login (`firstLogin = true`), users **must** call `POST /auth/change-password`
- `firstLogin` flag is set to `false` after password change

---

## 7. Complete API Endpoint Reference

> **Base URL:** `http://localhost:8080`  
> **Auth Header:** `Authorization: Bearer <JWT_TOKEN>` (unless marked as Public)

---

### 7.1 Auth — Base: `/auth`

#### `POST /auth/login`
- **Access:** 🔓 Public
- **Description:** Authenticate user and receive JWT token
- **Request Body:**
```json
{
  "email": "user@example.com",
  "password": "string"
}
```
- **Response `200 OK`:**
```json
{
  "token": "eyJhbGc...",
  "role": "EMPLOYEE | ADMIN | MANAGER",
  "firstLogin": true,
  "userId": 1
}
```
- **Frontend Notes:** Store `token` in localStorage/cookie. Check `firstLogin` — if `true`, redirect to change-password screen. Store `role` for route guarding.

---

#### `POST /auth/register`
- **Access:** 🔓 Public
- **Description:** Self-register as a pending ADMIN account
- **Request Body:**
```json
{
  "fullName": "Omar Yosri",
  "employeeId": "EMP-001",
  "gmailAddress": "omar@example.com",
  "githubUsername": "omar-yosrii"
}
```
- **Response:** `201 Created` (no body)
- **Notes:** Creates user with `status = PENDING`, `role = ADMIN`. A Manager must approve via `/manager/approve-admin/{userId}`.

---

#### `POST /auth/change-password`
- **Access:** 🔒 Authenticated
- **Description:** Change password (required on first login)
- **Request Body:**
```json
{
  "currentPassword": "string",
  "newPassword": "string"
}
```
- **Response:** `200 OK` (no body)

---

#### `GET /auth/me`
- **Access:** 🔒 Authenticated
- **Description:** Get the currently authenticated user's profile
- **Response `200 OK`:** → [`UserResponse`](#userresponse-shape)

---

### 7.2 Users — Base: `/users`

> Role required: **Authenticated** (read) / **ADMIN or MANAGER** for write operations

#### `GET /users`
- Returns list of all users
- **Response:** `List<UserResponse>`

#### `GET /users/{id}`
- Returns user by numeric ID
- **Path Variable:** `id` (Long)
- **Response:** `UserResponse`

#### `GET /users/email/{email}`
- Returns user by email address
- **Path Variable:** `email` (String)
- **Response:** `UserResponse`

#### `GET /users/department/{department}`
- Returns all users in a specific department
- **Path Variable:** `department` (String)
- **Response:** `List<UserResponse>`

#### `POST /users`
- Creates a new user (admin-created, not self-registration)
- **Request Body:**
```json
{
  "fullName": "Jane Doe",
  "employeeId": "EMP-002",
  "email": "jane@example.com",
  "password": "initialPassword",
  "githubUsername": "janedoe",
  "phone": "+1234567890",
  "department": "Engineering",
  "role": "EMPLOYEE"
}
```
- **Response:** `201 Created` + `UserResponse`

#### `PUT /users/{id}`
- Updates user profile fields
- **Path Variable:** `id` (Long)
- **Request Body:** `UpdateUserRequest` (partial fields)
- **Response:** `200 OK` + `UserResponse`

---

### 7.3 Manager — Base: `/manager`

> Role required: **MANAGER only**

#### `GET /manager/pending-admins`
- List all users with `status = PENDING` (admin self-registrations awaiting approval)
- **Response:** `List<UserResponse>`

#### `POST /manager/approve-admin/{userId}`
- Approve a pending admin registration → sets `status = ACTIVE`
- **Path Variable:** `userId` (Long)
- **Response:** `200 OK` (no body)

#### `POST /manager/reject-admin/{userId}`
- Reject a pending admin registration → sets `status = REJECTED`
- **Path Variable:** `userId` (Long)
- **Response:** `200 OK` (no body)

#### `GET /manager/pending-tl-requests`
- List all pending Team Leader role requests
- **Response:** `List<TeamLeaderRequestResponse>`

#### `POST /manager/approve-tl/{requestId}`
- Approve a Team Leader request → assigns user as project's `teamLeader`
- **Path Variable:** `requestId` (Long)
- **Response:** `200 OK` (no body)

#### `POST /manager/reject-tl/{requestId}`
- Reject a Team Leader request
- **Path Variable:** `requestId` (Long)
- **Response:** `200 OK` (no body)

#### `GET /manager/users`
- Get all users (manager-scoped full list)
- **Response:** `List<UserResponse>`

#### `PUT /manager/users/{userId}/role`
- Update a user's role and/or department
- **Path Variable:** `userId` (Long)
- **Query Params:** `role` (optional), `department` (optional)
- **Example:** `PUT /manager/users/5/role?role=ADMIN&department=Engineering`
- **Response:** `200 OK` + `UserResponse`

#### `PUT /manager/users/{userId}/deactivate`
- Deactivate a user account (`status = INACTIVE`)
- **Path Variable:** `userId` (Long)
- **Response:** `200 OK` (no body)

#### `DELETE /manager/users/{userId}`
- Permanently delete a user
- **Path Variable:** `userId` (Long)
- **Response:** `204 No Content`

---

### 7.4 Projects — Base: `/projects`

> Role required: **Authenticated** (read) / **ADMIN or MANAGER** for write

#### `POST /projects`
- Create a new project (caller becomes implicit owner)
- **Request Body:**
```json
{
  "name": "Project Alpha",
  "department": "Engineering",
  "description": "Optional description text"
}
```
- **Response:** `201 Created` + `ProjectResponse`

#### `GET /projects/department/{dept}`
- Get all projects belonging to a department
- **Path Variable:** `dept` (String)
- **Response:** `List<ProjectResponse>`

#### `GET /projects/my`
- Get projects where the authenticated user is a contributor or team leader
- **Response:** `List<ProjectResponse>`

#### `GET /projects/{id}`
- Get a specific project by ID
- **Path Variable:** `id` (Long)
- **Response:** `ProjectResponse`

#### `POST /projects/{id}/contributors`
- Add contributors (users) to a project
- **Path Variable:** `id` (Long)
- **Request Body:**
```json
{
  "userIds": [2, 3, 7]
}
```
- **Response:** `200 OK` (no body)

#### `DELETE /projects/{id}/contributors/{userId}`
- Remove a specific contributor from a project
- **Path Variables:** `id` (project), `userId` (user to remove)
- **Response:** `200 OK` (no body)

#### `POST /projects/{id}/request-tl`
- Submit a request to become Team Leader of a project (authenticated user requests for themselves)
- **Path Variable:** `id` (Long)
- **Response:** `200 OK` (no body)

---

### 7.5 Progress — Base: `/progress`

> Role required: **Authenticated** (employees) / **MANAGER** for `/manager/all`

#### `GET /progress/project/{projectId}`
- Get task completion progress for a specific project
- **Path Variable:** `projectId` (Long)
- **Response `200 OK`:** `ProjectProgressResponse`
```json
{
  "projectId": 1,
  "projectName": "Project Alpha",
  "totalTasks": 10,
  "completedTasks": 6,
  "progressPercent": 60.0,
  "taskItems": [...]
}
```

#### `GET /progress/manager/all`
- Get progress summary for **all** projects (manager dashboard)
- **Response:** `List<ProjectProgressResponse>`

---

### 7.6 Tasks — Base: `/tasks`

> Role required: **Authenticated** — role-specific business rules enforced in service layer

#### `POST /tasks/project/{projectId}`
- Create a task inside a project (optionally with initial subtasks)
- **Path Variable:** `projectId` (Long)
- **Request Body:**
```json
{
  "name": "Implement login API",
  "description": "Build JWT-based login endpoint",
  "deadline": "2026-06-30",
  "githubRepoLink": "https://github.com/org/repo",
  "subtasks": [
    { "name": "Write unit tests", "description": "Optional" }
  ]
}
```
- **Response:** `201 Created` + `TaskResponse`

#### `PUT /tasks/{taskId}`
- Update task details (name, description, deadline, github link)
- **Path Variable:** `taskId` (Long)
- **Request Body:** `UpdateTaskRequest`
- **Response:** `200 OK` + `TaskResponse`

#### `DELETE /tasks/{taskId}`
- Delete a task
- **Path Variable:** `taskId` (Long)
- **Response:** `204 No Content`

#### `POST /tasks/{taskId}/subtasks`
- Add a subtask to an existing task
- **Path Variable:** `taskId` (Long)
- **Request Body:**
```json
{
  "name": "Write integration test",
  "description": "Optional subtask description"
}
```
- **Response:** `201 Created` + `SubtaskResponse`

#### `PUT /tasks/{taskId}/subtasks/{subtaskId}`
- Update a subtask's name/description
- **Path Variables:** `taskId`, `subtaskId` (Long)
- **Request Body:** `CreateSubtaskRequest`
- **Response:** `200 OK` (no body)

#### `DELETE /tasks/{taskId}/subtasks/{subtaskId}`
- Delete a subtask
- **Path Variables:** `taskId`, `subtaskId` (Long)
- **Response:** `204 No Content`

#### `POST /tasks/{taskId}/assign`
- Assign a task to an employee
- **Path Variable:** `taskId` (Long)
- **Request Body:**
```json
{
  "employeeId": 5
}
```
- **Response:** `200 OK` (no body)
- **Side Effect:** Triggers `TASK_ASSIGNED` notification to the employee

#### `GET /tasks/my`
- Get all tasks assigned to the authenticated user
- **Response:** `List<TaskResponse>`

#### `POST /tasks/{taskId}/start`
- Employee marks a task as started → `status = IN_PROGRESS`, sets `startedAt`
- **Path Variable:** `taskId` (Long)
- **Response:** `200 OK` + `TaskResponse`

#### `POST /tasks/{taskId}/subtasks/{subtaskId}/complete`
- Employee marks a subtask as complete (with optional comment)
- **Path Variables:** `taskId`, `subtaskId` (Long)
- **Request Body:** Plain string (optional comment) or empty
- **Response:** `200 OK` (no body)

---

### 7.7 Submissions — Base: `/submissions`

> Role required: **Authenticated** — ownership enforced in service

#### `POST /submissions/task/{taskId}`
- Submit completed task work (supports file upload)
- **Path Variable:** `taskId` (Long)
- **Content-Type:** `multipart/form-data`
- **Form Fields:**
  - `request` (String / JSON): `{ "accomplishmentComment": "I implemented...", "alternativeGithubLink": "https://..." }`
  - `file` (MultipartFile, optional): Attachment file
- **Response:** `201 Created` + `SubmissionResponse`
- **Side Effect:** Task `status` → `SUBMITTED`

#### `GET /submissions/my`
- Get all submissions made by the authenticated employee
- **Response:** `List<SubmissionResponse>`
```json
[{
  "id": 1,
  "taskName": "Implement login API",
  "submittedAt": "2026-05-20T10:30:00",
  "reviewStatus": "PENDING | APPROVED | REJECTED",
  "rejectionReason": null
}]
```

#### `GET /submissions/{id}/detail`
- Get full submission details including comments
- **Path Variable:** `id` (Long)
- **Response:** `SubmissionDetailResponse`

#### `POST /submissions/{id}/review`
- Admin reviews a submission (approve or reject)
- **Path Variable:** `id` (Long)
- **Request Body:**
```json
{
  "approved": true,
  "adminNote": "Great work!",
  "rejectionReason": null
}
```
- **Response:** `200 OK` + `SubmissionResponse`
- **Side Effects:** Task `status` → `APPROVED` or `REJECTED`; triggers `SUBMISSION_APPROVED` or `SUBMISSION_REJECTED` notification

#### `GET /submissions/pending/project/{projectId}`
- Get all pending (unreviewed) submissions for a project
- **Path Variable:** `projectId` (Long)
- **Response:** `List<SubmissionResponse>`

#### `POST /submissions/{id}/comments`
- Add a comment to a submission (discussion thread)
- **Path Variable:** `id` (Long)
- **Request Body:**
```json
{
  "content": "Please revise the authentication logic."
}
```
- **Response:** `201 Created` + `CommentResponse`
- **Side Effect:** May trigger `ADMIN_COMMENT` notification

#### `GET /submissions/{id}/comments`
- Get all comments on a submission
- **Path Variable:** `id` (Long)
- **Response:** `List<CommentResponse>`

---

### 7.8 Timer / Work Sessions — Base: `/timer`

> Role required: **Authenticated** (Employee)

The timer tracks real-time work on a task using a **Strategy Pattern** (`RunningStrategy` / `BreakStrategy`). Each `/sync` call advances the clock.

#### `POST /timer/start/{taskId}`
- Start a new work session for a task
- **Path Variable:** `taskId` (Long)
- **Response:** `200 OK` + `WorkSessionResponse`
```json
{
  "sessionState": "RUNNING",
  "workedSeconds": 0,
  "breakSeconds": 0,
  "breakTaken": false,
  "breakWarning": false,
  "breakEndingWarning": false,
  "triggerSubmitPage": false
}
```

#### `GET /timer/active`
- Get the current active session state for the authenticated user
- **Response:** `200 OK` + `WorkSessionResponse`
- **Frontend Notes:** Poll this endpoint (or call on page load) to restore timer UI state

#### `POST /timer/sync`
- Tick/advance the timer — call periodically (e.g. every 60s) to update `workedSeconds`
- **Response:** `200 OK` + `WorkSessionResponse`
- **Frontend Notes:**
  - If `breakWarning: true` → show break warning UI
  - If `breakEndingWarning: true` → show "break ending soon" warning
  - If `triggerSubmitPage: true` → redirect employee to submission form

#### `POST /timer/end`
- End the current work session → `state = COMPLETED`
- **Response:** `200 OK` (no body)

---

### 7.9 Notifications — Base: `/api/notifications`

> Role required: **Authenticated** (`@PreAuthorize("isAuthenticated()")`)

#### `GET /api/notifications`
- Get paginated notifications for the authenticated user
- **Query Params:** `page` (default: 0), `size` (default: 10)
- **Response:** `Page<NotificationResponse>` (Spring Data Page with `content`, `totalPages`, `totalElements`, etc.)
```json
{
  "content": [{
    "id": 1,
    "title": "Task Assigned",
    "message": "You have been assigned 'Implement login API'",
    "type": "TASK_ASSIGNED",
    "read": false,
    "createdAt": "2026-05-20T09:00:00"
  }],
  "totalElements": 25,
  "totalPages": 3
}
```

#### `GET /api/notifications/unread-count`
- Get count of unread notifications (for badge display)
- **Response:** `Long` (plain number)

#### `POST /api/notifications/{id}/read`
- Mark a single notification as read
- **Path Variable:** `id` (Long)
- **Response:** `200 OK` (no body)

#### `POST /api/notifications/read-all`
- Mark all of the user's notifications as read
- **Response:** `200 OK` (no body)

---

### 7.10 Audit Logs — Base: `/api/audit`

> Role required: **MANAGER** (`@PreAuthorize("hasRole('MANAGER')")`)

#### `GET /api/audit/logs`
- Get paginated, filterable audit logs
- **Query Params (all optional):**
  - `actionName` (String) — filter by action type
  - `userEmail` (String) — filter by subject user email
  - `performedByEmail` (String) — filter by actor email
  - `from` (ISO DateTime: `2026-01-01T00:00:00`) — start of date range
  - `to` (ISO DateTime: `2026-12-31T23:59:59`) — end of date range
  - `page` (default: 0)
  - `size` (default: 20)
- **Response:** `Page<AuditLogResponse>` sorted by `timestamp DESC`
```json
{
  "content": [{
    "id": 1,
    "actionName": "USER_APPROVED",
    "userEmail": "employee@company.com",
    "performedByEmail": "manager@company.com",
    "timestamp": "2026-05-20T10:00:00",
    "details": "Account activated"
  }]
}
```

#### `GET /api/audit/logs/export`
- Export filtered audit logs as a **CSV file download**
- **Query Params:** Same filters as above (no pagination — returns all matching)
- **Response:** `200 OK` with `Content-Disposition: attachment; filename="audit_logs.csv"`, `Content-Type: text/csv`
- **CSV Columns:** `ID, Action Name, User Email, Performed By, Timestamp, Details`
- **Frontend Notes:** Trigger via `window.location.href = '/api/audit/logs/export?...'` or use `fetch` with `blob()` to download programmatically

---

## 8. Design Patterns Used

| Pattern | Module | Purpose |
|---|---|---|
| **Strategy** | `timer/strategy/` | Interchangeable timer behavior (`RunningStrategy`, `BreakStrategy`) controlled by `TimerContext` |
| **Observer** | `notification/observer/` | Decouple event producers (task assignment, submission review) from notification dispatch |
| **Command** | `audit/command/` | Encapsulate audit log write operations as command objects |
| **Repository** | All modules | Spring Data JPA repository abstraction per entity |
| **DTO / Mapper** | All modules | Strict separation between entity and API contract |
| **Interface-Based Service** | All modules | `IXxxService` interfaces for loose coupling and testability |
| **Global Exception Handler** | `common/exception/` | Centralized `@RestControllerAdvice` for consistent error responses |

---

## 9. Error Handling

All errors return a consistent JSON structure:

```json
{
  "timestamp": "2026-05-25T10:00:00",
  "status": 404,
  "message": "User not found with id: 99",
  "path": "/users/99"
}
```

| Exception | HTTP Status | Trigger |
|---|---|---|
| `ResourceNotFoundException` | `404 Not Found` | Entity not found by ID/email |
| `AccountPendingException` | `403 Forbidden` | Login attempt on PENDING account |
| `AccountDisabledException` | `403 Forbidden` | Login attempt on INACTIVE/REJECTED account |
| `DuplicateEntityException` | `409 Conflict` | Duplicate email or employeeId |
| `IllegalArgumentException` | `400 Bad Request` | Invalid business rule violation |
| `MethodArgumentNotValidException` | `400 Bad Request` | `@Valid` annotation failure on request body |
| `Exception` (catch-all) | `500 Internal Server Error` | Unhandled server-side error |

---

## 10. Frontend Integration Guide

### Authentication Flow

```
1. POST /auth/login → store { token, role, firstLogin, userId }
2. If firstLogin === true → force navigate to /change-password
3. POST /auth/change-password → clears firstLogin flag
4. For all subsequent requests: set header Authorization: Bearer <token>
5. On 401 response → clear token + redirect to /login
```

### Axios Setup (JavaScript / TypeScript)

```js
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true,        // Important for CORS with credentials
});

// Attach token to every request
api.interceptors.request.use(config => {
  const token = localStorage.getItem('rwms_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Handle 401 globally
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('rwms_token');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);
```

### Role-Based Route Guarding

```js
// Roles returned by /auth/login
const ROLES = { EMPLOYEE: 'EMPLOYEE', ADMIN: 'ADMIN', MANAGER: 'MANAGER' };

// Route access matrix
const routeAccess = {
  '/dashboard':            [ROLES.EMPLOYEE, ROLES.ADMIN, ROLES.MANAGER],
  '/manager/*':            [ROLES.MANAGER],
  '/admin/submissions':    [ROLES.ADMIN, ROLES.MANAGER],
  '/audit-logs':           [ROLES.MANAGER],
};
```

### File Upload (Submission)

```js
const formData = new FormData();
formData.append('request', JSON.stringify({
  accomplishmentComment: 'I completed the task by...',
  alternativeGithubLink: 'https://github.com/...'
}));
formData.append('file', fileInput.files[0]); // optional

api.post(`/submissions/task/${taskId}`, formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
});
```

### Timer Integration Pattern

```js
// On task start
await api.post(`/timer/start/${taskId}`);

// Poll every 60 seconds
const timerInterval = setInterval(async () => {
  const { data } = await api.post('/timer/sync');
  updateTimerUI(data.workedSeconds, data.breakSeconds);
  
  if (data.breakWarning)        showBreakWarning();
  if (data.breakEndingWarning)  showBreakEndingWarning();
  if (data.triggerSubmitPage)   navigateToSubmission();
}, 60000);

// On end
await api.post('/timer/end');
clearInterval(timerInterval);
```

### Notification Badge

```js
// On app load / after actions
const { data: count } = await api.get('/api/notifications/unread-count');
setBadgeCount(count);

// Fetch notification list
const { data } = await api.get('/api/notifications?page=0&size=10');
renderNotifications(data.content);

// Mark single as read
await api.post(`/api/notifications/${notifId}/read`);

// Mark all as read
await api.post('/api/notifications/read-all');
```

### Audit Log Export Button

```js
const exportAuditLogs = (filters = {}) => {
  const params = new URLSearchParams(filters).toString();
  window.open(`http://localhost:8080/api/audit/logs/export?${params}`, '_blank');
};
```

---

## 11. CORS Configuration

Configured in `SecurityConfig.java`:

| Setting | Value |
|---|---|
| **Allowed Origins** | `http://localhost:3000` (React), `http://localhost:5173` (Vite) |
| **Allowed Methods** | `GET, POST, PUT, DELETE, PATCH, OPTIONS` |
| **Allowed Headers** | `*` (all headers) |
| **Allow Credentials** | `true` |
| **Pattern** | `/**` (all endpoints) |

> ⚠️ **Important for production:** Update `allowedOriginPatterns` in `SecurityConfig.java` to your deployed frontend domain.

---

## 12. Module Summary Table

| Module | Package | Controller(s) | Entities | Status |
|---|---|---|---|---|
| Auth | `com.rwms.auth` | `AuthController` | — | ✅ Active |
| User Management | `com.rwms.user` | `UserController`, `ManagerController` | `User` | ✅ Active |
| Project Management | `com.rwms.project` | `ProjectController`, `ProgressController` | `Project`, `TeamLeaderRequest` | ✅ Active |
| Task Management | `com.rwms.task` | `TaskController` | `Task`, `Subtask` | ✅ Active |
| Submissions | `com.rwms.submission` | `SubmissionController` | `TaskSubmission`, `SubmissionComment` | ✅ Active |
| Work Timer | `com.rwms.timer` | `TimerController` | `WorkSession` | ✅ Active |
| Notifications | `com.rwms.notification` | `NotificationController` | `Notification` | ✅ Active |
| Audit Logs | `com.rwms.audit` | `AuditController` | `AuditLog` | ✅ Active |
| Attendance | `com.rwms.attendance` | *(none yet)* | *(scaffolded)* | 🚧 Planned |
| Time-Off | `com.rwms.timeoff` | *(none yet)* | *(scaffolded)* | 🚧 Planned |
| Common/Shared | `com.rwms.common` | `GlobalExceptionHandler` | — | ✅ Active |
| Security | `com.rwms.security` | — | — | ✅ Active |

---

## UserResponse Shape

```json
{
  "id": 1,
  "fullName": "Omar Yosri",
  "employeeId": "EMP-001",
  "email": "omar@example.com",
  "githubUsername": "omar-yosrii",
  "phone": "+20123456789",
  "department": "Engineering",
  "role": "EMPLOYEE | ADMIN | MANAGER",
  "status": "PENDING | ACTIVE | INACTIVE | REJECTED",
  "firstLogin": false,
  "createdAt": "2026-05-01T08:00:00",
  "updatedAt": "2026-05-25T10:00:00"
}
```

---

*End of Report — Remote Work Management System (RWMS) Backend*
