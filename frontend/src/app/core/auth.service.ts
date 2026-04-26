import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse, UserRole } from './models';
import { TokenStoreService } from './token-store.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokens = inject(TokenStoreService);

  readonly authenticated = this.tokens.authenticated;
  readonly email = this.tokens.subject;
  readonly role = this.tokens.role;

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>('/api/auth/login', request)
      .pipe(tap((response) => this.tokens.set(response.accessToken)));
  }

  logout(): void {
    this.tokens.clear();
  }

  hasRole(role: UserRole): boolean {
    return this.tokens.hasRole(role);
  }
}
