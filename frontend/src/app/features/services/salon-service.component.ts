import { CurrencyPipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { apiErrorMessage, fieldErrors } from '../../core/api-error';
import { SalonServiceApi } from '../../core/api.services';
import { SalonService } from '../../core/models';
import { NotificationService } from '../../core/notification.service';

@Component({
  selector: 'app-salon-service',
  standalone: true,
  imports: [CurrencyPipe, ReactiveFormsModule],
  templateUrl: './salon-service.component.html',
  styleUrl: './salon-service.component.scss',
})
export class SalonServiceComponent implements OnInit {
  private readonly api = inject(SalonServiceApi);
  private readonly notifications = inject(NotificationService);

  readonly services = signal<SalonService[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly editingId = signal<number | null>(null);
  readonly serverErrors = signal<Record<string, string>>({});
  readonly form = new FormGroup({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(120)],
    }),
    description: new FormControl('', { nonNullable: true, validators: Validators.maxLength(500) }),
    durationMinutes: new FormControl(30, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(5), Validators.max(480)],
    }),
    price: new FormControl(0, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(0)],
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
        next: (items) => this.services.set(items),
        error: (error) =>
          this.notifications.error(
            apiErrorMessage(error, 'Leistungen konnten nicht geladen werden.'),
          ),
      });
  }

  edit(service: SalonService): void {
    this.editingId.set(service.id);
    this.serverErrors.set({});
    this.form.setValue({
      name: service.name,
      description: service.description ?? '',
      durationMinutes: service.durationMinutes,
      price: service.price,
    });
  }

  save(): void {
    this.serverErrors.set({});
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const request = { ...raw, description: raw.description.trim() || null };
    const id = this.editingId();
    const operation = id ? this.api.update(id, request) : this.api.create(request);
    this.saving.set(true);
    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.notifications.success('Leistung wurde gespeichert.');
        this.reset();
        this.load();
      },
      error: (error) => {
        this.serverErrors.set(fieldErrors(error));
        this.notifications.error(
          apiErrorMessage(error, 'Leistung konnte nicht gespeichert werden.'),
        );
      },
    });
  }

  toggleActive(service: SalonService): void {
    this.api.changeActive(service.id, !service.active).subscribe({
      next: (updated) => {
        this.services.update((items) =>
          items.map((item) => (item.id === updated.id ? updated : item)),
        );
        this.notifications.success(
          `Leistung wurde ${updated.active ? 'aktiviert' : 'deaktiviert'}.`,
        );
      },
      error: (error) =>
        this.notifications.error(apiErrorMessage(error, 'Status konnte nicht geändert werden.')),
    });
  }

  reset(): void {
    this.editingId.set(null);
    this.serverErrors.set({});
    this.form.reset({ name: '', description: '', durationMinutes: 30, price: 0 });
  }
}
