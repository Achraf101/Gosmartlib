import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Observable } from 'rxjs';
import { CampusSettings } from '../models/campus-settings';

@Injectable({ providedIn: 'root' })
export class CampusSettingsService {
  private endpoint = 'campus-settings';

  constructor(private apiService: ApiService) {}

  getSettings(): Observable<CampusSettings> {
    return this.apiService.get<CampusSettings>(this.endpoint);
  }

  updateSettings(settings: CampusSettings): Observable<CampusSettings> {
    return this.apiService.put<CampusSettings>(this.endpoint, settings);
  }
}
