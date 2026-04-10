package de.salonplan.appointment;

import de.salonplan.employee.Employee;
import de.salonplan.employee.EmployeeService;
import de.salonplan.salonservice.SalonService;
import de.salonplan.salonservice.SalonServiceService;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Service
public class AvailabilityService {

    private static final LocalTime OPENING =
            LocalTime.of(8, 0);

    private static final LocalTime CLOSING =
            LocalTime.of(19, 0);

    private static final int STEP_MINUTES = 15;

    private final AppointmentRepository appointments;
    private final EmployeeService employees;
    private final SalonServiceService services;
    private final Clock clock;

    public AvailabilityService(
            AppointmentRepository appointments,
            EmployeeService employees,
            SalonServiceService services,
            Clock clock
    ) {
        this.appointments = appointments;
        this.employees = employees;
        this.services = services;
        this.clock = clock;
    }

    public List<LocalDateTime> availableStarts(
            Long employeeId,
            Long serviceId,
            LocalDate day
    ) {
        Employee employee =
                employees.getEntity(employeeId);

        SalonService service =
                services.getActiveEntity(serviceId);

        if (!employee.isActive()) {
            return List.of();
        }

        LocalDateTime openingTime =
                day.atTime(OPENING);

        LocalDateTime closingTime =
                day.atTime(CLOSING);

        LocalDateTime now =
                LocalDateTime.now(clock);

        List<Appointment> busyAppointments =
                appointments.findBusyIntervals(
                        employeeId,
                        openingTime,
                        closingTime,
                        EnumSet.of(
                                AppointmentStatus.BOOKED,
                                AppointmentStatus.CONFIRMED,
                                AppointmentStatus.COMPLETED
                        )
                );

        List<LocalDateTime> availableStarts =
                new ArrayList<>();

        for (
                LocalDateTime start = openingTime;
                !start
                        .plusMinutes(service.getDurationMinutes())
                        .isAfter(closingTime);
                start = start.plusMinutes(STEP_MINUTES)
        ) {
            LocalDateTime candidateStart = start;

            LocalDateTime candidateEnd =
                    candidateStart.plusMinutes(
                            service.getDurationMinutes()
                    );

            boolean isInFuture =
                    candidateStart.isAfter(now);

            boolean overlaps =
                    busyAppointments.stream()
                            .anyMatch(appointment ->
                                    candidateStart.isBefore(
                                            appointment.getEndTime()
                                    )
                                            && candidateEnd.isAfter(
                                            appointment.getStartTime()
                                    )
                            );

            if (isInFuture && !overlaps) {
                availableStarts.add(candidateStart);
            }
        }

        return availableStarts;
    }
}