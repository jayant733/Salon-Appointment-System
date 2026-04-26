import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { NotificationService } from './notification.service';
import { TokenStoreService } from './token-store.service';

export const adminGuard: CanActivateFn = () => {
  const tokens = inject(TokenStoreService);
  if (tokens.hasRole('ADMIN')) {
    return true;
  }

  inject(NotificationService).warning('Dieser Bereich ist Administratoren vorbehalten.');
  return inject(Router).createUrlTree(['/appointments']);
};
