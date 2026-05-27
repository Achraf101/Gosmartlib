import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { ReviewReportService } from '../../services/review-report';
import { ReviewReport } from '../../models/review-report';
import { MessageService } from 'primeng/api';
import { Card } from 'primeng/card';
import { Button } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { Dialog } from 'primeng/dialog';

@Component({
  selector: 'app-review-moderation-page',
  standalone: true,
  imports: [CommonModule, RouterModule, NavBarComponent, Card, Button, TableModule, Dialog],
  templateUrl: './review-moderation-page.html',
  styleUrl: './review-moderation-page.css',
})
export class ReviewModerationPageComponent implements OnInit {
  reports: ReviewReport[] = [];
  loading = false;
  selectedReport: ReviewReport | null = null;
  detailDialogVisible = false;

  constructor(
    private reviewReportService: ReviewReportService,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    this.loadReports();
  }

  loadReports(): void {
    this.loading = true;
    this.reviewReportService.getPendingReports().subscribe({
      next: (data) => {
        this.reports = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  openDetail(report: ReviewReport): void {
    this.selectedReport = report;
    this.detailDialogVisible = true;
  }

  deleteReview(report: ReviewReport): void {
    this.reviewReportService.acceptReport(report.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Verwijderd',
          detail: 'Recensie verwijderd.',
          life: 3000,
        });
        this.detailDialogVisible = false;
        this.loadReports();
      },
    });
  }

  rejectReport(report: ReviewReport): void {
    this.reviewReportService.rejectReport(report.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'info',
          summary: 'Geweigerd',
          detail: 'Rapportage geweigerd.',
          life: 3000,
        });
        this.detailDialogVisible = false;
        this.loadReports();
      },
    });
  }

  getStars(rating: number): number[] {
    return Array(Math.round(rating)).fill(0);
  }

  getEmptyStars(rating: number): number[] {
    return Array(5 - Math.round(rating)).fill(0);
  }
}
