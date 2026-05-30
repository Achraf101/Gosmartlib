import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Observable } from 'rxjs';
import { LocationBook } from '../models/locationBook';
import { LocationAvailability, LocationBookDetail, SchoolStats } from '../models/locationBookDetail';
import { BookCopyDetail, CopyStatus } from '../models/bookCopy';

@Injectable({
  providedIn: 'root',
})
export class LocationBookService {
  private readonly endpoint = 'locationbook';
  private readonly copyEndpoint = 'bookcopy';

  constructor(private apiService: ApiService) {}

  createLocationBook(locationBook: Omit<LocationBook, 'id'>): Observable<LocationBookDetail> {
    return this.apiService.post<LocationBookDetail>(this.endpoint, locationBook);
  }

  getAll(): Observable<LocationBook[]> {
    return this.apiService.get<LocationBook[]>(this.endpoint);
  }

  getByLocation(locationId: number, page: number, rows: number): Observable<any> {
    return this.apiService.get<any>(
      `${this.endpoint}/location/${locationId}?page=${page}&size=${rows}`,
    );
  }

  getLocationBook(locationId: number, bookId: number): Observable<LocationBook> {
    return this.apiService.get<LocationBook>(`${this.endpoint}/${locationId}/books/${bookId}`);
  }

  getSchoolStats(): Observable<SchoolStats> {
    return this.apiService.get<SchoolStats>(`${this.endpoint}/stats`);
  }

  getAvailabilityByBook(bookId: number): Observable<LocationAvailability[]> {
    return this.apiService.get<LocationAvailability[]>(`${this.endpoint}/book/${bookId}`);
  }

  getCopiesByLocationBook(locationBookId: number): Observable<BookCopyDetail[]> {
    return this.apiService.get<BookCopyDetail[]>(`${this.copyEndpoint}/locationbook/${locationBookId}`);
  }

  getCopyByAccessionId(accessionId: string): Observable<BookCopyDetail> {
    return this.apiService.get<BookCopyDetail>(`${this.copyEndpoint}/by-accession/${accessionId}`);
  }

  updateCopyStatus(id: number, status: CopyStatus): Observable<BookCopyDetail> {
    return this.apiService.put<BookCopyDetail>(`${this.copyEndpoint}/${id}/status`, { status });
  }
}
