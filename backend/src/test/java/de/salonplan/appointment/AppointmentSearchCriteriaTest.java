package de.salonplan.appointment;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppointmentSearchCriteriaTest {

    @Test
    void shouldCreateHalfOpenDateRange() {
        AppointmentSearchCriteria criteria =
                AppointmentSearchCriteria.fromDates(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 9, 1),
                        5L,
                        8L,
                        AppointmentStatus.CONFIRMED
                );

        assertEquals(
                LocalDateTime.of(
                        2026,
                        8,
                        1,
                        0,
                        0
                ),
                criteria.fromInclusive()
        );

        assertEquals(
                LocalDateTime.of(
                        2026,
                        9,
                        1,
                        0,
                        0
                ),
                criteria.toExclusive()
        );

        assertEquals(
                5L,
                criteria.employeeId()
        );

        assertEquals(
                8L,
                criteria.customerId()
        );

        assertEquals(
                AppointmentStatus.CONFIRMED,
                criteria.status()
        );
    }

    @Test
    void shouldAllowOptionalFiltersToBeNull() {
        AppointmentSearchCriteria criteria =
                AppointmentSearchCriteria.fromDates(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 2),
                        null,
                        null,
                        null
                );

        assertNull(
                criteria.employeeId()
        );

        assertNull(
                criteria.customerId()
        );

        assertNull(
                criteria.status()
        );
    }

    @Test
    void shouldRejectEqualFromAndToDate() {
        LocalDate date =
                LocalDate.of(
                        2026,
                        8,
                        1
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> AppointmentSearchCriteria.fromDates(
                        date,
                        date,
                        null,
                        null,
                        null
                )
        );
    }

    @Test
    void shouldRejectToBeforeFrom() {
        assertThrows(
                IllegalArgumentException.class,
                () -> AppointmentSearchCriteria.fromDates(
                        LocalDate.of(2026, 8, 10),
                        LocalDate.of(2026, 8, 1),
                        null,
                        null,
                        null
                )
        );
    }

    @Test
    void shouldRejectNegativeEmployeeId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> AppointmentSearchCriteria.fromDates(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 2),
                        -1L,
                        null,
                        null
                )
        );
    }

    @Test
    void shouldRejectZeroCustomerId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> AppointmentSearchCriteria.fromDates(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 2),
                        null,
                        0L,
                        null
                )
        );
    }
}