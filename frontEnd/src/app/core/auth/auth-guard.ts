import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { UserRole } from '../models';
import { Auth } from './auth';

export function authGuard(...roles: UserRole[]): CanActivateFn {
  return (_route, state) => {
    const auth = inject(Auth);
    if (!auth.authenticated()) {
      auth.login(state.url);
      return false;
    }
    return (
      roles.length === 0 || auth.hasAnyRole(...roles) || inject(Router).createUrlTree(['/books'])
    );
  };
}
