import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { apiErrorMessage } from '../../core/api-error';
import { AppointmentApi, EmployeeApi } from '../../core/api.services';
import { Appointment, AppointmentStatus, Employee } from '../../core/models';
import { NotificationService } from '../../core/notification.service';

@Component({
  selector: 'app-appointment-list',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, ReactiveFormsModule, RouterLink],
  templateUrl: './appointment-list.component.html',
  styleUrl: './appointment-list.component.scss',
})
export class AppointmentListComponent implements OnInit {
  private readonly api = inject(AppointmentApi);
  private readonly employeeApi = inject(EmployeeApi);
  private readonly notifications = inject(NotificationService);
  private readonly router = inject(Router);

  readonly appointments = signal<Appointment[]>([]);
  readonly employees = signal<Employee[]>([]);
  readonly loading = signal(false);
  readonly totalElements = signal(0);
  readonly filter = new FormGroup({
    from: new FormControl(localIsoDate(new Date()), { nonNullable: true }),
    to: new FormControl(localIsoDate(addDays(new Date(), 14)), { nonNullable: true }),
    employeeId: new FormControl<number | null>(null),
    status: new FormControl<AppointmentStatus | ''>('', { nonNullable: true }),
  });

  ngOnInit(): void {
    this.loading.set(true);
    forkJoin({
      employees: this.employeeApi.list(false),
      page: this.api.search({ ...this.searchValues() }),
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (result) => {
          this.employees.set(result.employees);
          this.appointments.set(result.page.content);
          this.totalElements.set(result.page.totalElements);
        },
        error: (error) =>
          this.notifications.error(apiErrorMessage(error, 'Termine konnten nicht geladen werden.')),
      });
  }

  load(): void {
    const { from, to } = this.filter.getRawValue();
    if (!from || !to || from > to) {
      this.notifications.warning('Der Zeitraum ist ungültig.');
      return;
    }

    this.loading.set(true);
    this.api
      .search(this.searchValues())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (page) => {
          this.appointments.set(page.content);
          this.totalElements.set(page.totalElements);
        },
        error: (error) =>
          this.notifications.error(apiErrorMessage(error, 'Termine konnten nicht geladen werden.')),
      });
  }

  resetFilters(): void {
    this.filter.reset({
      from: localIsoDate(new Date()),
      to: localIsoDate(addDays(new Date(), 14)),
      employeeId: null,
      status: '',
    });
    this.load();
  }

  confirm(appointment: Appointment): void {
    this.updateStatus(appointment, 'confirm');
  }

  complete(appointment: Appointment): void {
    this.updateStatus(appointment, 'complete');
  }

  cancel(appointment: Appointment): void {
    const reason = window.prompt('Optionaler Stornierungsgrund:', appointment.cancelReason ?? '');
    if (reason === null) {
      return;
    }

    this.api.cancel(appointment.id, reason).subscribe({
      next: (updated) => this.replace(updated, 'Termin wurde storniert.'),
      error: (error) =>
        this.notifications.error(apiErrorMessage(error, 'Termin konnte nicht storniert werden.')),
    });
  }

  changeNote(appointment: Appointment): void {
    const note = window.prompt('Interne Notiz:', appointment.note ?? '');
    if (note === null) {
      return;
    }

    this.api.changeNote(appointment.id, note.trim() || null).subscribe({
      next: (updated) => this.replace(updated, 'Notiz wurde gespeichert.'),
      error: (error) =>
        this.notifications.error(apiErrorMessage(error, 'Notiz konnte nicht gespeichert werden.')),
    });
  }

  edit(appointment: Appointment): void {
    void this.router.navigate(['/appointments', appointment.id, 'edit']);
  }

  statusLabel(status: AppointmentStatus): string {
    const labels: Record<AppointmentStatus, string> = {
      BOOKED: 'Gebucht',
      CONFIRMED: 'Bestätigt',
      COMPLETED: 'Abgeschlossen',
      CANCELLED: 'Storniert',
    };
    return labels[status];
  }

  private updateStatus(appointment: Appointment, action: 'confirm' | 'complete'): void {
    const operation =
      action === 'confirm' ? this.api.confirm(appointment.id) : this.api.complete(appointment.id);

    operation.subscribe({
      next: (updated) => this.replace(updated, 'Terminstatus wurde aktualisiert.'),
      error: (error) =>
        this.notifications.error(apiErrorMessage(error, 'Status konnte nicht geändert werden.')),
    });
  }

  private replace(updated: Appointment, message: string): void {
    this.appointments.update((items) =>
      items.map((item) => (item.id === updated.id ? updated : item)),
    );
    this.notifications.success(message);
  }

  private searchValues(): {
    from: string;
    to: string;
    employeeId?: number;
    status?: AppointmentStatus;
  } {
    const value = this.filter.getRawValue();
    return {
      from: value.from,
      to: value.to,
      employeeId: value.employeeId ?? undefined,
      status: value.status || undefined,
    };
  }
}

function addDays(date: Date, days: number): Date {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result;
}

function localIsoDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}
