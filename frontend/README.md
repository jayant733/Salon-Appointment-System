# SalonPlan Frontend

Angular 22 frontend for the SalonPlan salon appointment management system.

**Author:** Jayant Sharma (<jayant.sharma2023@vitstudent.ac.in>)

## Project Structure

The project workspace layout:

```text
salonplan/
├── backend/       # Spring Boot, Port 8080
└── frontend/      # Angular frontend application, Port 4200
```

## Prerequisites

- Node.js `>= 22.22.3` or Node.js 24 LTS
- npm `>= 10.9`
- Running backend at `http://localhost:8080`

## Local Development

From the frontend directory:

```bash
cd frontend
npm install
npm start
```

The application will be available at `http://localhost:4200`. `proxy.conf.json` forwards `/api`, `/actuator`, and `/v3` requests to the backend.

## Production Build

```bash
npm run build
```

Build output is generated in `dist/frontend/browser/`.

## Docker Deployment

```bash
docker build -t salonplan-frontend .
docker run --rm -p 4200:80 salonplan-frontend
```

The bundled `nginx.conf` expects a backend service named `backend` on port `8080` in Docker Compose.

## Features Included

- JWT login and session storage
- Auth interceptor and route guards
- Interactive appointment calendar with filtering and status actions
- Appointment booking and rescheduling with slot availability check
- Customer management
- Employee and salon service administration
- Role and user status management
- Responsive UI and notification alerts

---

## Deutsch

Angular-22-Frontend für das SalonPlan-Spring-Boot-Backend.

### Lokal starten

```bash
cd frontend
npm install
npm start
```

Die Anwendung ist danach unter `http://localhost:4200` erreichbar. `proxy.conf.json` leitet `/api`, `/actuator` und `/v3` an das Backend weiter.
