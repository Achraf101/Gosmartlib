import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { CreateSchool, School } from '../models/school';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class SchoolService {
  private readonly endpoint = 'school';

  constructor(private apiService: ApiService) {}

  addSchool(school: Omit<CreateSchool, 'id'>): Observable<CreateSchool> {
    return this.apiService.post<CreateSchool>(this.endpoint, school);
  }

  getAll(): Observable<School[]> {
    return this.apiService.get<School[]>(this.endpoint);
  }

  getById(id: number): Observable<School> {
    return this.apiService.get<School>(`${this.endpoint}/${id}`);
  }

  updateSchool(id: number, school: Omit<School, 'id'>): Observable<School> {
  return this.apiService.put<School>(`${this.endpoint}/${id}`, school);
}
}
