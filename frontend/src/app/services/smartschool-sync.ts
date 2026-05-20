import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api';

@Injectable({
  providedIn: 'root',
})
export class SmartschoolSyncService {
  constructor(private api: ApiService) {}

  syncSchool(schoolId: number): Observable<string> {
    return this.api.post<string>(`smartschool/sync/${schoolId}`, {});
  }
}
