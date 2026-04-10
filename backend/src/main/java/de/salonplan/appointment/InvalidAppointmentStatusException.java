package de.salonplan.appointment;

public class InvalidAppointmentStatusException extends RuntimeException {

    public InvalidAppointmentStatusException(String message) {
        super(message);
    }
}