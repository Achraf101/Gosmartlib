import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Observable } from 'rxjs';
import { CreateLoanDTO, LoanDTO, LoanStatus } from '../models/loan';

@Injectable({
  providedIn: 'root',
})
export class LoanService {
  private readonly endpoint = 'loan';

  constructor(private apiService: ApiService) {}

  getRequested(): Observable<LoanDTO[]> {
    return this.apiService.get<LoanDTO[]>(`${this.endpoint}/requested`);
  }

  createLoan(loan: Omit<CreateLoanDTO, 'id'>): Observable<LoanDTO[]> {
    return this.apiService.post<LoanDTO[]>(this.endpoint, loan);
  }

  updateNote(id: number, note: string): Observable<LoanDTO> {
    return this.apiService.put<LoanDTO>(`${this.endpoint}/${id}/note`, { note });
  }

  changeStatus(id: number, status: LoanStatus): Observable<LoanDTO> {
    return this.apiService.put<LoanDTO>(`${this.endpoint}/${id}/status`, {
      status,
    });
  }

  getByUser(): Observable<LoanDTO[]> {
    return this.apiService.get<LoanDTO[]>(`${this.endpoint}/user`);
  }

  delete(loanId: number): Observable<void> {
    return this.apiService.delete<void>(`${this.endpoint}/${loanId}`);
  }

  getOverdue(): Observable<LoanDTO[]> {
    return this.apiService.get<LoanDTO[]>(`${this.endpoint}/overdue`);
  }

  getTopBooks(): Observable<{ title: string; count: number }[]> {
    return this.apiService.get<{ title: string; count: number }[]>(`${this.endpoint}/top-books`);
  }

  getOverdueLength(): Observable<number> {
    return this.apiService.get<number>(`${this.endpoint}/overdue/length`);
  }

  getDueSoon(): Observable<LoanDTO[]> {
    return this.apiService.get<LoanDTO[]>(`${this.endpoint}/due-soon`);
  }

  getDueSoonLength(): Observable<number> {
    return this.apiService.get<number>(`${this.endpoint}/due-soon/length`);
  }

  getTopGenres(): Observable<{ title: string; count: number }[]> {
    return this.apiService.get<{ title: string; count: number }[]>(`${this.endpoint}/top-genres`);
  }

  getByState(loanState: LoanStatus): Observable<LoanDTO[]> {
    return this.apiService.get<LoanDTO[]>(`${this.endpoint}/state/${loanState}`);
  }
}
