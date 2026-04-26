import { computed, Injectable, signal } from '@angular/core';
import { UserRole } from './models';

interface TokenClaims {
  sub?: string;
  roles?: UserRole[];
  exp?: number;
  userId?: number;
}

@Injectable({ providedIn: 'root' })
export class TokenStoreService {
  private readonly storageKey = 'salonplan.accessToken';
  readonly token = signal<string | null>(sessionStorage.getItem(this.storageKey));
  readonly claims = computed(() => decodeClaims(this.token()));
  readonly authenticated = computed(() => {
    const expiresAt = this.claims()?.exp;
    return typeof expiresAt === 'number' && expiresAt * 1000 > Date.now();
  });
  readonly subject = computed(() => this.claims()?.sub ?? null);
  readonly role = computed<UserRole | null>(() => this.claims()?.roles?.[0] ?? null);
  readonly userId = computed(() => this.claims()?.userId ?? null);

  set(token: string): void {
    sessionStorage.setItem(this.storageKey, token);
    this.token.set(token);
  }

  clear(): void {
    sessionStorage.removeItem(this.storageKey);
    this.token.set(null);
  }

  hasRole(role: UserRole): boolean {
    return this.claims()?.roles?.includes(role) ?? false;
  }
}

function decodeClaims(token: string | null): TokenClaims | null {
  if (!token) {
    return null;
  }

  try {
    const encoded = token.split('.')[1];
    if (!encoded) {
      return null;
    }

    const normalized = encoded.replace(/-/g, '+').replace(/_/g, '/');
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=');
    const binary = atob(padded);
    const bytes = Uint8Array.from(binary, (char) => char.charCodeAt(0));
    return JSON.parse(new TextDecoder().decode(bytes)) as TokenClaims;
  } catch {
    return null;
  }
}
