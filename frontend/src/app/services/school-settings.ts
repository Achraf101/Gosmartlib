import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Observable } from 'rxjs';
import { SchoolSettings } from '../models/school-settings';

@Injectable({ providedIn: 'root' })
export class SchoolSettingsService {
  private endpoint = 'school-settings';

  constructor(private apiService: ApiService) {}

  getSettings(): Observable<SchoolSettings> {
    return this.apiService.get<SchoolSettings>(this.endpoint);
  }

  updateSettings(settings: SchoolSettings): Observable<SchoolSettings> {
    return this.apiService.put<SchoolSettings>(this.endpoint, settings);
  }
}
