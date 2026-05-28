import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Observable } from 'rxjs';
import {
  BookCard,
  BookDetail,
  BookFilter,
  BookLookupDTO,
  BookResult,
  CreateBook,
} from '../models/book';
import { Page } from '../models/page';
import { HttpParams } from '@angular/common/http';
import { AuthService } from './auth';

@Injectable({
  providedIn: 'root',
})
export class BookService {
  private readonly endpoint = 'book';

  constructor(
    private apiService: ApiService,
    private auth: AuthService,
  ) {}

  getAll(page: number = 0, size: number = 5, full: boolean = false): Observable<Page<BookResult>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('full', full);
    return this.apiService.get<Page<BookResult>>(this.endpoint, params);
  }

  search(query: string, page: number = 0, size: number = 5): Observable<Page<BookResult>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.apiService.get<Page<BookResult>>(
      `${this.endpoint}/search/${encodeURIComponent(query)}`,
      params,
    );
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

  lookupByIsbn(isbn: string): Observable<BookLookupDTO> {
    return this.apiService.get<BookLookupDTO>(`${this.endpoint}/isbn/${encodeURIComponent(isbn)}`);
  }

  getIaPreview(id: number): Observable<{ ia_id: string }> {
    return this.apiService.get<{ ia_id: string }>(`${this.endpoint}/${id}/ia-preview`);
  }

  // get all bookResults
  getAllBookResults(page: number = 0, size: number = 5): Observable<Page<BookResult>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.apiService.get<Page<BookResult>>(`${this.endpoint}/bookResult`, params);
  }

  filter(filters: BookFilter, page: number = 0, size: number = 5): Observable<Page<BookResult>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());

    if (filters.query?.trim()) params = params.set('query', filters.query.trim());
    if (filters.genre)
      filters.genre.forEach((g) => (params = params.append('genres', g.toString())));

    if (filters.theme)
      filters.theme.forEach((h) => (params = params.append('themes', h.toString())));

    if (filters.language) params = params.set('language', filters.language.toString());

    if (filters.fiction !== undefined && filters.fiction !== null)
      params = params.set('fiction', filters.fiction.toString());

    if (filters.author) params = params.set('authorIds', filters.author.join(','));

    if (filters.series && filters.series.length > 0)
      params = params.set('seriesIds', filters.series.join(','));

    if (filters.clibs && filters.clibs.length > 0) {
      params = params.set('clibs', filters.clibs.join(','));
    }

    if (filters.pages?.[0] != null) params = params.set('pagesMin', filters.pages[0].toString());

    if (filters.pages?.[1] != null) params = params.set('pagesMax', filters.pages[1].toString());

    if (filters.didactic !== undefined && filters.didactic !== null) {
      params = params.set('didactic', filters.didactic.toString());
    }

    if (filters.location) params = params.set('location', filters.location);
    return this.apiService.get<Page<BookResult>>(`${this.endpoint}/filter`, params);
  }

  updateBook(id: number, data: Partial<CreateBook>): Observable<BookDetail> {
    return this.apiService.put<BookDetail>(`${this.endpoint}/${id}`, data);
  }
}
