package de.salonplan.appointment;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long>,
        JpaSpecificationExecutor<Appointment> {

    @Query("""
            select (count(a) > 0)
            from Appointment a
            where a.employee.id = :employeeId
              and a.status <> de.salonplan.appointment.AppointmentStatus.CANCELLED
              and (:excludedId is null or a.id <> :excludedId)
              and a.startTime < :newEnd
              and a.endTime > :newStart
            """)
    boolean existsConflict(
            @Param("employeeId")
            Long employeeId,

            @Param("newStart")
            LocalDateTime newStart,

            @Param("newEnd")
            LocalDateTime newEnd,

            @Param("excludedId")
            Long excludedId
    );

    @Query("""
            select a
            from Appointment a
            where a.employee.id = :employeeId
              and a.status in :statuses
              and a.startTime < :to
              and a.endTime > :from
            order by a.startTime asc
            """)
    List<Appointment> findBusyIntervals(
            @Param("employeeId")
            Long employeeId,

            @Param("from")
            LocalDateTime from,

            @Param("to")
            LocalDateTime to,

            @Param("statuses")
            Collection<AppointmentStatus> statuses
    );

    default Specification<Appointment> createSpecification(
            AppointmentSearchCriteria criteria
    ) {
        return AppointmentSpecifications.matching(
                criteria
        );
    }
}