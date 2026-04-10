package de.salonplan.appointment;

import de.salonplan.common.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/appointments")
@PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
@Validated
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(
            AppointmentService service
    ) {
        this.service = service;
    }

    /**
     * Der Parameter to ist exklusiv.
     *
     * Beispiel:
     * from=2026-08-01 und to=2026-09-01
     * liefert alle Termine im August.
     */
    @GetMapping
    public PageResponse<AppointmentDtos.AppointmentResponse> search(
            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate from,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate to,

            @RequestParam(required = false)
            @Positive(
                    message = "employeeId muss größer als 0 sein"
            )
            Long employeeId,

            @RequestParam(required = false)
            @Positive(
                    message = "customerId muss größer als 0 sein"
            )
            Long customerId,

            @RequestParam(required = false)
            AppointmentStatus status,

            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "startTime",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return service.search(
                from,
                to,
                employeeId,
                customerId,
                status,
                pageable
        );
    }

    @GetMapping("/{id}")
    public AppointmentDtos.AppointmentResponse get(
            @PathVariable
            @Positive(
                    message = "Die ID muss größer als 0 sein"
            )
            Long id
    ) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentDtos.AppointmentResponse create(
            @Valid
            @RequestBody
            AppointmentDtos.CreateAppointmentRequest request,

            Authentication authentication
    ) {
        return service.create(
                request,
                authentication
        );
    }

    @PatchMapping("/{id}/reschedule")
    public AppointmentDtos.AppointmentResponse reschedule(
            @PathVariable
            @Positive
            Long id,

            @Valid
            @RequestBody
            AppointmentDtos.RescheduleAppointmentRequest request,

            Authentication authentication
    ) {
        return service.reschedule(
                id,
                request,
                authentication
        );
    }

    @PatchMapping("/{id}/confirm")
    public AppointmentDtos.AppointmentResponse confirm(
            @PathVariable
            @Positive
            Long id,

            Authentication authentication
    ) {
        return service.confirm(
                id,
                authentication
        );
    }

    @PatchMapping("/{id}/cancel")
    public AppointmentDtos.AppointmentResponse cancel(
            @PathVariable
            @Positive
            Long id,

            @Valid
            @RequestBody
            AppointmentDtos.CancelAppointmentRequest request,

            Authentication authentication
    ) {
        return service.cancel(
                id,
                request,
                authentication
        );
    }

    @PatchMapping("/{id}/complete")
    public AppointmentDtos.AppointmentResponse complete(
            @PathVariable
            @Positive
            Long id,

            Authentication authentication
    ) {
        return service.complete(
                id,
                authentication
        );
    }

    @PatchMapping("/{id}/note")
    public AppointmentDtos.AppointmentResponse changeNote(
            @PathVariable
            @Positive
            Long id,

            @Valid
            @RequestBody
            AppointmentDtos.NoteRequest request,

            Authentication authentication
    ) {
        return service.changeNote(
                id,
                request,
                authentication
        );
    }
}