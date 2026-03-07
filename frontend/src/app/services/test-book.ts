import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api';
import { TestBook } from '../models/test-book';
import { Page } from '../models/page';

@Injectable({
  providedIn: 'root',
})
export class TestBookService {
  private readonly endpoint = 'test-book';

  constructor(private apiService: ApiService) {}

  getBooks(page: number, size: number): Observable<Page<TestBook>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.apiService.get<Page<TestBook>>(this.endpoint, params);
  }
}
