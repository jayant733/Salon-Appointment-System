package de.salonplan.appointment;

import de.salonplan.common.AuditService;
import de.salonplan.common.BusinessConflictException;
import de.salonplan.customer.Customer;
import de.salonplan.customer.CustomerService;
import de.salonplan.employee.Employee;
import de.salonplan.employee.EmployeeService;
import de.salonplan.salonservice.SalonService;
import de.salonplan.salonservice.SalonServiceService;
import de.salonplan.user.AppUser;
import de.salonplan.user.AppUserRepository;
import de.salonplan.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AppointmentServiceTest {
    AppointmentRepository appointments = mock(AppointmentRepository.class);
    CustomerService customers = mock(CustomerService.class);
    EmployeeService employees = mock(EmployeeService.class);
    SalonServiceService services = mock(SalonServiceService.class);
    AppUserRepository users = mock(AppUserRepository.class);
    AppointmentNotificationService notifications = mock(AppointmentNotificationService.class);
    AuditService audit = mock(AuditService.class);
    Clock clock = Clock.fixed(Instant.parse("2030-01-01T08:00:00Z"), ZoneOffset.UTC);

    AppointmentService service;

    @BeforeEach
    void setUp() {
        service = new AppointmentService(
                appointments, customers, employees, services, users,
                notifications, audit, clock
        );
    }

    @Test
    void rejectsOverlappingAppointment() {
        Customer customer = new Customer("Mia", "Muster", "mia@example.test", null);
        Employee employee = new Employee("Eva", "Schnitt", "eva@example.test");
        SalonService salonService = new SalonService(
                "Schnitt", null, 60, new BigDecimal("40.00")
        );
        AppUser user = new AppUser(
                "employee@example.test", "hash", UserRole.EMPLOYEE
        );

        when(customers.getEntity(1L)).thenReturn(customer);
        when(employees.getActiveEntityForUpdate(2L)).thenReturn(employee);
        when(services.getActiveEntity(3L)).thenReturn(salonService);
        when(users.findByEmailIgnoreCase("employee@example.test")).thenReturn(Optional.of(user));
        when(appointments.existsConflict(
                isNull(),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                isNull()
        )).thenReturn(true);

        AppointmentDtos.CreateAppointmentRequest request =
                new AppointmentDtos.CreateAppointmentRequest(
                        1L, 2L, 3L,
                        LocalDateTime.of(2030, 1, 2, 10, 0),
                        null
                );

        assertThatThrownBy(() -> service.create(
                request,
                new UsernamePasswordAuthenticationToken("employee@example.test", "n/a")
        ))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("already has an appointment");

        verify(appointments, never()).save(any());
        verifyNoInteractions(notifications);
    }
}