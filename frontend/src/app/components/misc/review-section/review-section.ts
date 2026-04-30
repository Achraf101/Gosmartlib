import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RatingModule } from 'primeng/rating';
import { ApiService } from '../../../services/api';

interface ReviewDTO {
  id?: number;
  bookId?: number;
  rating: number;
  content: string;
  added?: string;
}

@Component({
  selector: 'app-review-section',
  standalone: true,
  imports: [CommonModule, FormsModule, RatingModule],
  templateUrl: './review-section.html',
  styleUrl: './review-section.css',
})
export class ReviewSectionComponent implements OnInit {
  @Input() bookId!: number;
  @Output() reviewAdded = new EventEmitter<void>();

  reviews: ReviewDTO[] = [];
  newRating = 0;
  newContent = '';
  error = '';
  submitted = false;

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.loadReviews();
  }

  loadReviews(): void {
    this.apiService.get<ReviewDTO[]>(`review/book/${this.bookId}`).subscribe({
      next: (reviews) => (this.reviews = reviews),
    });
  }

 submitReview(): void {
     if (this.newRating === 0) {
      this.error = 'Geef een beoordeling.';
      return;
    }
    
    const urlPattern = /((https?|ftp):\/\/|www\.)\S{2,}/i;
    if (urlPattern.test(this.newContent)) {
      this.error = 'Je recensie mag geen URLs bevatten.';
      return;
    }
    this.error = '';
    this.apiService.post<ReviewDTO>(`review/book/${this.bookId}`, {
      rating: this.newRating,
      content: this.newContent,
    }).subscribe({
      next: () => {
        this.newRating = 0;
        this.newContent = '';
        this.submitted = true;
        this.loadReviews();
        this.reviewAdded.emit();
      },
      error: (err) => {
        this.error = err.error ?? 'Er is een fout opgetreden.';
      }
    });
  }

  getStars(rating: number): number[] {
    return Array(rating).fill(0);
  }

  getEmptyStars(rating: number): number[] {
    return Array(5 - rating).fill(0);
  }
}