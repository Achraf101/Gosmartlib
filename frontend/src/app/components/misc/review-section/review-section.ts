import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RatingModule } from 'primeng/rating';
import { MenuModule } from 'primeng/menu';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { Dialog } from 'primeng/dialog';
import { Button } from 'primeng/button';
import { ConfirmationService, MenuItem } from 'primeng/api';
import { ApiService } from '../../../services/api';
import { ReviewReportService } from '../../../services/review-report';
import { Review } from '../../../models/review';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-review-section',
  standalone: true,
  imports: [CommonModule, FormsModule, RatingModule, MenuModule, ConfirmDialogModule, Dialog, Button],
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

  reportDialogVisible = false;
  reportNote = '';
  reportingReviewId: number | null = null;
  reportError = '';

  constructor(
    private apiService: ApiService,
    private confirmationService: ConfirmationService,
    private reviewReportService: ReviewReportService,
    private messageService: MessageService,
  ) {}

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
    const items: MenuItem[] = [];

    if (review.user_id === this.currentUserId) {
      items.push({
        label: 'Recensie verwijderen',
        icon: 'pi pi-trash',
        command: () => this.confirmDelete(review),
      });
    } else {
      items.push({
        label: 'Recensie rapporteren',
        icon: 'pi pi-flag',
        command: () => this.openReportDialog(review),
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

  openReportDialog(review: Review): void {
    this.reportingReviewId = review.id!;
    this.reportNote = '';
    this.reportError = '';
    this.reportDialogVisible = true;
  }

  submitReport(): void {
    if (this.reportingReviewId === null) return;
    this.reviewReportService.reportReview(this.reportingReviewId, this.reportNote).subscribe({
      next: () => {
        this.reportDialogVisible = false;
        this.messageService.add({
          severity: 'success',
          summary: 'Verzonden',
          detail: 'Je rapportage is ingediend.',
          life: 3000,
        });
      },
      error: (err) => {
        this.reportError = err.error ?? 'Er is een fout opgetreden.';
      },
    });
  }

  getStars(rating: number): number[] {
    return Array(rating).fill(0);
  }

  getEmptyStars(rating: number): number[] {
    return Array(5 - rating).fill(0);
  }
}
