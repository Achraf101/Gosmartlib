import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';
import { filter, map, take } from 'rxjs/operators';

export const authGuard = (allowedRoles: string[]): CanActivateFn => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    return authService.currentUser$.pipe(
      take(1),
      map((user) => {
        if (!user) {
          return router.createUrlTree(['/login']);
        }
        if (allowedRoles.length > 0 && !user.roles.some((role) => allowedRoles.includes(role))) {
          return router.createUrlTree(['/']);
        }
        return true;
      }),
    );
  };
};
