import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Observable } from 'rxjs';
import { Location } from '../models/location';

@Injectable({
  providedIn: 'root',
})
export class LocationService {
  private readonly endpoint = 'location';

  constructor(private apiService: ApiService) {}

  createLocation(location: Omit<Location, 'id'>): Observable<Location> {
    return this.apiService.post<Location>(this.endpoint, location);
  }

  getAll(): Observable<Location[]> {
    return this.apiService.get<Location[]>(this.endpoint);
  }

  getById(id: number): Observable<Location> {
    return this.apiService.get<Location>(`${this.endpoint}/${id}`);
  }
}
