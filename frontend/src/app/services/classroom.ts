import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api';
import { ClassroomDTO, StudentPreviewDTO } from '../models/classroom';

@Injectable({
  providedIn: 'root',
})
export class ClassroomService {
  private readonly endpoint = 'classrooms';

  constructor(private apiService: ApiService) {}

  getMyClassrooms(): Observable<ClassroomDTO[]> {
    return this.apiService.get<ClassroomDTO[]>(this.endpoint);
  }

  getStudents(classroomId: number): Observable<StudentPreviewDTO[]> {
    return this.apiService.get<StudentPreviewDTO[]>(`${this.endpoint}/${classroomId}/students`);
  }
}
