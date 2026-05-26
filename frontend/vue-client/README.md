# SalonPlan Vue.js Client Reference

This directory provides a **Vue 3** reference architecture for the SalonPlan salon appointment scheduling system.

While the primary production frontend is implemented using Angular 22, SalonPlan's backend was designed as a framework-agnostic, decoupled Spring Boot REST API. This module demonstrates how the exact same backend endpoints (`/api/appointments`, `/api/availability`) are consumed using modern Vue 3 patterns.

---

## Architectural Parity: Angular vs. Vue.js

| Concept | Angular 22 Implementation | Vue 3 Equivalent |
|---|---|---|
| **Reactivity** | Angular Signals (`signal()`), RxJS `BehaviorSubject` | `ref()`, `reactive()`, `computed()` |
| **State Sharing** | `@Injectable({ providedIn: 'root' })` Services | Pinia Stores (`defineStore`) or Composables (`useAppointments`) |
| **Component Syntax** | TypeScript class with template & decorator | `<script setup lang="ts">` Single File Components (SFC) |
| **HTTP Layer** | `HttpClient` with functional `HttpInterceptorFn` | Axios / native Fetch with Bearer token header interceptor |
| **Navigation Guards**| `canActivate: [AuthGuard]` | `router.beforeEach()` navigation guard |

---

## Included Files

- **`useAppointments.ts`**: Vue 3 Composable encapsulating appointment state, 15-minute slot availability queries, and appointment booking requests.
- **`AppointmentBooking.vue`**: Vue 3 Single File Component (SFC) with reactive date/stylist selection and interactive 15-minute slot grid.
