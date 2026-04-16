package de.salonplan.appointment;

import de.salonplan.customer.Customer;
import de.salonplan.customer.CustomerRepository;
import de.salonplan.employee.Employee;
import de.salonplan.employee.EmployeeRepository;
import de.salonplan.salonservice.SalonService;
import de.salonplan.salonservice.SalonServiceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AppointmentRepositoryTest extends PostgresIntegrationTest {

    private final AppointmentRepository appointments;
    private final CustomerRepository customers;
    private final EmployeeRepository employees;
    private final SalonServiceRepository services;

    @Autowired
    AppointmentRepositoryTest(
            AppointmentRepository appointments,
            CustomerRepository customers,
            EmployeeRepository employees,
            SalonServiceRepository services
    ) {
        this.appointments = appointments;
        this.customers = customers;
        this.employees = employees;
        this.services = services;
    }

    @Test
    void detectsOverlappingAppointmentButIgnoresCancelledAppointment() {
        Customer customer = customers.save(
                new Customer(
                        "Mia",
                        "Muster",
                        "mia.repository@example.test",
                        "0123"
                )
        );

        Employee employee = employees.save(
                new Employee(
                        "Eva",
                        "Schnitt",
                        "eva.repository@example.test"
                )
        );

        SalonService salonService = services.save(
                new SalonService(
                        "Testleistung",
                        "Repository-Test",
                        60,
                        new BigDecimal("40.00")
                )
        );

        LocalDateTime start = LocalDateTime.of(
                2030,
                6,
                10,
                10,
                0
        );

        Appointment appointment = Appointment.book(
                customer,
                employee,
                salonService,
                null,
                start,
                start.plusHours(1),
                null
        );

        appointments.saveAndFlush(appointment);

        boolean conflictBeforeCancellation =
                appointments.existsConflict(
                        employee.getId(),
                        start.plusMinutes(30),
                        start.plusMinutes(90),
                        null
                );

        assertThat(conflictBeforeCancellation)
                .isTrue();

        appointment.cancel(
                "Stornierung im Repository-Test",
                Instant.parse("2030-06-01T10:00:00Z")
        );

        appointments.flush();

        assertThat(appointment.getStatus())
                .isEqualTo(AppointmentStatus.CANCELLED);

        boolean conflictAfterCancellation =
                appointments.existsConflict(
                        employee.getId(),
                        start.plusMinutes(30),
                        start.plusMinutes(90),
                        null
                );

        assertThat(conflictAfterCancellation)
                .isFalse();
    }
}