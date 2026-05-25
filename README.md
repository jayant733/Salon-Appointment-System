# SalonPlan - Salon Appointment Management System

**SalonPlan** is a modern, full-stack salon appointment scheduling and management platform designed and engineered by **Jayant Sharma**. The application provides an enterprise-ready architecture featuring a Spring Boot 4 REST API, an Angular 22 single-page application, PostgreSQL persistence with Liquibase version-controlled migrations, JWT-based security, and end-to-end automated testing.

---

## Author & Maintainer

- **Developer:** Jayant Sharma
- **Email:** [jayant.sharma2023@vitstudent.ac.in](mailto:jayant.sharma2023@vitstudent.ac.in)
- **Repository:** [Salon-Appointment-System](https://github.com/jayant733/Salon-Appointment-System)

---

## Technology Stack

| Layer | Technologies & Frameworks | Description |
|---|---|---|
| **Backend** | Java 17, Spring Boot 4.1.0, Spring Data JPA, Spring Security, Maven | Modular RESTful API handling scheduling, auth, and business logic |
| **Frontend** | Angular 22.0.7, TypeScript 6.0.2, RxJS 7.8, SCSS | Reactive, accessible single-page web application with responsive UI |
| **Database** | PostgreSQL 17, Liquibase | Relational persistence with automated schema versioning and indexing |
| **Security** | Spring Security 6, JWT (HMAC-SHA256), BCrypt | Stateless token-based authentication and role-based access control |
| **Testing** | JUnit 5, MockMvc, Testcontainers, Vitest | Unit, slice, integration, and UI component test suites |
| **DevOps** | Docker, Docker Compose, Nginx | Containerized PostgreSQL development environment and production Nginx image |

---

## Core Features

- **Interactive Appointment Scheduling:**
  - Dynamic slot calculation on a strict 15-minute grid between 08:00 and 19:00.
  - Automatic collision and double-booking prevention per employee.
  - End-time calculation driven dynamically by selected salon service duration.
  - Lifecycle state machine: `BOOKED → CONFIRMED → COMPLETED` and `BOOKED/CONFIRMED → CANCELLED`.
  - Optimistic locking (`@Version`) to prevent concurrent modification conflicts during reschedule or cancellation.

- **Role-Based Access Control (RBAC):**
  - **`ROLE_ADMIN`:** Full management over employees, services, users, and customer master data.
  - **`ROLE_EMPLOYEE`:** Operational access to manage daily appointments, view schedules, and update statuses.
  - Stateless JWT token transmission with automatic refresh interceptors in Angular.

- **Master Data Management:**
  - **Customers:** Profile registration, contact details, notes, and appointment history.
  - **Employees:** Work schedules, active/inactive statuses, and associated services.
  - **Salon Services:** Service catalogue, pricing, duration (in minutes), and active state flags.
  - **Users:** System accounts, credentials, enabled/disabled states, and role assignments.

- **Notifications & Audit:**
  - Pluggable notification service supporting Console Logging and Mailpit SMTP dispatch.
  - Audited operational logs for appointment updates and cancellations.

---

## System Architecture

```text
salonplan-spring-angular/
├── backend/
│   ├── src/main/java/de/salonplan/
│   │   ├── appointment/        # Booking, availability calculation & notifications
│   │   ├── auth/               # Login, JWT issuing, authentication controllers
│   │   ├── common/             # Global error handler, exceptions, page responses
│   │   ├── config/             # Security, JWT, CORS, OpenAPI, Clock configuration
│   │   ├── customer/           # Customer entities, repositories & controllers
│   │   ├── employee/           # Employee management and scheduling
│   │   ├── salonservice/       # Salon service catalogue
│   │   └── user/               # AppUser security credentials and roles
│   ├── src/main/resources/
│   │   ├── db/changelog/       # Liquibase SQL and YAML migration changesets
│   │   └── application*.yml    # Configuration profiles (dev, prod, test)
│   └── pom.xml
├── frontend/
│   ├── src/app/
│   │   ├── core/               # Guards, interceptors, API clients, token store
│   │   └── features/           # Appointments, customers, employees, services, login
│   ├── angular.json
│   ├── package.json
│   └── proxy.conf.json         # Dev proxy forwarding to port 8080
├── compose.dev.yml             # Docker Compose configuration for PostgreSQL 17
└── README.md
```

---

## Prerequisites

- **Java Development Kit (JDK):** Version 17 or higher
- **Node.js:** `>= 22.22.3` or Node.js 24 LTS
- **npm:** `>= 10.9`
- **Docker:** Docker Desktop or Docker Engine with Docker Compose support

---

## Quick Start Guide

### 1. Launch PostgreSQL Database

Start the development PostgreSQL container using Docker Compose:

```bash
docker compose -f compose.dev.yml up -d
docker compose -f compose.dev.yml ps
```

### 2. Start the Spring Boot Backend

Navigate to the `backend` directory and execute with Maven Wrapper:

```bash
cd backend
export DB_URL='jdbc:postgresql://localhost:5432/salonplan'
export DB_USER='salon'
export DB_PASSWORD='salon'
export JWT_SECRET='replace-with-at-least-32-characters-secret-key'
./mvnw spring-boot:run
```

*(On Windows PowerShell, use `$env:DB_URL="jdbc:postgresql://localhost:5432/salonplan"`, etc.)*

### 3. Start the Angular Frontend

In a separate terminal, navigate to the `frontend` directory:

```bash
cd frontend
npm ci
npm start
```

The frontend application will be live at `http://localhost:4200`.

---

## Application URLs & Endpoints

| Resource | URL | Description |
|---|---|---|
| **Frontend UI** | `http://localhost:4200` | Single-page application interface |
| **Backend API** | `http://localhost:8080` | Spring Boot REST API |
| **Swagger UI** | `http://localhost:8080/swagger-ui.html` | Interactive OpenAPI documentation |
| **OpenAPI Specification** | `http://localhost:8080/v3/api-docs` | Raw OpenAPI v3 JSON spec |
| **Actuator Health** | `http://localhost:8080/actuator/health` | Service health status |

---

## Default Development Accounts

The database is pre-seeded with initial development credentials:

| Role | Email | Password | Allowed Scopes |
|---|---|---|---|
| **Administrator** | `admin@salonplan.local` | `admin-change-me` | Full administrative permissions (`ROLE_ADMIN`, `ROLE_EMPLOYEE`) |
| **Employee** | `employee@salonplan.local` | `employee-change-me` | Daily appointment operations (`ROLE_EMPLOYEE`) |

> *Note: Credentials are intended for local development and testing only.*

---

## Optional: Email Testing with Mailpit

Email notifications are disabled by default. To test appointment confirmation and cancellation emails locally using Mailpit:

```bash
docker run --name salonplan-mailpit \
  -p 1025:1025 -p 8025:8025 \
  -d axllent/mailpit

export MAIL_ENABLED=true
export SPRING_MAIL_HOST=localhost
export SPRING_MAIL_PORT=1025
```

Access the Mailpit web interface at `http://localhost:8025`.

---

## Business Rules & Validation

1. **Salon Opening Hours:** Daily operating window is between `08:00` and `19:00`.
2. **Time Grid Resolution:** All appointments and available slots align to `15-minute` intervals.
3. **Dynamic Duration:** An appointment's `end_time` is strictly derived from `start_time + service.duration_minutes`.
4. **Collision Guard:** No employee can have overlapping active appointments.
5. **Master Data Integrity:** Inactive employees or inactive services cannot be selected for new bookings.
6. **Concurrency Protection:** Rescheduling and cancellation verify the entity `@Version` column to prevent lost updates.
7. **Status Workflow:**
   - Standard progression: `BOOKED` → `CONFIRMED` → `COMPLETED`
   - Cancellation: `BOOKED` or `CONFIRMED` → `CANCELLED`

---

## Testing & Quality Assurance

### Backend Tests

Execute unit tests, repository slice tests, and Testcontainers integration tests:

```bash
cd backend
./mvnw clean test
./mvnw clean verify
```

### Frontend Tests & Production Build

Run the frontend test suite and generate production bundles:

```bash
cd frontend
npm ci
npm test -- --watch=false
npm run build
```

Production artifacts are compiled into `frontend/dist/frontend/browser/`.

---

## Production Docker Deployment

A multi-stage `Dockerfile` and `nginx.conf` are provided to run the frontend behind an Nginx reverse proxy:

```bash
cd frontend
docker build -t salonplan-frontend .
docker run --rm -p 4200:80 salonplan-frontend
```

---

## License & Attribution

Designed and implemented by **Jayant Sharma** ([jayant.sharma2023@vitstudent.ac.in](mailto:jayant.sharma2023@vitstudent.ac.in)).
Distributed for salon appointment management and portfolio demonstrations.
