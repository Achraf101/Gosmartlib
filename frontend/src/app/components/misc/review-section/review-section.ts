import { Component, Input, OnInit, Output, EventEmitter, ViewChildren, QueryList } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RatingModule } from 'primeng/rating';
import { MenuModule } from 'primeng/menu';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MenuItem } from 'primeng/api';
import { ApiService } from '../../../services/api';
import { Review } from '../../../models/review';


@Component({
  selector: 'app-review-section',
  standalone: true,
  imports: [CommonModule, FormsModule, RatingModule, MenuModule, ConfirmDialogModule],
  providers: [ConfirmationService],
  templateUrl: './review-section.html',
  styleUrl: './review-section.css',
})
export class ReviewSectionComponent implements OnInit {
  @Input() bookId!: number;
  @Output() reviewAdded = new EventEmitter<void>();

  reviews: Review[] = [];
  menuItems: MenuItem[] = [];
  newRating = 0;
  newContent = '';
  error = '';
  submitted = false;
  currentUserId: number | null = null;

  constructor(private apiService: ApiService, private confirmationService: ConfirmationService) {}

  ngOnInit(): void {
    this.loadReviews();
    this.loadCurrentUser();
  }

  loadCurrentUser(): void {
    this.apiService.get<any>('auth/me/id').subscribe({
        next: (user) => (this.currentUserId = user.userId),
    });
}

  loadReviews(): void {
    this.apiService.get<Review[]>(`review/book/${this.bookId}`).subscribe({
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
    this.apiService.post<Review>(`review/book/${this.bookId}`, {
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

  onMenuShow(review: Review): void {
  this.menuItems = this.getMenuItems(review);
  }

  getMenuItems(review: Review): MenuItem[] {
    const items: MenuItem[] = [
      {
        label: 'Recensie rapporteren',
        icon: 'pi pi-flag',
        command: () => this.reportReview(review),
      }
    ];

    if (review.user_id === this.currentUserId) {
      items.unshift({
        label: 'Recensie verwijderen',
        icon: 'pi pi-trash',
        command: () => this.confirmDelete(review),
      });
    }

    return items;
  }

  confirmDelete(review: Review): void {
    this.confirmationService.confirm({
      message: 'Ben je zeker dat je deze recensie wilt verwijderen?',
      header: 'Recensie verwijderen',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Ja, verwijderen',
      rejectLabel: 'Annuleren',
      accept: () => this.deleteReview(review.id!),
    });
  }

  deleteReview(reviewId: number): void {
    this.apiService.delete(`review/${reviewId}`).subscribe({
      next: () => this.loadReviews(),
    });
  }

  reportReview(review: Review): void {
    // later implementeren
  }

  getStars(rating: number): number[] {
    return Array(rating).fill(0);
  }

  getEmptyStars(rating: number): number[] {
    return Array(5 - rating).fill(0);
  }
}