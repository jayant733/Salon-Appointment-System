package de.salonplan.appointment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        prefix = "salonplan.mail",
        name = "enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class LoggingAppointmentNotificationService implements AppointmentNotificationService {
    private static final Logger log = LoggerFactory.getLogger(LoggingAppointmentNotificationService.class);

    @Override
    public void appointmentCreated(Appointment appointment) {
        log.info("Appointment {} created for {}", appointment.getId(), appointment.getCustomer().getEmail());
    }

    @Override
    public void appointmentChanged(Appointment appointment) {
        log.info("Appointment {} changed", appointment.getId());
    }

    @Override
    public void appointmentCancelled(Appointment appointment) {
        log.info("Appointment {} cancelled", appointment.getId());
    }
}