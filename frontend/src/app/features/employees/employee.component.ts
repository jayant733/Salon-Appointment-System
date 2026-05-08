import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { apiErrorMessage, fieldErrors } from '../../core/api-error';
import { EmployeeApi } from '../../core/api.services';
import { Employee } from '../../core/models';
import { NotificationService } from '../../core/notification.service';

@Component({
  selector: 'app-employee',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './employee.component.html',
  styleUrl: './employee.component.scss',
})
export class EmployeeComponent implements OnInit {
  private readonly api = inject(EmployeeApi);
  private readonly notifications = inject(NotificationService);

  readonly employees = signal<Employee[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly editingId = signal<number | null>(null);
  readonly serverErrors = signal<Record<string, string>>({});
  readonly form = new FormGroup({
    firstName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(80)],
    }),
    lastName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(80)],
    }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email, Validators.maxLength(150)],
    }),
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.api
      .list(false)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (items) => this.employees.set(items),
        error: (error) =>
          this.notifications.error(
            apiErrorMessage(error, 'Mitarbeiter konnten nicht geladen werden.'),
          ),
      });
  }

  edit(employee: Employee): void {
    this.editingId.set(employee.id);
    this.serverErrors.set({});
    this.form.setValue({
      firstName: employee.firstName,
      lastName: employee.lastName,
      email: employee.email,
    });
  }

  save(): void {
    this.serverErrors.set({});
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const id = this.editingId();
    const operation = id
      ? this.api.update(id, this.form.getRawValue())
      : this.api.create(this.form.getRawValue());
    this.saving.set(true);
    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.notifications.success('Mitarbeiter wurde gespeichert.');
        this.reset();
        this.load();
      },
      error: (error) => {
        this.serverErrors.set(fieldErrors(error));
        this.notifications.error(
          apiErrorMessage(error, 'Mitarbeiter konnte nicht gespeichert werden.'),
        );
      },
    });
  }

  toggleActive(employee: Employee): void {
    const verb = employee.active ? 'deaktivieren' : 'aktivieren';
    if (!window.confirm(`Mitarbeiter wirklich ${verb}?`)) {
      return;
    }

    this.api.changeActive(employee.id, !employee.active).subscribe({
      next: (updated) => {
        this.employees.update((items) =>
          items.map((item) => (item.id === updated.id ? updated : item)),
        );
        this.notifications.success(
          `Mitarbeiter wurde ${updated.active ? 'aktiviert' : 'deaktiviert'}.`,
        );
      },
      error: (error) =>
        this.notifications.error(apiErrorMessage(error, 'Status konnte nicht geändert werden.')),
    });
  }

  reset(): void {
    this.editingId.set(null);
    this.serverErrors.set({});
    this.form.reset({ firstName: '', lastName: '', email: '' });
  }
}
