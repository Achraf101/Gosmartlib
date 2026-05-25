import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api';
import { AuthUser } from '../models/auth';
import { TeacherDTO } from '../models/teacher';

@Injectable({
  providedIn: 'root',
})
export class SmartschoolLookupService {
  constructor(private api: ApiService) {}
  private readonly endpoint = 'smartschool/lookup';

  getAllTeachersForSchool(schoolId: number): Observable<TeacherDTO[]> {
    return this.api.get<TeacherDTO[]>(`${this.endpoint}/${schoolId}/teachers`);
  }
}
