import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Language } from '../models/language';
import { ApiService } from './api';

@Injectable({
  providedIn: 'root',
})
export class LanguageService {
  private readonly endpoint = 'language';

  constructor(private apiService: ApiService) {}

  getAll(): Observable<Language[]> {
    return this.apiService.get<Language[]>(this.endpoint);
  }

  getById(id: number): Observable<Language> {
    return this.apiService.get<Language>(this.endpoint);
  }

  getByName(query: string): Observable<Language[]> {
    return this.apiService.get<Language[]>(`${this.endpoint}/search/${query}`);
  }

  addLanguage(language: Omit<Language, 'id'>): Observable<Language> {
    return this.apiService.post<Language>(`${this.endpoint}`, language);
  }

  getByCode(code: string): Observable<Language> {
    return this.apiService.get<Language>(`${this.endpoint}/code/${code}`);
  }
}
