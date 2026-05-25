import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Account, Password } from '../models/account';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AccountService {
  private readonly endpoint = 'account';

  constructor(private apiService: ApiService) {}

  getInfo(): Observable<Account[]> {
    return this.apiService.get<Account[]>(this.endpoint);
  }

  updatePassword(body: Password): Observable<boolean> {
    return this.apiService.put<boolean>(`${this.endpoint}/password`, body);
  }
}
