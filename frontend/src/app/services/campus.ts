import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Observable } from 'rxjs';
import { Campus } from '../models/campus';

@Injectable({
  providedIn: 'root',
})
export class CampusService {
  private readonly endpoint = 'campus';

  constructor(private apiService: ApiService) {}

  createCampus(campus: Omit<Campus, 'id'>): Observable<Campus> {
    return this.apiService.post<Campus>(this.endpoint, campus);
  }
}
