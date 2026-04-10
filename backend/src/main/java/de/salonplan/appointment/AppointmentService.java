package de.salonplan.appointment;

import de.salonplan.common.AuditService;
import de.salonplan.common.BusinessConflictException;
import de.salonplan.common.NotFoundException;
import de.salonplan.common.PageResponse;
import de.salonplan.customer.Customer;
import de.salonplan.customer.CustomerService;
import de.salonplan.employee.Employee;
import de.salonplan.employee.EmployeeService;
import de.salonplan.salonservice.SalonService;
import de.salonplan.salonservice.SalonServiceService;
import de.salonplan.user.AppUser;
import de.salonplan.user.AppUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@Transactional(readOnly = true)
public class AppointmentService {

    private static final LocalTime OPENING_TIME =
            LocalTime.of(8, 0);

    private static final LocalTime CLOSING_TIME =
            LocalTime.of(19, 0);

    private final AppointmentRepository appointments;
    private final CustomerService customers;
    private final EmployeeService employees;
    private final SalonServiceService services;
    private final AppUserRepository users;
    private final AppointmentNotificationService notifications;
    private final AuditService audit;
    private final Clock clock;

    public AppointmentService(
            AppointmentRepository appointments,
            CustomerService customers,
            EmployeeService employees,
            SalonServiceService services,
            AppUserRepository users,
            AppointmentNotificationService notifications,
            AuditService audit,
            Clock clock
    ) {
        this.appointments = appointments;
        this.customers = customers;
        this.employees = employees;
        this.services = services;
        this.users = users;
        this.notifications = notifications;
        this.audit = audit;
        this.clock = clock;
    }

    public PageResponse<AppointmentDtos.AppointmentResponse> search(
            LocalDate from,
            LocalDate to,
            Long employeeId,
            Long customerId,
            AppointmentStatus status,
            Pageable pageable
    ) {
        AppointmentSearchCriteria criteria =
                AppointmentSearchCriteria.fromDates(
                        from,
                        to,
                        employeeId,
                        customerId,
                        status
                );

        Pageable safePageable =
                AppointmentPageRequest.sanitize(
                        pageable
                );

        Page<AppointmentDtos.AppointmentResponse> resultPage =
                appointments
                        .findAll(
                                appointments.createSpecification(
                                        criteria
                                ),
                                safePageable
                        )
                        .map(this::toResponse);

        return PageResponse.from(resultPage);
    }

    public AppointmentDtos.AppointmentResponse get(
            Long id
    ) {
        return toResponse(
                getEntity(id)
        );
    }

    @Transactional
    public AppointmentDtos.AppointmentResponse create(
            AppointmentDtos.CreateAppointmentRequest request,
            Authentication authentication
    ) {
        Customer customer =
                customers.getEntity(
                        request.customerId()
                );

        if (!customer.isActive()) {
            throw new BusinessConflictException(
                    "CUSTOMER_INACTIVE",
                    "Der ausgewählte Kunde ist deaktiviert."
            );
        }

        Employee employee =
                employees.getActiveEntityForUpdate(
                        request.employeeId()
                );

        SalonService salonService =
                services.getActiveEntity(
                        request.serviceId()
                );

        LocalDateTime startTime =
                request.startTime();

        LocalDateTime endTime =
                calculateEndTime(
                        startTime,
                        salonService
                );

        validateSchedule(
                startTime,
                endTime
        );

        ensureNoConflict(
                employee.getId(),
                startTime,
                endTime,
                null
        );

        AppUser creator =
                getAuthenticatedUser(
                        authentication
                );

        Appointment appointment =
                Appointment.book(
                        customer,
                        employee,
                        salonService,
                        creator,
                        startTime,
                        endTime,
                        request.note()
                );

        Appointment saved =
                appointments.saveAndFlush(
                        appointment
                );

        audit.record(
                "APPOINTMENT_CREATED",
                creator.getEmail(),
                saved.getId()
        );

        notifications.appointmentCreated(
                saved
        );

        return toResponse(saved);
    }

    @Transactional
    public AppointmentDtos.AppointmentResponse reschedule(
            Long id,
            AppointmentDtos.RescheduleAppointmentRequest request,
            Authentication authentication
    ) {
        Appointment appointment =
                getEntity(id);

        appointment.verifyVersion(
                request.version()
        );

        Employee employee =
                employees.getActiveEntityForUpdate(
                        request.employeeId()
                );

        SalonService salonService =
                services.getActiveEntity(
                        request.serviceId()
                );

        LocalDateTime startTime =
                request.startTime();

        LocalDateTime endTime =
                calculateEndTime(
                        startTime,
                        salonService
                );

        validateSchedule(
                startTime,
                endTime
        );

        ensureNoConflict(
                employee.getId(),
                startTime,
                endTime,
                appointment.getId()
        );

        appointment.reschedule(
                employee,
                salonService,
                startTime,
                endTime
        );

        appointments.flush();

        audit.record(
                "APPOINTMENT_RESCHEDULED",
                getActor(authentication),
                appointment.getId()
        );

        notifications.appointmentChanged(
                appointment
        );

        return toResponse(appointment);
    }

