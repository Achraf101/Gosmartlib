import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface BulkPreviewItem {
  row: number;
  isbn: string;
  found: boolean;
  title: string | null;
  author: string | null;
  coverUrl: string | null;
}

export interface BulkPreviewResult {
  items: BulkPreviewItem[];
  total: number;
  foundCount: number;
  notFoundCount: number;
}

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
