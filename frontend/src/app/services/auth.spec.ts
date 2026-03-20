import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter, Router } from '@angular/router';

import { AuthService } from './auth';
import { AuthUser } from '../models/auth';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return false for isLoggedIn initially', () => {
    expect(service.isLoggedIn).toBeFalse();
  });

  it('should return null for currentUser initially', () => {
    expect(service.currentUser).toBeNull();
  });

  it('loadCurrentUser should set currentUser on success', () => {
    const mockUser: AuthUser = { username: 'admin', role: 'ROLE_ADMIN' };

    service.loadCurrentUser().subscribe(user => {
      expect(user).toEqual(mockUser);
      expect(service.currentUser).toEqual(mockUser);
      expect(service.isLoggedIn).toBeTrue();
    });

    const req = httpMock.expectOne('/api/auth/me');
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBeTrue();
    req.flush(mockUser);
  });

  it('loadCurrentUser should set null on error', () => {
    service.loadCurrentUser().subscribe(user => {
      expect(user).toBeNull();
      expect(service.currentUser).toBeNull();
      expect(service.isLoggedIn).toBeFalse();
    });

    const req = httpMock.expectOne('/api/auth/me');
    req.flush(null, { status: 401, statusText: 'Unauthorized' });
  });

  it('login should POST credentials and then load current user', () => {
    const mockUser: AuthUser = { username: 'admin', role: 'ROLE_ADMIN' };

    service.login('admin', 'password').subscribe(user => {
      expect(user).toEqual(mockUser);
      expect(service.isLoggedIn).toBeTrue();
    });

    const loginReq = httpMock.expectOne('/api/auth/login');
    expect(loginReq.request.method).toBe('POST');
    expect(loginReq.request.body).toContain('username=admin');
    expect(loginReq.request.body).toContain('password=password');
    expect(loginReq.request.headers.get('Content-Type')).toBe('application/x-www-form-urlencoded');
    expect(loginReq.request.withCredentials).toBeTrue();
    loginReq.flush({ message: 'Login successful' });

    const meReq = httpMock.expectOne('/api/auth/me');
    meReq.flush(mockUser);
  });

  it('logout should POST and clear current user', () => {
    spyOn(router, 'navigate');

    service.logout().subscribe(() => {
      expect(service.currentUser).toBeNull();
      expect(service.isLoggedIn).toBeFalse();
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    });

    const req = httpMock.expectOne('/api/auth/logout');
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBeTrue();
    req.flush({ message: 'Logged out' });
  });
});
