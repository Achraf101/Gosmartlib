import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { ApiService } from './api';

@Injectable({ providedIn: 'root' })
export class GamificationService {
  private totalBooksSubject = new BehaviorSubject<number>(0);
  totalBooks$ = this.totalBooksSubject.asObservable();

  constructor(private apiService: ApiService) {}

  load(): void {
    this.apiService.get<any>('gamification').subscribe({
      next: (data) => this.totalBooksSubject.next(data.total_books),
    });
  }
}