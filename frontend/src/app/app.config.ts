import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { nl } from 'primelocale/js/nl.js';

import { routes } from './app.routes';
import { MessageService } from 'primeng/api';
import { AuthService } from './services/auth';
import { authInterceptor } from './interceptors/auth.interceptor';
import { errorInterceptor } from './interceptors/error.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
    providePrimeNG({ translation: nl, theme: { preset: Aura, options: { darkModeSelector: '' } } }),
    MessageService,
    provideAppInitializer(() => inject(AuthService).loadCurrentUser()),
  ],
};
