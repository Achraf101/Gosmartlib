import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class SectionService {
  private readonly endpoint = 'section';

  constructor(private apiService: ApiService) {}

  getAll(): Observable<any[]> {
    return this.apiService.get<any[]>(this.endpoint);
  }

  getBooksBySection(id: number): Observable<any[]> {
    return this.apiService.get<any[]>(`${this.endpoint}/${id}/books`);
  }
}