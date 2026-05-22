import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Observable } from 'rxjs';
import { Genre } from '../models/genre';

@Injectable({
  providedIn: 'root',
})
export class GenreService {
  private readonly endpoint = 'genre';

  constructor(private apiService: ApiService) {}

  getAll(): Observable<Genre[]> {
    return this.apiService.get<Genre[]>(this.endpoint);
  }

  getById(id: number): Observable<Genre> {
    return this.apiService.get<Genre>(this.endpoint);
  }

  getByName(query: string): Observable<Genre[]> {
    return this.apiService.get<Genre[]>(`${this.endpoint}/search/${query}`);
  }

  addAuthor(genre: Omit<Genre, 'id'>): Observable<Genre> {
    return this.apiService.post<Genre>(`${this.endpoint}`, genre);
  }
}
