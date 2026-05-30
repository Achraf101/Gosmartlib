import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth';

export const adminRedirectGuard = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.hasRole('ADMIN')) {
    return router.createUrlTree(['/dashboard/admin']);
  }
  return true;
};