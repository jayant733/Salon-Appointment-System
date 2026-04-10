package de.salonplan.appointment;

public interface AppointmentNotificationService {
    void appointmentCreated(Appointment appointment);
    void appointmentChanged(Appointment appointment);
    void appointmentCancelled(Appointment appointment);
}