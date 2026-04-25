import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Observable } from 'rxjs';
import { BookCard, BookDetail, BookFilter, BookResult, CreateBook } from '../models/book';
import { HttpParams } from '@angular/common/http';

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
    let params = new HttpParams();

    // Query toevoegen indien aanwezig
    if (query) {
      params = params.set('q', query);
    }

    // Filters transformeren naar HttpParams
    if (filters.genre) {
      params = params.set('genres', filters.genre.join(','));
    }
    if (filters.language) {
      params = params.set('language', filters.language.toString());
    }
    if (filters.fiction !== undefined && filters.fiction !== null) {
      params = params.set('fiction', filters.fiction.toString());
    }
    if (filters.author) {
      params = params.set('authorIds', filters.author.join(','));
    }
    
    // De cruciale toevoeging voor CLIB niveau
    if (filters.clibs && filters.clibs.length > 0) {
      params = params.set('clibs', filters.clibs.join(','));
    }

    if (filters.pages) {
      if (filters.pages[0] != null) params = params.set('pagesMin', filters.pages[0].toString());
      if (filters.pages[1] != null) params = params.set('pagesMax', filters.pages[1].toString());
    }

    // We gebruiken hier het filter endpoint omdat search meestal beperkter is
    return this.apiService.get<BookResult[]>(`${this.endpoint}/filter`, params);
  }

  getById(id: number): Observable<BookDetail> {
    return this.apiService.get<BookDetail>(`${this.endpoint}/${id}`);
  }

  getRelated(id: number): Observable<BookCard[]> {
    return this.apiService.get<BookCard[]>(`${this.endpoint}/${id}/related`);
  }

  addBook(book: CreateBook): Observable<BookDetail> {
    return this.apiService.post<BookDetail>(this.endpoint, book);
  }
}