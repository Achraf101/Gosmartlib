import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Observable } from 'rxjs';
import { LocationBook } from '../models/locationBook';
import { SchoolStats } from '../models/locationBookDetail';

@Injectable({
  providedIn: 'root',
})
export class LocationBookService {
  private readonly endpoint = 'locationbook';

  constructor(private apiService: ApiService) {}

  createLocationBook(locationBook: Omit<LocationBook, 'id'>): Observable<LocationBook> {
    return this.apiService.post<LocationBook>(this.endpoint, locationBook);
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
}
