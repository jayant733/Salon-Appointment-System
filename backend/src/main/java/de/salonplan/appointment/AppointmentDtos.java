package de.salonplan.appointment;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public final class AppointmentDtos {

    private AppointmentDtos() {
    }

    public record CreateAppointmentRequest(

            @NotNull(message = "customerId darf nicht fehlen")
            Long customerId,

            @NotNull(message = "employeeId darf nicht fehlen")
            Long employeeId,

            @NotNull(message = "serviceId darf nicht fehlen")
            Long serviceId,

            @NotNull(message = "startTime darf nicht fehlen")
            @Future(message = "Die Startzeit muss in der Zukunft liegen")
            LocalDateTime startTime,

            @Size(
                    max = 1000,
                    message = "Die Notiz darf höchstens 1000 Zeichen enthalten"
            )
            String note
    ) {
    }

    public record RescheduleAppointmentRequest(

            @NotNull(message = "employeeId darf nicht fehlen")
            Long employeeId,

            @NotNull(message = "serviceId darf nicht fehlen")
            Long serviceId,

            @NotNull(message = "startTime darf nicht fehlen")
            @Future(message = "Die Startzeit muss in der Zukunft liegen")
            LocalDateTime startTime,

            @NotNull(message = "version darf nicht fehlen")
            @PositiveOrZero(message = "version darf nicht negativ sein")
            Long version
    ) {
    }

    public record CancelAppointmentRequest(

            @NotNull(message = "version darf nicht fehlen")
            @PositiveOrZero(message = "version darf nicht negativ sein")
            Long version,

            @Size(
                    max = 500,
                    message = "Die Stornierungsbegründung darf höchstens 500 Zeichen enthalten"
            )
            String reason
    ) {
    }

    public record NoteRequest(

            @Size(
                    max = 1000,
                    message = "Die Notiz darf höchstens 1000 Zeichen enthalten"
            )
            String note
    ) {
    }

    public record AppointmentResponse(
            Long id,
            Long customerId,
            String customerName,
            Long employeeId,
            String employeeName,
            Long serviceId,
            String serviceName,
            BigDecimal price,
            LocalDateTime startTime,
            LocalDateTime endTime,
            AppointmentStatus status,
            String note,
            String cancelReason,
            Instant cancelledAt,
            long version,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}