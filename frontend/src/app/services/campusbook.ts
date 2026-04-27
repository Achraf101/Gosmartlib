import { Injectable } from '@angular/core';
import { CampusBook } from '../models/CampusBook';
import { ApiService } from './api';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class CampusBookService {
  private readonly endpoint = 'campusbook';

  constructor(private apiService: ApiService) {}

  createCampusBook(campusBook: Omit<CampusBook, 'id'>): Observable<CampusBook> {
    return this.apiService.post<CampusBook>(this.endpoint, campusBook);
  }

  getAll(): Observable<CampusBook[]> {
    return this.apiService.get<CampusBook[]>(this.endpoint);
  }

  getByCampus(campusId: number, page: number, rows: number): Observable<any> {
    return this.apiService.get<any>(
      `${this.endpoint}/campus/${campusId}?page=${page}&size=${rows}`,
    );
  }
  getCampusBook(campusId: number, bookId: number): Observable<CampusBook> {
    return this.apiService.get<CampusBook>(`${this.endpoint}/${campusId}/books/${bookId}`);
  }
}
