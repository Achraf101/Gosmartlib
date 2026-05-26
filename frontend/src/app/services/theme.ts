import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Observable } from 'rxjs';
import { Theme } from '../models/theme';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private readonly endpoint = 'theme';

  constructor(private apiService: ApiService) {}

  getAll(): Observable<Theme[]> {
    return this.apiService.get<Theme[]>(this.endpoint);
  }

  addTheme(theme: { name: string }) {
    return this.apiService.post(this.endpoint, theme);
  }
}
