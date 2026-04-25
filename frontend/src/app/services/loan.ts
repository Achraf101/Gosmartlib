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

  createLoan(loan: Omit<CreateLoanDTO, 'id'>): Observable<CreateLoanDTO> {
    const body = {
      ...loan,
      start: this.formatDate(loan.start),
      end: this.formatDate(loan.end),
    };
    return this.apiService.post<CreateLoanDTO>(this.endpoint, body);
  }

  updateNote(id: number, note: string): Observable<LoanDTO> {
    return this.apiService.put<LoanDTO>(`${this.endpoint}/${id}/note`, { note });
  }

  changeStatus(id: number, status: LoanStatus): Observable<LoanDTO> {
    return this.apiService.put<LoanDTO>(`${this.endpoint}/${id}/status`, {
      status,
    });
  }

  private formatDate(date: Date): string {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }
}
