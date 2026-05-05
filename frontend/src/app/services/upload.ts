import { Injectable } from '@angular/core';
import { Cover } from '../models/cover';
import { ApiService } from './api';
import { Observable } from 'rxjs';
import { Material } from '../models/material';

@Injectable({
  providedIn: 'root',
})
export class UploadService {
  private readonly endpoint = 'upload';

  constructor(private apiService: ApiService) {}

  addCover(file: FormData): Observable<Cover> {
    return this.apiService.post<Cover>(`${this.endpoint}/cover`, file);
  }

  addMaterial(file: FormData): Observable<Material> {
    return this.apiService.post<Material>(`${this.endpoint}/material`, file);
  }
}
