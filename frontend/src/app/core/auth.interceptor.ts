import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from './notification.service';
import { TokenStoreService } from './token-store.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const tokens = inject(TokenStoreService);
  const router = inject(Router);
  const notifications = inject(NotificationService);
  const token = tokens.token();
  const isApiRequest = request.url.startsWith('/api');

  const authenticatedRequest =
    token && isApiRequest
      ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : request;

  return next(authenticatedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && request.url !== '/api/auth/login') {
        tokens.clear();
        notifications.warning('Deine Sitzung ist abgelaufen. Bitte melde dich erneut an.');
        void router.navigate(['/login'], { queryParams: { returnUrl: router.url } });
      } else if (error.status === 403) {
        notifications.error('Für diese Aktion fehlt dir die Berechtigung.');
      }
      return throwError(() => error);
    }),
  );
};
