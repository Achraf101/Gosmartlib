import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Observable } from 'rxjs';
import { BookDetail, BookFilter, BookResult, CreateBook } from '../models/book';
import { Page } from '../models/page';
import { HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class BookService {
  private readonly endpoint = 'book';

  constructor(private apiService: ApiService) {}

  getAll(page: number = 0, size: number = 5): Observable<Page<BookResult>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.apiService.get<Page<BookResult>>(this.endpoint, params);
  }

  getFiltered(query: string, filters: BookFilter): Observable<BookResult[]> {
    // transform bookfilter to http params using eg: genre=1,2,3 (spring boot supports this)
    // params: HttpParams;
    return this.apiService.get<BookResult[]>(`${this.endpoint}/search/${query}` /*params*/);
  }

  getById(id: number): Observable<BookDetail> {
    return this.apiService.get<BookDetail>(`${this.endpoint}/${id}`);
  }

  // receives book detail with id, etc
  addBook(book: CreateBook): Observable<BookDetail> {
    return this.apiService.post<BookDetail>(this.endpoint, book);
  }
}
