import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Observable } from 'rxjs';
import { Hello } from '../models/hello';

@Injectable({
  providedIn: 'root',
})
export class HelloService {

  private readonly endpoint = 'hello';

  constructor(private apiService: ApiService) { }

  getAll(): Observable<Hello[]> {
    return this.apiService.get<Hello[]>(this.endpoint);
  }

  getById(id: number): Observable<Hello> {
    return this.apiService.get<Hello>(`${this.endpoint}/${id}`);
  }
}
