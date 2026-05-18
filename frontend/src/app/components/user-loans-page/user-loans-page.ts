import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TabsModule } from 'primeng/tabs';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { CardModule } from 'primeng/card';
import { SkeletonModule } from 'primeng/skeleton';
import { TooltipModule } from 'primeng/tooltip';
import { LoanDTO, LoanStatus } from '../../models/loan';
import { LoanService } from '../../services/loan';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { BookCard } from '../../models/book';
import { BookCover } from '../misc/book-cover/book-cover';
import { Dialog } from 'primeng/dialog';

@Component({
  selector: 'app-user-loans-page',
  standalone: true,
  imports: [
    CommonModule,
    TabsModule,
    TableModule,
    TagModule,
    CardModule,
    SkeletonModule,
    TooltipModule,
    NavBarComponent,
    Button,
    BookCover,
    Dialog,
  ],
  templateUrl: './user-loans-page.html',
  styleUrl: './user-loans-page.css',
})
export class UserLoansPageComponent implements OnInit {
  loans: LoanDTO[] = [];
  loading = true;
  today: Date = new Date();
  bookWithDetialList: BookCard[] = [];
  noteDialogVisible = false;
  selectedLoan: any = null;

  extendDialogVisible = false;
  loanToExtend: LoanDTO | null = null;

  constructor(
    private loanService: LoanService,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    this.getRecords();
  }

  private getRecords() {
    this.loanService.getByUser().subscribe({
      next: (data) => {
        this.loans = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  get activeLoans(): LoanDTO[] {
  return this.loans
    .filter((l) => l.status === LoanStatus.RECEIVED || l.status === LoanStatus.RETURNED)
    .sort((a, b) => {
      if (a.status === LoanStatus.RECEIVED && b.status !== LoanStatus.RECEIVED) return -1;
      if (a.status !== LoanStatus.RECEIVED && b.status === LoanStatus.RECEIVED) return 1;
      return 0;
    });
}

  get loanRequests(): LoanDTO[] {
    return this.loans.filter(
      (l) =>
        l.status === LoanStatus.REQUESTED ||
        l.status === LoanStatus.ACCEPTED ||
        l.status === LoanStatus.DECLINED,
    );
  }

  get extendedEndDate(): Date | null {
    if (!this.loanToExtend) return null;
    const date = new Date(this.loanToExtend.end);
    date.setDate(date.getDate() + 14);
    return date;
  }

  isOverdue(endDate: string | Date): boolean {
    const end = new Date(endDate);
    end.setHours(0, 0, 0, 0);
    return end < this.today;
  }

  statusSeverity(
    status: LoanStatus,
  ): 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast' {
    switch (status) {
      case LoanStatus.RETURNED:
        return 'success';
      case LoanStatus.RECEIVED:
        return 'info';
      case LoanStatus.ACCEPTED:
        return 'success';
      case LoanStatus.REQUESTED:
        return 'warn';
      case LoanStatus.DECLINED:
        return 'danger';
      default:
        return 'secondary';
    }
  }

  statusLabel(status: LoanStatus): string {
    switch (status) {
      case LoanStatus.RETURNED:
        return 'Teruggebracht';
      case LoanStatus.RECEIVED:
        return 'Actief';
      case LoanStatus.ACCEPTED:
        return 'Geaccepteerd';
      case LoanStatus.REQUESTED:
        return 'In behandeling';
      case LoanStatus.DECLINED:
        return 'Geweigerd';
      default:
        return status;
    }
  }

  totalBooks(loan: LoanDTO): number {
    return loan.books?.reduce((sum, b) => sum + (b.requestedAmount ?? 0), 0) ?? 0;
  }

  openExtendDialog(loan: LoanDTO): void {
    this.loanToExtend = loan;
    this.extendDialogVisible = true;
  }

  confirmExtend(): void {
    if (!this.loanToExtend) return;
    this.extendDialogVisible = false;
    this.extendLoan(this.loanToExtend.id);
    this.loanToExtend = null;
  }

  deleteLoan(loanId: number) {
    this.loanService.delete(loanId).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'Uitleenverzoek succesvol verwijderd',
          life: 3000,
        });
        this.getRecords();
      },
      error: () =>
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Uitleenverzoek niet succesvol verwijderd',
          life: 3000,
        }),
    });
  }

  setStatus(loanId: number) {
    this.loanService.changeStatus(loanId, LoanStatus.RECEIVED).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'Test geslaagd',
          life: 3000,
        });
        this.getRecords();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Test gefaald',
          life: 3000,
        });
        this.loading = false;
      },
    });
  }

  setStatus2(loanId: number) {
    this.loanService.changeStatus(loanId, LoanStatus.RETURNED).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'Test geslaagd',
          life: 3000,
        });
        this.getRecords();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Test gefaald',
          life: 3000,
        });
        this.loading = false;
      },
    });
  }

  extendLoan(loanId: number) {
    this.loanService.extend(loanId).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'Uitlening succesvol verlengd!',
          life: 3000,
        });
        this.getRecords();
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: err.error ?? 'Verlengen mislukt, probeer opnieuw.',
          life: 3000,
        });
      },
    });
  }
}