import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Publisher } from '../models/publisher';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class PublisherService {
  private readonly endpoint = 'publisher';

  constructor(private apiService: ApiService) {}

  getAll(): Observable<Publisher[]> {
    return this.apiService.get<Publisher[]>(this.endpoint);
  }

  getById(id: number): Observable<Publisher> {
    return this.apiService.get<Publisher>(this.endpoint);
  }

  getByName(query: string): Observable<Publisher> {
    return this.apiService.get<Publisher>(`${this.endpoint}/search/${query}`);
  }

  addPublisher(publisher: Omit<Publisher, 'id'>): Observable<Publisher> {
    return this.apiService.post<Publisher>(`${this.endpoint}`, publisher);
  }
}
