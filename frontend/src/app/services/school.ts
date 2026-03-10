import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { School } from '../models/school';
import { Observable } from 'rxjs';
import { Campus } from '../models/campus';

@Injectable({
  providedIn: 'root',
})
export class SchoolService {
  private readonly endpoint = 'school';

  constructor(private apiService: ApiService) {}

  addSchool(school: Omit<School, 'id'>): Observable<School> {
    return this.apiService.post<School>(this.endpoint, school);
  }

  getAll(): Observable<School[]> {
    return this.apiService.get<School[]>(this.endpoint);
  }
}
