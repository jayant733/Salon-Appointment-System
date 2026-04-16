package de.salonplan.appointment;

import de.salonplan.common.BusinessConflictException;
import de.salonplan.customer.Customer;
import de.salonplan.employee.Employee;
import de.salonplan.salonservice.SalonService;
import de.salonplan.user.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AppointmentTest {

    private Customer customer;
    private Employee employee;
    private Employee replacementEmployee;
    private SalonService salonService;
    private SalonService replacementService;
    private AppUser creator;

    @BeforeEach
    void setUp() {
        customer = mock(Customer.class);
        employee = mock(Employee.class);
        replacementEmployee = mock(Employee.class);
        salonService = mock(SalonService.class);
        replacementService = mock(SalonService.class);
        creator = mock(AppUser.class);
    }

    @Test
    void shouldRescheduleBookedAppointment() {
        Appointment appointment =
                createAppointment();

        LocalDateTime newStart =
                LocalDateTime.of(
                        2026,
                        8,
                        12,
                        14,
                        0
                );

        LocalDateTime newEnd =
                newStart.plusMinutes(90);

        appointment.reschedule(
                replacementEmployee,
                replacementService,
                newStart,
                newEnd
        );

        assertEquals(
                replacementEmployee,
                appointment.getEmployee()
        );

        assertEquals(
                replacementService,
                appointment.getService()
        );

        assertEquals(
                newStart,
                appointment.getStartTime()
        );

        assertEquals(
                newEnd,
                appointment.getEndTime()
        );

        assertEquals(
                AppointmentStatus.BOOKED,
                appointment.getStatus()
        );
    }

    @Test
    void shouldRescheduleConfirmedAppointment() {
        Appointment appointment =
                createAppointment();

        appointment.confirm();

        LocalDateTime newStart =
                LocalDateTime.of(
                        2026,
                        8,
                        13,
                        11,
                        0
                );

        LocalDateTime newEnd =
                newStart.plusMinutes(60);

        appointment.reschedule(
                replacementEmployee,
                replacementService,
                newStart,
                newEnd
        );

        assertEquals(
                AppointmentStatus.CONFIRMED,
                appointment.getStatus()
        );

        assertEquals(
                newStart,
                appointment.getStartTime()
        );
    }

    @Test
    void shouldRejectReschedulingCancelledAppointment() {
        Appointment appointment =
                createAppointment();

        appointment.cancel(
                "Kunde ist verhindert",
                Instant.parse(
                        "2026-08-01T10:00:00Z"
                )
        );

        LocalDateTime newStart =
                LocalDateTime.of(
                        2026,
                        8,
                        14,
                        10,
                        0
                );

        LocalDateTime newEnd =
                newStart.plusMinutes(60);

        assertThrows(
                InvalidAppointmentStatusException.class,
                () -> appointment.reschedule(
                        replacementEmployee,
                        replacementService,
                        newStart,
                        newEnd
                )
        );
    }

    @Test
    void shouldRejectReschedulingCompletedAppointment() {
        Appointment appointment =
                createAppointment();

        appointment.confirm();
        appointment.complete();

        LocalDateTime newStart =
                LocalDateTime.of(
                        2026,
                        8,
                        14,
                        10,
                        0
                );

        LocalDateTime newEnd =
                newStart.plusMinutes(60);

        assertThrows(
                InvalidAppointmentStatusException.class,
                () -> appointment.reschedule(
                        replacementEmployee,
                        replacementService,
                        newStart,
                        newEnd
                )
        );
    }

    @Test
    void shouldCancelAppointmentWithoutDeletingHistory() {
        Appointment appointment =
                createAppointment();

        Instant cancellationTime =
                Instant.parse(
                        "2026-08-01T10:15:00Z"
                );

        appointment.cancel(
                "  Kunde ist krank  ",
                cancellationTime
        );

        assertEquals(
                AppointmentStatus.CANCELLED,
                appointment.getStatus()
        );

        assertEquals(
                "Kunde ist krank",
                appointment.getCancelReason()
        );

        assertEquals(
                cancellationTime,
                appointment.getCancelledAt()
        );

        assertEquals(
                customer,
                appointment.getCustomer()
        );

        assertEquals(
                employee,
                appointment.getEmployee()
        );

        assertEquals(
                salonService,
                appointment.getService()
        );
    }

    @Test
    void cancelledAppointmentShouldNotBlockTime() {
        Appointment appointment =
                createAppointment();

        appointment.cancel(
                null,
                Instant.parse(
                        "2026-08-01T10:15:00Z"
                )
        );

        assertTrue(
                !appointment.getStatus().blocksTime()
        );
    }

    @Test
    void shouldAcceptCurrentVersion() {
        Appointment appointment =
                createAppointment();

        appointment.verifyVersion(0L);
    }

    @Test
    void shouldRejectStaleVersion() {
        Appointment appointment =
                createAppointment();

        assertThrows(
                BusinessConflictException.class,
                () -> appointment.verifyVersion(2L)
        );
    }

    @Test
    void shouldRejectCancellingAlreadyCancelledAppointment() {
        Appointment appointment =
                createAppointment();

        appointment.cancel(
                "Erste Stornierung",
                Instant.parse(
                        "2026-08-01T10:15:00Z"
                )
        );

        assertThrows(
                InvalidAppointmentStatusException.class,
                () -> appointment.cancel(
                        "Zweite Stornierung",
                        Instant.parse(
                                "2026-08-01T10:30:00Z"
                        )
                )
        );
    }

    @Test
    void shouldRejectCancelReasonLongerThanFiveHundredCharacters() {
        Appointment appointment =
                createAppointment();

        String invalidReason =
                "x".repeat(501);

        assertThrows(
                IllegalArgumentException.class,
                () -> appointment.cancel(
                        invalidReason,
                        Instant.parse(
                                "2026-08-01T10:15:00Z"
                        )
                )
        );
    }

    private Appointment createAppointment() {
        LocalDateTime startTime =
                LocalDateTime.of(
                        2026,
                        8,
                        10,
                        10,
                        0
                );

        LocalDateTime endTime =
                startTime.plusMinutes(60);

        return Appointment.book(
                customer,
                employee,
                salonService,
                creator,
                startTime,
                endTime,
                "Testtermin"
        );
    }
}