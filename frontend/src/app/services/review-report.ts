import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api';
import { ReviewReport } from '../models/review-report';

@Injectable({ providedIn: 'root' })
export class ReviewReportService {

  constructor(private api: ApiService) {}

  reportReview(reviewId: number, note: string): Observable<void> {
    return this.api.post<void>(`review/${reviewId}/rapporteer`, { note });
  }

  getPendingReports(): Observable<ReviewReport[]> {
    return this.api.get<ReviewReport[]>('review/rapportages');
  }

  acceptReport(reportId: number): Observable<void> {
    return this.api.put<void>(`review/rapportages/${reportId}/accepteren`, {});
  }

  rejectReport(reportId: number): Observable<void> {
    return this.api.put<void>(`review/rapportages/${reportId}/weigeren`, {});
  }
}
