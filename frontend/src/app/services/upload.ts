import { Injectable } from '@angular/core';
import { Cover } from '../models/cover';
import { ApiService } from './api';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class UpLoadService {
  private readonly endpoint = 'upload';

  constructor(private apiService: ApiService) {}

  addCover(file: FormData): Observable<Cover> {
    return this.apiService.post<Cover>(`${this.endpoint}/cover`, file);
  }
}
