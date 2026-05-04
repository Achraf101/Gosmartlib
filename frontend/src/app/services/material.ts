import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Material } from '../models/material';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class MaterialService {
  private readonly endpoint = 'material';

  constructor(private apiService: ApiService) {}

  getAll(id: number): Observable<Material[]> {
    return this.apiService.get<Material[]>(`${this.endpoint}/${id}`);
  }

  uploadMaterial(file: any): Observable<Material> {
    return this.apiService.post<Material>(`${this.endpoint}`, file);
  }
}
