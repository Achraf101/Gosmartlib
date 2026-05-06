import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api';
import { StudentReportDTO } from '../models/student-report';

@Injectable({
  providedIn: 'root',
})
export class StudentReportService {
  private readonly endpoint = 'students';

  constructor(private apiService: ApiService) {}

  getReport(studentId: number): Observable<StudentReportDTO> {
    return this.apiService.get<StudentReportDTO>(`${this.endpoint}/${studentId}/report`);
  }
}
