ALTER TABLE appointment
    ADD COLUMN cancel_reason VARCHAR(500);

ALTER TABLE appointment
    ADD COLUMN cancelled_at TIMESTAMPTZ;