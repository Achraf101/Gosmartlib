import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Observable } from 'rxjs';
import { HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class SectionService {
  private readonly endpoint = 'section';

  constructor(private apiService: ApiService) {}

  getAll(): Observable<any[]> {
    return this.apiService.get<any[]>(this.endpoint);
  }

  getBooksBySection(id: number): Observable<any[]> {
    return this.apiService.get<any[]>(`${this.endpoint}/${id}/books`);
  }

  getBookBySectionAndGrade(id: number, grade: number): Observable<any> {
    const params = new HttpParams().set('grade', grade.toString());
    return this.apiService.get<any>(`${this.endpoint}/${id}/books/grade`, params);
  }

  setBookOfMonth(sectionId: number, bookId: number, grade: number): Observable<any> {
  return this.apiService.put<any>(
    `${this.endpoint}/${sectionId}/book?bookId=${bookId}&grade=${grade}`,
    {}
  );
}
}