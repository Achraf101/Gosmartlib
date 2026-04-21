import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class BulkUpload {
  private readonly apiUrl = '/api/excel/book';

  constructor(private http: HttpClient) {}

  downloadTemplate(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/template`, {
      responseType: 'blob',
    });
  }
}
