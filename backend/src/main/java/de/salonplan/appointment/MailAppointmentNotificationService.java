package de.salonplan.appointment;

import de.salonplan.config.MailProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "salonplan.mail", name = "enabled", havingValue = "true")
public class MailAppointmentNotificationService implements AppointmentNotificationService {
    private final JavaMailSender mailSender;
    private final MailProperties properties;

    public MailAppointmentNotificationService(JavaMailSender mailSender, MailProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void appointmentCreated(Appointment appointment) {
        send(appointment, "Termin wurde gebucht");
    }

    @Override
    public void appointmentChanged(Appointment appointment) {
        send(appointment, "Termin wurde geändert");
    }

    @Override
    public void appointmentCancelled(Appointment appointment) {
        send(appointment, "Termin wurde storniert");
    }

    private void send(Appointment appointment, String subject) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.from());
        message.setTo(appointment.getCustomer().getEmail());
        message.setSubject("SalonPlan: " + subject);
        message.setText("""
                Hallo %s,

                %s
                Leistung: %s
                Beginn: %s
                Ende: %s
                Status: %s
                """.formatted(
                appointment.getCustomer().getFirstName(),
                subject,
                appointment.getService().getName(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus()
        ));
        mailSender.send(message);
    }
}