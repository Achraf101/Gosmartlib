import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { BehaviorSubject } from 'rxjs';

import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth';
import { AuthUser } from '../models/auth';

describe('authGuard', () => {
  let currentUserSubject: BehaviorSubject<AuthUser | null>;
  let router: Router;

  beforeEach(() => {
    currentUserSubject = new BehaviorSubject<AuthUser | null>(null);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'login', redirectTo: '' }]),
        {
          provide: AuthService,
          useValue: {
            currentUser$: currentUserSubject.asObservable(),
          },
        },
      ],
    });

    router = TestBed.inject(Router);
  });

  it('should allow access when user is authenticated', (done) => {
    currentUserSubject.next({ username: 'admin', role: 'ROLE_ADMIN' });

    TestBed.runInInjectionContext(() => {
      const result = authGuard({} as any, {} as any);
      if (result instanceof UrlTree) {
        fail('Expected true but got UrlTree');
        done();
      } else {
        (result as any).subscribe((value: boolean | UrlTree) => {
          expect(value).toBeTrue();
          done();
        });
      }
    });
  });

  it('should redirect to /login when user is not authenticated', (done) => {
    currentUserSubject.next(null);

    TestBed.runInInjectionContext(() => {
      const result = authGuard({} as any, {} as any);
      if (result instanceof UrlTree) {
        expect(result.toString()).toBe('/login');
        done();
      } else {
        (result as any).subscribe((value: boolean | UrlTree) => {
          expect(value instanceof UrlTree).toBeTrue();
          expect((value as UrlTree).toString()).toBe('/login');
          done();
        });
      }
    });
  });
});
