import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter, Router } from '@angular/router';

import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let httpMock: HttpTestingController;
  let http: HttpClient;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });

    httpMock = TestBed.inject(HttpTestingController);
    http = TestBed.inject(HttpClient);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should add withCredentials to requests', () => {
    http.get('/api/test').subscribe();

    const req = httpMock.expectOne('/api/test');
    expect(req.request.withCredentials).toBeTrue();
    req.flush({});
  });

  it('should navigate to /login on 401 error', () => {
    spyOn(router, 'navigate');
    spyOnProperty(router, 'url', 'get').and.returnValue('/catalogus');

    http.get('/api/test').subscribe({
      error: () => {
        expect(router.navigate).toHaveBeenCalledWith(['/login']);
      },
    });

    const req = httpMock.expectOne('/api/test');
    req.flush(null, { status: 401, statusText: 'Unauthorized' });
  });

  it('should not navigate to /login on 401 if already on login page', () => {
    spyOn(router, 'navigate');
    spyOnProperty(router, 'url', 'get').and.returnValue('/login');

    http.get('/api/test').subscribe({
      error: () => {
        expect(router.navigate).not.toHaveBeenCalled();
      },
    });

    const req = httpMock.expectOne('/api/test');
    req.flush(null, { status: 401, statusText: 'Unauthorized' });
  });

  it('should not navigate on non-401 errors', () => {
    spyOn(router, 'navigate');
    spyOnProperty(router, 'url', 'get').and.returnValue('/catalogus');

    http.get('/api/test').subscribe({
      error: () => {
        expect(router.navigate).not.toHaveBeenCalled();
      },
    });

    const req = httpMock.expectOne('/api/test');
    req.flush(null, { status: 500, statusText: 'Server Error' });
  });
});
