import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged, finalize } from 'rxjs';
import { apiErrorMessage, fieldErrors } from '../../core/api-error';
import { CustomerApi } from '../../core/api.services';
import { Customer } from '../../core/models';
import { NotificationService } from '../../core/notification.service';

@Component({
  selector: 'app-customer',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './customer.component.html',
  styleUrl: './customer.component.scss',
})
export class CustomerComponent implements OnInit {
  private readonly api = inject(CustomerApi);
  private readonly notifications = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);

  readonly customers = signal<Customer[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly editingId = signal<number | null>(null);
  readonly serverErrors = signal<Record<string, string>>({});
  readonly search = new FormControl('', { nonNullable: true });
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
    phone: new FormControl('', {
      nonNullable: true,
      validators: Validators.maxLength(40),
    }),
  });

  ngOnInit(): void {
    this.load();
    this.search.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.load());
  }

  load(): void {
    this.loading.set(true);
    this.api
      .list(this.search.value)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (page) => this.customers.set(page.content),
        error: (error) =>
          this.notifications.error(apiErrorMessage(error, 'Kunden konnten nicht geladen werden.')),
      });
  }

  edit(customer: Customer): void {
    this.editingId.set(customer.id);
    this.serverErrors.set({});
    this.form.setValue({
      firstName: customer.firstName,
      lastName: customer.lastName,
      email: customer.email,
      phone: customer.phone ?? '',
    });
  }

  save(): void {
    this.serverErrors.set({});
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const request = { ...raw, phone: raw.phone.trim() || null };
    const id = this.editingId();
    const operation = id ? this.api.update(id, request) : this.api.create(request);

    this.saving.set(true);
    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.notifications.success('Kundendaten wurden gespeichert.');
        this.reset();
        this.load();
      },
      error: (error) => {
        this.serverErrors.set(fieldErrors(error));
        this.notifications.error(
          apiErrorMessage(error, 'Kundendaten konnten nicht gespeichert werden.'),
        );
      },
    });
  }

  toggleActive(customer: Customer): void {
    this.api.changeActive(customer.id, !customer.active).subscribe({
      next: (updated) => {
        this.customers.update((items) =>
          items.map((item) => (item.id === updated.id ? updated : item)),
        );
        this.notifications.success(
          updated.active ? 'Kunde wurde aktiviert.' : 'Kunde wurde deaktiviert.',
        );
      },
      error: (error) =>
        this.notifications.error(apiErrorMessage(error, 'Status konnte nicht geändert werden.')),
    });
  }

  reset(): void {
    this.editingId.set(null);
    this.serverErrors.set({});
    this.form.reset({ firstName: '', lastName: '', email: '', phone: '' });
  }
}
