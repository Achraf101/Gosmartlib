import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private readonly endpoint = 'notify';

  constructor(private apiService: ApiService) {}

  notify(loan_id: number): Observable<boolean> {
    return this.apiService.post<boolean>(`${this.endpoint}/${loan_id}`);
  }
}
