import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Observable } from 'rxjs';
import { Bookmarked } from '../models/bookmarked';

@Injectable({
  providedIn: 'root',
})
export class BookmarkedService {
  private endpoint = 'bookmarked';

  constructor(private apiService: ApiService) {}

  getBookmarked(userId: number): Observable<Bookmarked[]> {
    return this.apiService.get<Bookmarked[]>(`${this.endpoint}/${userId}`);
  }

  isBookmarked(userId: number, bookId: number): Observable<boolean> {
    return this.apiService.get<boolean>(`${this.endpoint}/${userId}/${bookId}`);
  }

  toggleBookmarked(userId: number, bookId: number): Observable<boolean> {
    return this.apiService.post<boolean>(`${this.endpoint}/${userId}/${bookId}`, {});
  }
}
