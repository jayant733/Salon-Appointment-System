import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';
import { NotificationService } from './core/notification.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  readonly auth = inject(AuthService);
  readonly notifications = inject(NotificationService);
  private readonly router = inject(Router);

  readonly isAdmin = computed(() => this.auth.hasRole('ADMIN'));
  readonly roleLabel = computed(() =>
    this.auth.role() === 'ADMIN' ? 'Administrator' : 'Mitarbeiter',
  );

  logout(): void {
    this.auth.logout();
    void this.router.navigateByUrl('/login');
  }
}
