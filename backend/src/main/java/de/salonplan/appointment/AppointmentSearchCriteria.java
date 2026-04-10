package de.salonplan.appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public record AppointmentSearchCriteria(
        LocalDateTime fromInclusive,
        LocalDateTime toExclusive,
        Long employeeId,
        Long customerId,
        AppointmentStatus status
) {

    public AppointmentSearchCriteria {
        Objects.requireNonNull(
                fromInclusive,
                "fromInclusive darf nicht null sein."
        );

        Objects.requireNonNull(
                toExclusive,
                "toExclusive darf nicht null sein."
        );

        if (!toExclusive.isAfter(fromInclusive)) {
            throw new IllegalArgumentException(
                    "to muss zeitlich nach from liegen."
            );
        }

        validateId(
                employeeId,
                "employeeId"
        );

        validateId(
                customerId,
                "customerId"
        );
    }

    public static AppointmentSearchCriteria fromDates(
            LocalDate from,
            LocalDate to,
            Long employeeId,
            Long customerId,
            AppointmentStatus status
    ) {
        Objects.requireNonNull(
                from,
                "Der Parameter from darf nicht fehlen."
        );

        Objects.requireNonNull(
                to,
                "Der Parameter to darf nicht fehlen."
        );

        if (!to.isAfter(from)) {
            throw new IllegalArgumentException(
                    "to muss nach from liegen. "
                            + "Der Parameter to ist exklusiv."
            );
        }

        return new AppointmentSearchCriteria(
                from.atStartOfDay(),
                to.atStartOfDay(),
                employeeId,
                customerId,
                status
        );
    }

    private static void validateId(
            Long id,
            String fieldName
    ) {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException(
                    fieldName
                            + " muss größer als 0 sein."
            );
        }
    }
}