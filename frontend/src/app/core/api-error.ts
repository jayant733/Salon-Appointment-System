import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from './models';

export function apiErrorMessage(error: unknown, fallback: string): string {
  if (!(error instanceof HttpErrorResponse)) {
    return fallback;
  }

  if (error.status === 0) {
    return 'Das Backend ist nicht erreichbar. Bitte prüfe, ob Spring Boot auf Port 8080 läuft.';
  }

  const body = asApiError(error.error);
  if (body?.message?.trim()) {
    return body.message;
  }

  return switchStatus(error.status, fallback);
}

export function fieldErrors(error: unknown): Record<string, string> {
  if (!(error instanceof HttpErrorResponse)) {
    return {};
  }

  const source = asApiError(error.error)?.fieldErrors;
  if (!source) {
    return {};
  }

  if (Array.isArray(source)) {
    return Object.fromEntries(source.map((item) => [item.field, item.message]));
  }

  return source;
}

function asApiError(value: unknown): ApiError | null {
  return value && typeof value === 'object' ? (value as ApiError) : null;
}

function switchStatus(status: number, fallback: string): string {
  switch (status) {
    case 400:
      return 'Die Eingaben sind nicht gültig.';
    case 401:
      return 'Die Anmeldung ist abgelaufen oder ungültig.';
    case 403:
      return 'Für diese Aktion fehlt die Berechtigung.';
    case 404:
      return 'Der angeforderte Datensatz wurde nicht gefunden.';
    case 409:
      return 'Die Aktion steht im Konflikt mit dem aktuellen Datenbestand.';
    default:
      return fallback;
  }
}
