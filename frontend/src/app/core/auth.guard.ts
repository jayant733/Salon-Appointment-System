import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { TokenStoreService } from './token-store.service';

export const authGuard: CanActivateFn = (_route, state) => {
  const tokens = inject(TokenStoreService);
  return tokens.authenticated()
    ? true
    : inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

export const guestGuard: CanActivateFn = () => {
  const tokens = inject(TokenStoreService);
  return tokens.authenticated() ? inject(Router).createUrlTree(['/appointments']) : true;
};
