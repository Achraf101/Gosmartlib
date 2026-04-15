import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Observable } from 'rxjs';
import { Series } from '../models/series';

@Injectable({
  providedIn: 'root',
})
export class SeriesService {
  private readonly endpoint = 'series';

  constructor(private apiService: ApiService) {}

  
  getAll(): Observable<Series[]> {
    return this.apiService.get<Series[]>(this.endpoint);
  }

  search(query: string): Observable<Series[]> {
    return this.apiService.get<Series[]>(`${this.endpoint}/search/${encodeURIComponent(query)}`);
  }


  create(series: any): Observable<Series> {
  return this.apiService.post<Series>(this.endpoint, series);
}

  
  getByAuthor(authorId: number): Observable<Series[]> {
    return this.apiService.get<Series[]>(`${this.endpoint}/author/${authorId}`);
  }
}