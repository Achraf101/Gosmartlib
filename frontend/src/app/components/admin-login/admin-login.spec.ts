import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { AdminLoginComponent } from './admin-login';
import { AuthService } from '../../services/auth';

describe('AdminLoginComponent', () => {
  let component: AdminLoginComponent;
  let fixture: ComponentFixture<AdminLoginComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    const authSpy = jasmine.createSpyObj('AuthService', ['login']);

    await TestBed.configureTestingModule({
      imports: [AdminLoginComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: AuthService, useValue: authSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminLoginComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    router = TestBed.inject(Router);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have empty fields initially', () => {
    expect(component.username).toBe('');
    expect(component.password).toBe('');
    expect(component.errorMessage).toBe('');
    expect(component.loading).toBeFalse();
  });

  it('should show error when username is empty', () => {
    component.username = '';
    component.password = 'password';

    component.onSubmit();

    expect(component.errorMessage).toBe('Vul gebruikersnaam en wachtwoord in.');
    expect(authService.login).not.toHaveBeenCalled();
  });

  it('should show error when password is empty', () => {
    component.username = 'admin';
    component.password = '';

    component.onSubmit();

    expect(component.errorMessage).toBe('Vul gebruikersnaam en wachtwoord in.');
    expect(authService.login).not.toHaveBeenCalled();
  });

  it('should show error when both fields are whitespace', () => {
    component.username = '   ';
    component.password = '   ';

    component.onSubmit();

    expect(component.errorMessage).toBe('Vul gebruikersnaam en wachtwoord in.');
    expect(authService.login).not.toHaveBeenCalled();
  });

  it('should call authService.login and navigate on success', () => {
    spyOn(router, 'navigate');
    authService.login.and.returnValue(of({ username: 'admin', role: 'ROLE_ADMIN' }));

    component.username = 'admin';
    component.password = 'password';
    component.onSubmit();

    expect(authService.login).toHaveBeenCalledWith('admin', 'password');
    expect(component.loading).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should show error message on login failure', () => {
    authService.login.and.returnValue(throwError(() => new Error('Unauthorized')));

    component.username = 'admin';
    component.password = 'wrongpassword';
    component.onSubmit();

    expect(authService.login).toHaveBeenCalledWith('admin', 'wrongpassword');
    expect(component.loading).toBeFalse();
    expect(component.errorMessage).toBe('Ongeldige gebruikersnaam of wachtwoord.');
  });

  it('should clear previous error message on new submit', () => {
    authService.login.and.returnValue(of({ username: 'admin', role: 'ROLE_ADMIN' }));
    spyOn(router, 'navigate');

    component.errorMessage = 'Previous error';
    component.username = 'admin';
    component.password = 'password';
    component.onSubmit();

    expect(component.errorMessage).toBe('');
  });
});
