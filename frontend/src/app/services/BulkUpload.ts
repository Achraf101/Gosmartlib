import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BulkPreviewResult } from '../models/bulk';

@Injectable({ providedIn: 'root' })
export class BulkUpload {
  private readonly apiUrl = '/api/excel/book';

  constructor(private http: HttpClient) {}

  downloadTemplate(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/template`, {
      responseType: 'blob',
    });
  }

  preview(file: File): Observable<BulkPreviewResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<BulkPreviewResult>(`${this.apiUrl}/bulk-preview`, formData);
  }
}
