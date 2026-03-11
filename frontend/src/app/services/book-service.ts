import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Observable } from 'rxjs';
import { BookCard, BookDetail, BookFilter, BookResult, CreateBook } from '../models/book';

@Injectable({
  providedIn: 'root',
})
export class BookService {
  private readonly endpoint = 'book';

  constructor(private apiService: ApiService) {}

  getAll(): Observable<BookResult[]> {
    return this.apiService.get<BookResult[]>(this.endpoint);
  }

  getFiltered(query: string, filters: BookFilter): Observable<BookResult[]> {
    // transform bookfilter to http params using ex: genre=1,2,3 (spring boot supports this)
    // params: HttpParams;
    return this.apiService.get<BookResult[]>(`${this.endpoint}/search/${query}` /*params*/);
  }

  getById(id: number): Observable<BookDetail> {
    return this.apiService.get<BookDetail>(`${this.endpoint}/${id}`);
  }

  // get related books
  getRelated(id: number): Observable<BookCard[]> {
    return this.apiService.get<BookCard[]>(`${this.endpoint}/${id}/related`);
  }

  // receives book detail with id, etc
  addBook(book: CreateBook): Observable<BookDetail> {
    return this.apiService.post<BookDetail>(this.endpoint, book);
  }
}
