# SalonPlan Frontend

Angular 22 single-page application for the SalonPlan salon appointment management system.

**Author:** Jayant Sharma ([jayant.sharma2023@vitstudent.ac.in](mailto:jayant.sharma2023@vitstudent.ac.in))

---

## Project Structure

The project workspace layout:

```text
salonplan/
├── backend/       # Spring Boot REST API, Port 8080
└── frontend/      # Angular frontend application, Port 4200
```

---

## Prerequisites

- Node.js `>= 22.22.3` or Node.js 24 LTS
- npm `>= 10.9`
- Running backend service at `http://localhost:8080`

---

## Local Development

From the frontend directory:

```bash
cd frontend
npm install
npm start
```

The application will be served at `http://localhost:4200`. The development proxy configured in `proxy.conf.json` forwards `/api`, `/actuator`, and `/v3` requests to the Spring Boot backend on port 8080.

---

## Production Build

Compile the production distribution:

```bash
npm run build
```

Build artifacts are generated in `dist/frontend/browser/`.

---

## Docker Deployment

Build and run the standalone Nginx production container:

```bash
docker build -t salonplan-frontend .
docker run --rm -p 4200:80 salonplan-frontend
```

The bundled `nginx.conf` expects a backend service named `backend` on port `8080` when deployed in Docker Compose.

---

## Key Modules & Features

- **Authentication & Security:** JWT login, token persistence, HTTP interceptor, and Angular route guards (`AuthGuard`, `AdminGuard`).
- **Interactive Calendar:** Real-time appointment scheduling, date filtering, and status progression.
- **Availability Checker:** Dynamic time slot calculation preventing double-booking based on employee and service durations.
- **Customer Directory:** Full customer CRUD operations with search and appointment history.
- **Administration Panels:** Complete management views for staff members, services catalog, and user permissions.
- **UI & UX:** Modern responsive interface with toast notifications and optimistic concurrency support.

---

## Modern SPA Architecture & Vue.js Interoperability

SalonPlan's frontend follows modern Single Page Application (SPA) design patterns that seamlessly translate across modern JavaScript/TypeScript frameworks such as **Vue.js 3** and **React**.

### Framework Parity: Angular 22 vs. Vue 3

| Concept | Angular 22 Implementation | Vue 3 (Composition API) Equivalent |
|---|---|---|
| **Reactivity** | Angular Signals (`signal()`), RxJS `BehaviorSubject` | Vue 3 `ref()`, `reactive()`, `computed()` |
| **State Sharing** | Injectable services (`providedIn: 'root'`) | Pinia Stores (`defineStore`) and Composables |
| **Component Structure** | TypeScript class with HTML template and CSS/SCSS | `<script setup lang="ts">` Single File Components (SFC) |
| **HTTP Interception** | `HttpInterceptorFn` injecting `Authorization: Bearer <token>` | Axios / Fetch request interceptors |
| **Navigation Protection** | `canActivate` functional route guards (`AuthGuard`, `AdminGuard`) | Vue Router `beforeEach()` navigation guards |

### Vue 3 Reference Client (`frontend/vue-client/`)

A reference implementation illustrating how the SalonPlan Spring Boot REST API is consumed in Vue 3 is provided in [`vue-client/`](./vue-client/):
- **`useAppointments.ts`**: A Vue 3 Composable handling reactive state, 15-minute slot availability queries, and appointment submission.
- **`AppointmentBooking.vue`**: A Vue 3 Single File Component (SFC) implementing reactive stylist selection, real-time availability slot rendering, and appointment booking.
