-- ============================================================
-- SalonPlan
-- Indizes
-- ============================================================


-- ============================================================
-- Kunden
-- Suche nach Nachname und Vorname
-- ============================================================

CREATE INDEX idx_customer_name
    ON customer (
                 last_name,
                 first_name
        );


-- ============================================================
-- Mitarbeiter
-- Suche und Filterung nach Name
-- ============================================================

CREATE INDEX idx_employee_name
    ON employee (
                 last_name,
                 first_name
        );


-- ============================================================
-- Aktive Mitarbeiter
-- ============================================================

CREATE INDEX idx_employee_active
    ON employee (
                 active
        );


-- ============================================================
-- Aktive Leistungen
-- ============================================================

CREATE INDEX idx_salon_service_active
    ON salon_service (
                      active
        );


-- ============================================================
-- Kalenderabfragen eines Mitarbeiters
-- ============================================================

CREATE INDEX idx_appointment_employee_time
    ON appointment (
                    employee_id,
                    start_time,
                    end_time
        );


-- ============================================================
-- Termine eines Kunden
-- ============================================================

CREATE INDEX idx_appointment_customer_time
    ON appointment (
                    customer_id,
                    start_time
        );


-- ============================================================
-- Termine nach Leistung
-- ============================================================

CREATE INDEX idx_appointment_service
    ON appointment (
                    service_id
        );


-- ============================================================
-- Filterung nach Terminstatus
-- ============================================================

CREATE INDEX idx_appointment_status
    ON appointment (
                    status
        );


-- ============================================================
-- Allgemeine Kalenderabfragen
-- ============================================================

CREATE INDEX idx_appointment_start_time
    ON appointment (
                    start_time
        );


-- ============================================================
-- Kombination aus Status und Startzeit
-- ============================================================

CREATE INDEX idx_appointment_status_start_time
    ON appointment (
                    status,
                    start_time
        );