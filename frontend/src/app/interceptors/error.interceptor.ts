import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { MessageService } from 'primeng/api';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const messageService = inject(MessageService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const message = typeof error.error === 'string' ? error.error : 'Er is een onverwachte fout opgetreden.';

      switch (error.status) {
        case 400:
          messageService.add({ severity: 'warn', summary: 'Ongeldige invoer', detail: message, life: 4000 });
          break;
        case 403:
          messageService.add({ severity: 'error', summary: 'Geen toegang', detail: message, life: 4000 });
          break;
        case 404:
          messageService.add({ severity: 'error', summary: 'Niet gevonden', detail: message, life: 4000 });
          break;
        case 409:
          messageService.add({ severity: 'warn', summary: 'Conflict', detail: message, life: 4000 });
          break;
        case 500:
          messageService.add({ severity: 'error', summary: 'Serverfout', detail: 'Er is iets misgegaan op de server.', life: 4000 });
          break;
      }

      return throwError(() => error);
    })
  );
};