import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, switchMap, catchError, of } from 'rxjs';
import { Router } from '@angular/router';
import { AuthUser } from '../models/auth';
import { TeacherDTO } from '../models/teacher';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = '/api/auth';

  private currentUserSubject = new BehaviorSubject<AuthUser | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {}

  loadCurrentUser(): Observable<AuthUser | null> {
    return this.http.get<AuthUser>(`${this.baseUrl}/current-user`, { withCredentials: true }).pipe(
      tap((user) => this.currentUserSubject.next(user)),
      catchError(() => {
        this.currentUserSubject.next(null);
        return of(null);
      }),
    );
  }

  login(username: string, password: string): Observable<AuthUser | null> {
    const body = new URLSearchParams();
    body.set('username', username);
    body.set('password', password);
    return this.http
      .post(`${this.baseUrl}/login`, body.toString(), {
        withCredentials: true,
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      })
      .pipe(switchMap(() => this.loadCurrentUser()));
  }

  logout(): Observable<unknown> {
    return this.http.post(`${this.baseUrl}/logout`, {}, { withCredentials: true }).pipe(
      tap(() => {
        this.currentUserSubject.next(null);
        this.router.navigate(['/login']);
      }),
    );
  }

  addRoleForTeacher(teacherId: number): Observable<void> {
    return this.http.post<void>(
      `/api/users/${teacherId}/roles`,
      { role: 'BIBLIOTHEEKBEHEERDER' },
      { withCredentials: true },
    );
  }

  get currentUser(): AuthUser | null {
    return this.currentUserSubject.getValue();
  }

  get isLoggedIn(): boolean {
    return this.currentUserSubject.getValue() !== null;
  }

  hasRole(...roles: string[]): boolean {
    const user = this.currentUserSubject.getValue();
    return !!user && user.roles.some((r) => roles.includes(r));
  }
}
