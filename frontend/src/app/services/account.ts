import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Password } from '../models/account';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AccountService {
  private readonly endpoint = 'account';

  constructor(private apiService: ApiService) {}

  updatePassword(body: Password): Observable<boolean> {
    return this.apiService.put<boolean>(`${this.endpoint}/password`, body);
  }
}
