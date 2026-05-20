# SalonPlan

[English](#english) · [Deutsch](#deutsch) · [Русский](#русский)

SalonPlan is a full-stack salon appointment management application designed and developed by **Jayant Sharma**. It provides a modern Spring Boot backend, an Angular frontend, PostgreSQL/Liquibase persistence, JWT security, and automated tests.

---

# English

## Overview

SalonPlan manages customers, employees, salon services, user accounts, and appointments. It prevents double bookings, implements an appointment status workflow, and protects operations with `ADMIN` and `EMPLOYEE` roles.

| Area | Technology |
|---|---|
| Backend | Java 17, Spring Boot 4.1.0, Maven Wrapper |
| Frontend | Angular 22.0.7, TypeScript 6.0.2, RxJS 7.8 |
| Database | PostgreSQL 17, Spring Data JPA, Liquibase |
| Security | Spring Security, JWT HS256, BCrypt |
| Tests | JUnit, MockMvc, Testcontainers, Angular Test Runner |

## Requirements

- JDK 17
- Docker with Docker Compose
- Node.js `>= 22.22.3`
- npm `>= 10.9`

## Quick start

### 1. Start PostgreSQL

```bash
docker compose -f compose.dev.yml up -d
docker compose -f compose.dev.yml ps
```

### 2. Start the backend

Configure your database connection and secret parameters:

```bash
cd backend
export DB_URL='jdbc:postgresql://localhost:5432/salonplan'
export DB_USER='salon'
export DB_PASSWORD='salon'
export JWT_SECRET='replace-with-at-least-32-characters'
./mvnw spring-boot:run
```

### 3. Start the frontend

```bash
cd frontend
npm ci
npm start
```

## URLs

- Frontend: `http://localhost:4200`
- Backend: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`

## Development accounts

| Role | E-mail | Password |
|---|---|---|
| Administrator | `admin@salonplan.local` | `admin-change-me` |
| Employee | `employee@salonplan.local` | `employee-change-me` |

These values are for development and testing environments.

## Optional Mailpit

Mail delivery is disabled by default. For local testing:

```bash
docker run --name salonplan-mailpit \
  -p 1025:1025 -p 8025:8025 \
  -d axllent/mailpit

export MAIL_ENABLED=true
export SPRING_MAIL_HOST=localhost
export SPRING_MAIL_PORT=1025
```

Mailpit UI: `http://localhost:8025`

## Build and test

```bash
# Backend
cd backend
./mvnw clean test
./mvnw clean verify

# Frontend
cd frontend
npm ci
npm test -- --watch=false
npm run build
```

## Key business rules

- Opening hours: `08:00` to `19:00`
- Availability uses a 15-minute grid
- Appointment end is calculated from service duration
- Inactive master data cannot be used for new appointments
- No overlapping interval for the same employee
- Optimistic locking version for rescheduling and cancellation
- Status paths: `BOOKED → CONFIRMED → COMPLETED` and `BOOKED/CONFIRMED → CANCELLED`

---

# Deutsch

## Überblick

SalonPlan verwaltet Kunden, Mitarbeitende, Salonleistungen, Benutzerkonten und Termine. Die Anwendung verhindert Doppelbuchungen, unterstützt einen Terminstatus-Workflow und schützt Funktionen mit den Rollen `ADMIN` und `EMPLOYEE`.

| Bereich | Technologie |
|---|---|
| Backend | Java 17, Spring Boot 4.1.0, Maven Wrapper |
| Frontend | Angular 22.0.7, TypeScript 6.0.2, RxJS 7.8 |
| Datenbank | PostgreSQL 17, Spring Data JPA, Liquibase |
| Sicherheit | Spring Security, JWT HS256, BCrypt |
| Tests | JUnit, MockMvc, Testcontainers, Angular Test Runner |

## Voraussetzungen

- JDK 17
- Docker mit Docker Compose
- Node.js `>= 22.22.3`
- npm `>= 10.9`

## Schnellstart

### 1. PostgreSQL starten

```bash
docker compose -f compose.dev.yml up -d
docker compose -f compose.dev.yml ps
```

### 2. Backend starten

Für den lokalen Start `DB_URL` auf die lokale PostgreSQL-Instanz setzen:

```bash
cd backend
export DB_URL='jdbc:postgresql://localhost:5432/salonplan'
export DB_USER='salon'
export DB_PASSWORD='salon'
export JWT_SECRET='replace-with-at-least-32-characters'
./mvnw spring-boot:run
```

### 3. Frontend starten

```bash
cd frontend
npm ci
npm start
```

## Adressen

- Frontend: `http://localhost:4200`
- Backend: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`

## Entwicklungszugänge

| Rolle | E-Mail | Passwort |
|---|---|---|
| Administrator | `admin@salonplan.local` | `admin-change-me` |
| Mitarbeiter | `employee@salonplan.local` | `employee-change-me` |

Diese Werte sind ausschließlich für die Entwicklung vorgesehen und müssen vor einer realen Nutzung geändert werden.

## Optional: Mailpit

E-Mail-Versand ist standardmäßig deaktiviert. Zum lokalen Testen kann Mailpit gestartet werden:

```bash
docker run --name salonplan-mailpit \
  -p 1025:1025 -p 8025:8025 \
  -d axllent/mailpit

export MAIL_ENABLED=true
export SPRING_MAIL_HOST=localhost
export SPRING_MAIL_PORT=1025
```

Mailpit Web UI: `http://localhost:8025`

## Build und Tests

```bash
# Backend
cd backend
./mvnw clean test
./mvnw clean verify

# Frontend
cd frontend
npm ci
npm test -- --watch=false
npm run build
```

## Wichtige Geschäftsregeln

- Öffnungszeiten: `08:00` bis `19:00`
- Verfügbarkeit im 15-Minuten-Raster
- Terminende ergibt sich aus der Leistungsdauer
- Keine neuen Termine mit inaktiven Stammdaten
- Keine Zeitüberschneidung für denselben Mitarbeitenden
- Optimistische Version bei Verschieben und Stornieren
- Statuspfade: `BOOKED → CONFIRMED → COMPLETED` sowie `BOOKED/CONFIRMED → CANCELLED`

---

# Русский

## Обзор

SalonPlan управляет клиентами, сотрудниками, услугами салона, учётными записями и записями на приём. Система предотвращает двойные записи, поддерживает жизненный цикл статусов и защищает операции ролями `ADMIN` и `EMPLOYEE`.

| Область | Технология |
|---|---|
| Backend | Java 17, Spring Boot 4.1.0, Maven Wrapper |
| Frontend | Angular 22.0.7, TypeScript 6.0.2, RxJS 7.8 |
| База данных | PostgreSQL 17, Spring Data JPA, Liquibase |
| Безопасность | Spring Security, JWT HS256, BCrypt |
| Тесты | JUnit, MockMvc, Testcontainers, Angular Test Runner |

## Требования

- JDK 17
- Docker с Docker Compose
- Node.js `>= 22.22.3`
- npm `>= 10.9`

## Быстрый запуск

### 1. Запустить PostgreSQL

```bash
docker compose -f compose.dev.yml up -d
docker compose -f compose.dev.yml ps
```

### 2. Запустить backend

Для локального запуска укажите `DB_URL`:

```bash
cd backend
export DB_URL='jdbc:postgresql://localhost:5432/salonplan'
export DB_USER='salon'
export DB_PASSWORD='salon'
export JWT_SECRET='replace-with-at-least-32-characters'
./mvnw spring-boot:run
```

### 3. Запустить frontend

```bash
cd frontend
npm ci
npm start
```

## Адреса

- Frontend: `http://localhost:4200`
- Backend: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`

## Учётные записи разработки

| Роль | E-mail | Пароль |
|---|---|---|
| Администратор | `admin@salonplan.local` | `admin-change-me` |
| Сотрудник | `employee@salonplan.local` | `employee-change-me` |

Эти значения предназначены только для разработки и должны быть заменены перед реальным использованием.

## Опционально: Mailpit

Отправка писем по умолчанию отключена. Для локального теста:

```bash
docker run --name salonplan-mailpit \
  -p 1025:1025 -p 8025:8025 \
  -d axllent/mailpit

export MAIL_ENABLED=true
export SPRING_MAIL_HOST=localhost
export SPRING_MAIL_PORT=1025
```

Интерфейс Mailpit: `http://localhost:8025`

## Сборка и тесты

```bash
# Backend
cd backend
./mvnw clean test
./mvnw clean verify

# Frontend
cd frontend
npm ci
npm test -- --watch=false
npm run build
```

## Основные бизнес-правила

- Часы работы: `08:00`–`19:00`
- Доступность с шагом 15 минут
- Окончание вычисляется по длительности услуги
- Неактивные справочные записи нельзя использовать для новой записи
- Интервалы одного сотрудника не пересекаются
- Optimistic version при переносе и отмене
- Статусы: `BOOKED → CONFIRMED → COMPLETED` и `BOOKED/CONFIRMED → CANCELLED`

---

## Author & Maintainer

- **Developer:** Jayant Sharma
- **Email:** jayant.sharma2023@vitstudent.ac.in
- **Project:** SalonPlan Full-Stack Appointment Management System