    @Transactional
    public AppointmentDtos.AppointmentResponse confirm(
            Long id,
            Authentication authentication
    ) {
        Appointment appointment =
                getEntity(id);

        appointment.confirm();

        appointments.flush();

        audit.record(
                "APPOINTMENT_CONFIRMED",
                getActor(authentication),
                appointment.getId()
        );

        notifications.appointmentChanged(
                appointment
        );

        return toResponse(appointment);
    }

    @Transactional
    public AppointmentDtos.AppointmentResponse cancel(
            Long id,
            AppointmentDtos.CancelAppointmentRequest request,
            Authentication authentication
    ) {
        Appointment appointment =
                getEntity(id);

        appointment.verifyVersion(
                request.version()
        );

        appointment.cancel(
                request.reason(),
                clock.instant()
        );

        appointments.flush();

        audit.record(
                "APPOINTMENT_CANCELLED",
                getActor(authentication),
                appointment.getId()
        );

        notifications.appointmentCancelled(
                appointment
        );

        return toResponse(appointment);
    }

    @Transactional
    public AppointmentDtos.AppointmentResponse complete(
            Long id,
            Authentication authentication
    ) {
        Appointment appointment =
                getEntity(id);

        appointment.complete();

        appointments.flush();

        audit.record(
                "APPOINTMENT_COMPLETED",
                getActor(authentication),
                appointment.getId()
        );

        return toResponse(appointment);
    }

    @Transactional
    public AppointmentDtos.AppointmentResponse changeNote(
            Long id,
            AppointmentDtos.NoteRequest request,
            Authentication authentication
    ) {
        Appointment appointment =
                getEntity(id);

        appointment.changeNote(
                request.note()
        );

        appointments.flush();

        audit.record(
                "APPOINTMENT_NOTE_CHANGED",
                getActor(authentication),
                appointment.getId()
        );

        return toResponse(appointment);
    }

    private LocalDateTime calculateEndTime(
            LocalDateTime startTime,
            SalonService salonService
    ) {
        return startTime.plusMinutes(
                salonService.getDurationMinutes()
        );
    }

    private void validateSchedule(
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        LocalDateTime now =
                LocalDateTime.now(clock);

        if (!startTime.isAfter(now)) {
            throw new IllegalArgumentException(
                    "Der Termin muss in der Zukunft liegen."
            );
        }

        if (!endTime.toLocalDate()
                .equals(startTime.toLocalDate())) {
            throw new IllegalArgumentException(
                    "Der Termin muss am selben Tag enden."
            );
        }

        if (startTime.toLocalTime()
                .isBefore(OPENING_TIME)) {
            throw new IllegalArgumentException(
                    "Der Termin darf frühestens um 08:00 Uhr beginnen."
            );
        }

        if (endTime.toLocalTime()
                .isAfter(CLOSING_TIME)) {
            throw new IllegalArgumentException(
                    "Der Termin muss spätestens um 19:00 Uhr enden."
            );
        }
    }

    private void ensureNoConflict(
            Long employeeId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Long excludedAppointmentId
    ) {
        boolean conflict =
                appointments.existsConflict(
                        employeeId,
                        startTime,
                        endTime,
                        excludedAppointmentId
                );

        if (conflict) {
            throw new BusinessConflictException(
                    "APPOINTMENT_CONFLICT",
                    "Der Mitarbeiter hat in diesem Zeitraum "
                            + "bereits einen anderen Termin."
            );
        }
    }

    private Appointment getEntity(
            Long id
    ) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "Die Termin-ID darf nicht null sein."
            );
        }

        return appointments
                .findById(id)
                .orElseThrow(
                        () -> new NotFoundException(
                                "Termin mit ID "
                                        + id
                                        + " wurde nicht gefunden."
                        )
                );
    }

    private AppUser getAuthenticatedUser(
            Authentication authentication
    ) {
        String actor =
                getActor(authentication);

        return users
                .findByEmailIgnoreCase(actor)
                .orElseThrow(
                        () -> new NotFoundException(
                                "Der angemeldete Benutzer wurde "
                                        + "nicht gefunden."
                        )
                );
    }

    private String getActor(
            Authentication authentication
    ) {
        if (authentication == null
                || authentication.getName() == null
                || authentication.getName().isBlank()) {
            throw new IllegalStateException(
                    "Es ist kein angemeldeter Benutzer vorhanden."
            );
        }

        return authentication.getName();
    }

    private AppointmentDtos.AppointmentResponse toResponse(
            Appointment appointment
    ) {
        Customer customer =
                appointment.getCustomer();

        Employee employee =
                appointment.getEmployee();

        SalonService salonService =
                appointment.getService();

        String customerName =
                customer.getFirstName()
                        + " "
                        + customer.getLastName();

        String employeeName =
                employee.getFirstName()
                        + " "
                        + employee.getLastName();

        return new AppointmentDtos.AppointmentResponse(
                appointment.getId(),
                customer.getId(),
                customerName,
                employee.getId(),
                employeeName,
                salonService.getId(),
                salonService.getName(),
                salonService.getPrice(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus(),
                appointment.getNote(),
                appointment.getCancelReason(),
                appointment.getCancelledAt(),
                appointment.getVersion(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}