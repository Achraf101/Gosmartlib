import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api';
import { Book } from '../models/book';

@Injectable({
  providedIn: 'root',
})
export class BookService {
  private readonly endpoint = 'book';

  constructor(private apiService: ApiService) {}

  getAll(): Observable<Book[]> {
    return this.apiService.get<Book[]>(this.endpoint);
  }
}
