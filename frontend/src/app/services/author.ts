import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Author } from '../models/author';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthorService {
  private readonly endpoint = 'author';

  constructor(private apiService: ApiService) {}

  getAll(): Observable<Author[]> {
    return this.apiService.get<Author[]>(this.endpoint);
  }

  getById(id: number): Observable<Author> {
    return this.apiService.get<Author>(this.endpoint);
  }

  getByName(query: string): Observable<Author[]> {
    return this.apiService.get<Author[]>(`${this.endpoint}/search/${query}`);
  }

  addAuthor(author: Omit<Author, 'id'>): Observable<Author> {
    return this.apiService.post<Author>(`${this.endpoint}`, author);
  }
}
