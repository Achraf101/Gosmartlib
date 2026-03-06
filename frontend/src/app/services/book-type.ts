import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { BookType } from '../models/book-type';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class BookTypeService {
  private readonly endpoint = 'booktype';

  constructor(private apiService: ApiService) {}

  getAll(): Observable<BookType[]> {
    return this.apiService.get<BookType[]>(this.endpoint);
  }

  getById(id: number): Observable<BookType> {
    return this.apiService.get<BookType>(this.endpoint);
  }

  getByName(query: string): Observable<BookType> {
    return this.apiService.get<BookType>(`${this.endpoint}/search/${query}`);
  }

  addBookType(booktype: Omit<BookType, 'id'>): Observable<BookType> {
    return this.apiService.post<BookType>(`${this.endpoint}`, booktype);
  }
}
