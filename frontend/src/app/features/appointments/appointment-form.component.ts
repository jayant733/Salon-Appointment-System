import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, forkJoin, of, switchMap } from 'rxjs';
import { apiErrorMessage } from '../../core/api-error';
import { AppointmentApi, CustomerApi, EmployeeApi, SalonServiceApi } from '../../core/api.services';
import { Appointment, Customer, Employee, SalonService } from '../../core/models';
import { NotificationService } from '../../core/notification.service';

@Component({
  selector: 'app-appointment-form',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, ReactiveFormsModule],
  templateUrl: './appointment-form.component.html',
  styleUrl: './appointment-form.component.scss',
})
export class AppointmentFormComponent implements OnInit {
  private readonly appointments = inject(AppointmentApi);
  private readonly customerApi = inject(CustomerApi);
  private readonly employeeApi = inject(EmployeeApi);
  private readonly serviceApi = inject(SalonServiceApi);
  private readonly notifications = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly customers = signal<Customer[]>([]);
  readonly employees = signal<Employee[]>([]);
  readonly services = signal<SalonService[]>([]);
  readonly slots = signal<string[]>([]);
  readonly currentAppointment = signal<Appointment | null>(null);
  readonly loading = signal(true);
  readonly slotsLoading = signal(false);
  readonly saving = signal(false);
  readonly appointmentId = signal<number | null>(null);
  readonly editing = computed(() => this.appointmentId() !== null);
  readonly title = computed(() => (this.editing() ? 'Termin verschieben' : 'Neuen Termin buchen'));

  readonly form = new FormGroup({
    customerId: new FormControl<number | null>(null, Validators.required),
    employeeId: new FormControl<number | null>(null, Validators.required),
    serviceId: new FormControl<number | null>(null, Validators.required),
    date: new FormControl(localIsoDate(new Date()), {
      nonNullable: true,
      validators: Validators.required,
    }),
    startTime: new FormControl('', {
      nonNullable: true,
      validators: Validators.required,
    }),
    note: new FormControl('', {
      nonNullable: true,
      validators: Validators.maxLength(1000),
    }),
  });

  ngOnInit(): void {
    const idText = this.route.snapshot.paramMap.get('id');
    const id = idText ? Number(idText) : null;
    this.appointmentId.set(id !== null && Number.isFinite(id) ? id : null);

    const appointmentRequest = this.appointmentId()
      ? this.appointments.get(this.appointmentId()!)
      : of(null);

    forkJoin({
      customers: this.customerApi.list('', 0, 250),
      employees: this.employeeApi.list(true),
      services: this.serviceApi.list(true),
      appointment: appointmentRequest,
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (result) => {
          const customerItems = result.customers.content.filter((customer) => customer.active);
          this.customers.set(customerItems);
          this.employees.set(result.employees);
          this.services.set(result.services);

          if (result.appointment) {
            this.prepareEdit(result.appointment, customerItems);
          } else {
            const preselectedCustomer = Number(this.route.snapshot.queryParamMap.get('customerId'));
            if (Number.isFinite(preselectedCustomer)) {
              this.form.controls.customerId.setValue(preselectedCustomer);
            }
          }
        },
        error: (error) =>
          this.notifications.error(
            apiErrorMessage(error, 'Formulardaten konnten nicht geladen werden.'),
          ),
      });
  }

  loadSlots(): void {
    const { employeeId, serviceId, date } = this.form.getRawValue();
    this.form.controls.startTime.setValue('');

    if (!employeeId || !serviceId || !date) {
      this.slots.set([]);
      return;
    }

    this.slotsLoading.set(true);
    this.appointments
      .availableStarts(employeeId, serviceId, date)
      .pipe(finalize(() => this.slotsLoading.set(false)))
      .subscribe({
        next: (slots) => {
          const current = this.currentAppointment();
          const currentStart = current?.startTime;
          const values =
            currentStart?.startsWith(date) && !slots.includes(currentStart)
              ? [currentStart, ...slots]
              : slots;
          this.slots.set(values);
          if (currentStart?.startsWith(date)) {
            this.form.controls.startTime.setValue(currentStart);
          }
        },
        error: (error) =>
          this.notifications.error(
            apiErrorMessage(error, 'Freie Zeiten konnten nicht geladen werden.'),
          ),
      });
  }

  selectedService(): SalonService | undefined {
    const serviceId = this.form.controls.serviceId.value;
    return this.services().find((service) => service.id === serviceId);
  }

  estimatedEnd(): Date | null {
    const start = this.form.controls.startTime.value;
    const duration = this.selectedService()?.durationMinutes;
    if (!start || !duration) {
      return null;
    }
    return new Date(new Date(start).getTime() + duration * 60_000);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    this.saving.set(true);

    const operation = this.appointmentId()
      ? this.appointments
          .reschedule(this.appointmentId()!, {
            employeeId: value.employeeId!,
            serviceId: value.serviceId!,
            startTime: value.startTime,
          })
          .pipe(
            switchMap((updated) => {
              const desiredNote = value.note.trim() || null;
              return desiredNote !== (this.currentAppointment()?.note ?? null)
                ? this.appointments.changeNote(updated.id, desiredNote)
                : of(updated);
            }),
          )
      : this.appointments.create({
          customerId: value.customerId!,
          employeeId: value.employeeId!,
          serviceId: value.serviceId!,
          startTime: value.startTime,
          note: value.note.trim() || undefined,
        });

    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.notifications.success(
          this.editing() ? 'Termin wurde verschoben.' : 'Termin wurde gebucht.',
        );
        void this.router.navigateByUrl('/appointments');
      },
      error: (error) => {
        this.notifications.error(
          apiErrorMessage(
            error,
            error?.status === 409
              ? 'Dieser Zeitraum ist inzwischen belegt. Bitte wähle eine andere Startzeit.'
              : 'Termin konnte nicht gespeichert werden.',
          ),
        );
        this.loadSlots();
      },
    });
  }

  cancel(): void {
    void this.router.navigateByUrl('/appointments');
  }

  private prepareEdit(appointment: Appointment, activeCustomers: Customer[]): void {
    this.currentAppointment.set(appointment);
    if (!activeCustomers.some((customer) => customer.id === appointment.customerId)) {
      this.customers.update((items) => [
        {
          id: appointment.customerId,
          firstName: appointment.customerName,
          lastName: '',
          email: '',
          phone: null,
          active: false,
          createdAt: '',
        },
        ...items,
      ]);
    }

    this.form.patchValue({
      customerId: appointment.customerId,
      employeeId: appointment.employeeId,
      serviceId: appointment.serviceId,
      date: appointment.startTime.slice(0, 10),
      startTime: appointment.startTime,
      note: appointment.note ?? '',
    });
    this.form.controls.customerId.disable();
    this.loadSlots();
  }
}

function localIsoDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}
