import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Observable } from 'rxjs';
import { LocationSettings } from '../models/location-settings';

@Injectable({ providedIn: 'root' })
export class LocationSettingsService {
  private endpoint = 'location-settings';

  constructor(private apiService: ApiService) {}

  getSettings(): Observable<LocationSettings> {
    return this.apiService.get<LocationSettings>(this.endpoint);
  }

  updateSettings(settings: LocationSettings): Observable<LocationSettings> {
    return this.apiService.put<LocationSettings>(this.endpoint, settings);
  }
}
