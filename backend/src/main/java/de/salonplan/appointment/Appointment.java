package de.salonplan.appointment;

import de.salonplan.common.BusinessConflictException;
import de.salonplan.customer.Customer;
import de.salonplan.employee.Employee;
import de.salonplan.salonservice.SalonService;
import de.salonplan.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "customer_id",
            nullable = false
    )
    private Customer customer;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "employee_id",
            nullable = false
    )
    private Employee employee;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "service_id",
            nullable = false
    )
    private SalonService service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private AppUser createdBy;

    @Column(
            name = "start_time",
            nullable = false
    )
    private LocalDateTime startTime;

    @Column(
            name = "end_time",
            nullable = false
    )
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private AppointmentStatus status;

    @Column(length = 1000)
    private String note;

    @Column(
            name = "cancel_reason",
            length = 500
    )
    private String cancelReason;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    /**
     * Hibernate erhöht diesen Wert bei jeder Änderung.
     *
     * Versuchen zwei Transaktionen gleichzeitig denselben Datensatz
     * zu ändern, schlägt die zweite Änderung mit einem
     * Optimistic-Locking-Fehler fehl.
     */
    @Version
    @Column(nullable = false)
    private long version;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    /**
     * Von JPA benötigter Konstruktor.
     */
    protected Appointment() {
    }

    private Appointment(
            Customer customer,
            Employee employee,
            SalonService service,
            AppUser createdBy,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String note
    ) {
        this.customer = Objects.requireNonNull(
                customer,
                "Customer must not be null"
        );

        this.employee = Objects.requireNonNull(
                employee,
                "Employee must not be null"
        );

        this.service = Objects.requireNonNull(
                service,
                "Salon service must not be null"
        );

        this.createdBy = createdBy;
        this.status = AppointmentStatus.BOOKED;
        this.note = normalizeNote(note);

        setSchedule(startTime, endTime);
    }

    public static Appointment book(
            Customer customer,
            Employee employee,
            SalonService service,
            AppUser createdBy,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String note
    ) {
        return new Appointment(
                customer,
                employee,
                service,
                createdBy,
                startTime,
                endTime,
                note
        );
    }

    /**
     * Prüft, ob der Client noch mit der aktuellen Version arbeitet.
     */
    public void verifyVersion(Long expectedVersion) {
        if (expectedVersion == null) {
            throw new IllegalArgumentException(
                    "Die erwartete Version darf nicht null sein."
            );
        }

        if (expectedVersion != version) {
            throw new BusinessConflictException(
                    "STALE_APPOINTMENT_VERSION",
                    "Der Termin wurde zwischenzeitlich geändert. "
                            + "Bitte laden Sie ihn neu."
            );
        }
    }

    /**
     * Verschiebt den Termin.
     *
     * Dabei können der Mitarbeiter und die Leistung geändert werden.
     * Der Status bleibt erhalten. Ein bestätigter Termin bleibt somit
     * auch nach dem Verschieben bestätigt.
     */
    public void reschedule(
            Employee employee,
            SalonService service,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        requireEditable();

        this.employee = Objects.requireNonNull(
                employee,
                "Employee must not be null"
        );

        this.service = Objects.requireNonNull(
                service,
                "Salon service must not be null"
        );

        setSchedule(startTime, endTime);
        touch();
    }

    /**
     * BOOKED -> CONFIRMED
     */
    public void confirm() {
        if (status != AppointmentStatus.BOOKED) {
            throw new InvalidAppointmentStatusException(
                    "Nur gebuchte Termine können bestätigt werden. "
                            + "Aktueller Status: " + status
            );
        }

        status = AppointmentStatus.CONFIRMED;
        touch();
    }

    /**
     * BOOKED -> CANCELLED
     * CONFIRMED -> CANCELLED
     */
    public void cancel(
            String reason,
            Instant cancellationTime
    ) {
        if (status != AppointmentStatus.BOOKED
                && status != AppointmentStatus.CONFIRMED) {
            throw new InvalidAppointmentStatusException(
                    "Nur gebuchte oder bestätigte Termine können "
                            + "storniert werden. Aktueller Status: "
                            + status
            );
        }

        status = AppointmentStatus.CANCELLED;
        cancelReason = normalizeCancelReason(reason);
        cancelledAt = Objects.requireNonNull(
                cancellationTime,
                "Cancellation time must not be null"
        );

        touch();
    }

    /**
     * Rückwärtskompatible Methode für bestehende Tests.
     */
    public void cancel() {
        cancel(null, Instant.now());
    }

    /**
     * CONFIRMED -> COMPLETED
     */
    public void complete() {
        if (status != AppointmentStatus.CONFIRMED) {
            throw new InvalidAppointmentStatusException(
                    "Nur bestätigte Termine können abgeschlossen werden. "
                            + "Aktueller Status: " + status
            );
        }

        status = AppointmentStatus.COMPLETED;
        touch();
    }

    public void changeNote(String note) {
        requireEditable();

        this.note = normalizeNote(note);
        touch();
    }

    private void requireEditable() {
        if (status.isFinalStatus()) {
            throw new InvalidAppointmentStatusException(
                    "Ein Termin mit dem Status " + status
                            + " darf nicht mehr bearbeitet werden."
            );
        }
    }

    private void setSchedule(
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        Objects.requireNonNull(
                startTime,
                "Start time must not be null"
        );

        Objects.requireNonNull(
                endTime,
                "End time must not be null"
        );

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(
                    "Die Endzeit muss nach der Startzeit liegen."
            );
        }

        this.startTime = startTime;
        this.endTime = endTime;
    }

    private String normalizeNote(String note) {
        if (note == null || note.isBlank()) {
            return null;
        }

        String normalized = note.trim();

        if (normalized.length() > 1000) {
            throw new IllegalArgumentException(
                    "Die Notiz darf höchstens 1000 Zeichen enthalten."
            );
        }

        return normalized;
    }

    private String normalizeCancelReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }

        String normalized = reason.trim();

        if (normalized.length() > 500) {
            throw new IllegalArgumentException(
                    "Die Stornierungsbegründung darf höchstens "
                            + "500 Zeichen enthalten."
            );
        }

        return normalized;
    }

    @PrePersist
    private void onCreate() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;

        if (status == null) {
            status = AppointmentStatus.BOOKED;
        }
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = Instant.now();
    }

    private void touch() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Employee getEmployee() {
        return employee;
    }

    public SalonService getService() {
        return service;
    }

    public AppUser getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}