package de.salonplan.appointment;

public enum AppointmentStatus {

    BOOKED,
    CONFIRMED,
    COMPLETED,
    CANCELLED;

    /**
     * Ein stornierter Termin blockiert keinen Zeitraum mehr.
     */
    public boolean blocksTime() {
        return this != CANCELLED;
    }

    /**
     * Abgeschlossene und stornierte Termine dürfen nicht mehr
     * normal bearbeitet werden.
     */
    public boolean isFinalStatus() {
        return this == COMPLETED || this == CANCELLED;
    }
}