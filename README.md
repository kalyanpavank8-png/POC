# DSAR Portal — MVP

> **Data Subject Access Request** portal built with **Spring Boot 3 / Java 17** (backend) and **Vue 3 / Vite** (frontend). All data is held in-memory — no external database required.

---

## Features

| Feature | Details |
|---|---|
| **Submit requests** | Customers submit ACCESS, DELETE, or CORRECT requests via a guided form |
| **Role-based views** | Customers see only their own requests; admins see everything |
| **Admin processing** | Admins move requests through PENDING → IN_PROGRESS → COMPLETED / REJECTED with optional notes |
| **Audit trail** | Every action (submit, status change) is logged with timestamp, actor, and detail — admin-only view |
| **Basic Auth** | Spring Security HTTP Basic; stateless (no sessions) |
| **In-memory store** | `ConcurrentHashMap` + `CopyOnWriteArrayList` — no DB setup needed |

---

## Project Structure

```
dsar-portal/
├── backend/                   # Spring Boot + Java 17
│   ├── pom.xml
│   └── src/main/java/com/dsar/
│       ├── DsarApplication.java
│       ├── config/
│       │   └── SecurityConfig.java      # Basic Auth, CORS, role setup
│       ├── model/
│       │   ├── DsarRequest.java
│       │   ├── AuditLog.java
│       │   ├── RequestType.java         # ACCESS | DELETE | CORRECT
│       │   └── RequestStatus.java       # PENDING | IN_PROGRESS | COMPLETED | REJECTED
│       ├── dto/
│       │   ├── CreateRequestDto.java    # (record) customer submit payload
│       │   ├── UpdateStatusDto.java     # (record) admin update payload
│       │   └── UserInfoDto.java         # (record) /me response
│       ├── repository/
│       │   └── InMemoryStore.java       # Thread-safe in-memory data store
│       ├── service/
│       │   └── DsarService.java         # Business logic + audit trail
│       └── controller/
│           ├── DsarController.java      # REST endpoints
│           └── AuthController.java      # GET /api/auth/me
└── frontend/                  # Vue 3 + Vite + Bootstrap 5
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── main.js
        ├── App.vue              # Global navbar + router-view
        ├── router/index.js      # Route guards (role-based)
        ├── stores/auth.js       # Pinia: login / logout / session restore
        ├── api/index.js         # Axios instance + dsarApi / auditApi helpers
        └── views/
            ├── LoginView.vue        # Login form with demo credentials panel
            ├── CustomerView.vue     # Customer dashboard + submit modal
            ├── AdminView.vue        # Admin table + process modal
            └── AuditView.vue        # Compliance audit log table
```

---

## Prerequisites

| Tool | Version |
|---|---|
| Java | 17+ |
| Maven | 3.8+ |
| Node.js | 18+ |
| npm | 9+ |

---

## Running Locally

### 1 — Backend

```bash
cd backend
mvn spring-boot:run
# Starts on http://localhost:8080

# Or build a JAR and run it:
# mvn clean package -DskipTests
# java -jar target/dsar-backend-1.0.0-SNAPSHOT.jar
```

### 2 — Frontend

```bash
cd frontend
npm install
npm run dev
# Opens on http://localhost:5173
```

The Vite dev server proxies `/api/*` → `http://localhost:8080/api/*`, so no CORS issues in dev.

---

## Demo Credentials

| Username | Password | Role |
|---|---|---|
| `Bhargava` | `myinfy124` | **Admin** — can view all requests, process them, and view the audit log |
| `Pavan` | `pavan@123` | **Customer** — can submit requests and view their own |
| `Nagaraju` | `nag@123` | **Customer** |
| `Krishna` | `krishna@123` | **Customer** |

---

## REST API Reference

All endpoints require **HTTP Basic Authentication**.

| Method | Path | Role | Description |
|---|---|---|---|
| `GET` | `/api/auth/me` | Any | Returns current user's username and roles |
| `POST` | `/api/requests` | CUSTOMER | Submit a new DSAR |
| `GET` | `/api/requests` | Any | List requests (own for customer, all for admin) |
| `GET` | `/api/requests/{id}` | Any | Get single request |
| `PUT` | `/api/requests/{id}/status` | ADMIN | Update status + add admin notes |
| `GET` | `/api/audit` | ADMIN | Full audit trail |
| `GET` | `/api/audit/{requestId}` | ADMIN | Audit entries for one request |

### Example: Submit a request

```bash
curl -u Pavan:pavan@123 -X POST http://localhost:8080/api/requests \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail": "pavan@example.com",
    "requestType": "ACCESS",
    "description": "I would like a copy of all personal data you hold on me."
  }'
```

### Example: Admin processes a request

```bash
curl -u Bhargava:myinfy124 -X PUT http://localhost:8080/api/requests/{id}/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "COMPLETED",
    "adminNotes": "Data export sent to pavan@example.com on 2026-03-21."
  }'
```

---

## DSAR Request Lifecycle

```
PENDING  ──►  IN_PROGRESS  ──►  COMPLETED
                            └──►  REJECTED
```

Terminal states (`COMPLETED`, `REJECTED`) cannot be re-opened — an HTTP 400 is returned.

---

## Tech Decisions

- **No Lombok** — Java 17 records used for DTOs; plain classes for mutable domain models — zero annotation processing required.
- **No database** — `ConcurrentHashMap` and `CopyOnWriteArrayList` provide thread-safe in-memory storage.
- **Stateless auth** — `SessionCreationPolicy.STATELESS` ensures each request is independently authenticated.
- **Vue 3 Composition API** — `<script setup>` throughout for concise, readable components.
- **Pinia** — lightweight state management; auth token stored in `sessionStorage` for page-refresh persistence.

---

## AI Tooling Used

This codebase was scaffolded with **Claude (Anthropic)** as an AI pair-programmer, demonstrating GenAI-assisted development as part of the evaluation criteria.
