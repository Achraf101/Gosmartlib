import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';
import { filter, map, take } from 'rxjs/operators';

// export const authGuard: CanActivateFn = () => {
//   const authService = inject(AuthService);
//   const router = inject(Router);

//   return authService.currentUser$.pipe(
//     take(1),
//     map(user => {
//       if (user) {
//         return true;
//       }
//       return router.createUrlTree(['/login']);
//     })
//   );
// };

export const authGuard = (allowedRoles: string[]): CanActivateFn => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    return authService.currentUser$.pipe(
      filter((user) => user !== null),
      take(1),
      map((user) => {
        if (allowedRoles.length > 0 && !user!.roles.some((role) => allowedRoles.includes(role))) {
          return router.createUrlTree(['/']);
        }
        return true;
      }),
    );
  };
};
