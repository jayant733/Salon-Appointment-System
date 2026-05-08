import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { apiErrorMessage } from '../../core/api-error';
import { UserApi } from '../../core/api.services';
import { UserAccount, UserRole } from '../../core/models';
import { NotificationService } from '../../core/notification.service';

@Component({
  selector: 'app-user',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './user.component.html',
  styleUrl: './user.component.scss',
})
export class UserComponent implements OnInit {
  private readonly api = inject(UserApi);
  private readonly notifications = inject(NotificationService);

  readonly users = signal<UserAccount[]>([]);
  readonly loading = signal(false);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.api
      .list()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (page) => this.users.set(page.content),
        error: (error) =>
          this.notifications.error(
            apiErrorMessage(error, 'Benutzerkonten konnten nicht geladen werden.'),
          ),
      });
  }

  changeRole(user: UserAccount, role: UserRole): void {
    if (role === user.role) {
      return;
    }
    this.api.changeRole(user.id, role).subscribe({
      next: (updated) => this.replace(updated, 'Rolle wurde geändert.'),
      error: (error) =>
        this.notifications.error(apiErrorMessage(error, 'Rolle konnte nicht geändert werden.')),
    });
  }

  toggleEnabled(user: UserAccount): void {
    const action = user.enabled ? 'deaktivieren' : 'aktivieren';
    if (!window.confirm(`Benutzerkonto ${user.email} wirklich ${action}?`)) {
      return;
    }
    this.api.changeEnabled(user.id, !user.enabled).subscribe({
      next: (updated) =>
        this.replace(
          updated,
          `Benutzerkonto wurde ${updated.enabled ? 'aktiviert' : 'deaktiviert'}.`,
        ),
      error: (error) =>
        this.notifications.error(
          apiErrorMessage(error, 'Benutzerstatus konnte nicht geändert werden.'),
        ),
    });
  }

  roleLabel(role: UserRole): string {
    return role === 'ADMIN' ? 'Administrator' : 'Mitarbeiter';
  }

  private replace(updated: UserAccount, message: string): void {
    this.users.update((items) => items.map((item) => (item.id === updated.id ? updated : item)));
    this.notifications.success(message);
  }
}
