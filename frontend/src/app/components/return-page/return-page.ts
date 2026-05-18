import { Component } from '@angular/core';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Skeleton } from 'primeng/skeleton';
import { TableModule } from 'primeng/table';
import { BookCover } from '../misc/book-cover/book-cover';
import { Button } from 'primeng/button';
import { DatePipe } from '@angular/common';
import { LoanDTO, LoanStatus } from '../../models/loan';
import { LoanService } from '../../services/loan';
import { MessageService } from 'primeng/api';
import { TooltipModule } from 'primeng/tooltip';

@Component({
  selector: 'app-return-page',
  imports: [
    NavBarComponent,
    FormsModule,
    Skeleton,
    TableModule,
    BookCover,
    Button,
    DatePipe,
    TooltipModule,
  ],
  templateUrl: './return-page.html',
  styleUrl: './return-page.css',
})
export class ReturnPageComponent {
  loans: LoanDTO[] = [];
  loading = true;
  today: Date = new Date();
  query = '';

  get filteredLoans(): LoanDTO[] {
    const trimmedQuery = this.query.trim().toLowerCase();
    if (!trimmedQuery) return this.loans;
    return this.loans.filter((loan) => loan.username.toLowerCase().includes(trimmedQuery));
  }

  onDelete(): void {
    this.query = '';
  }

  isOverdue(endDate: string | Date): boolean {
    const end = new Date(endDate);
    end.setHours(0, 0, 0, 0);
    return end < this.today;
  }

  constructor(
    private loanService: LoanService,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    this.getRecords();
  }

  private getRecords() {
    this.loanService.getByState(LoanStatus.RECEIVED).subscribe({
      next: (data) => {
        this.loans = data;
        this.loading = false;
      },
      error: () => {
        (this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Fout bij laden van de ontleningen',
          life: 3000,
        }),
          (this.loading = false));
      },
    });
  }

  setState(loanId: number) {
    this.loanService.changeStatus(loanId, LoanStatus.RETURNED).subscribe({
      next: () => {
        this.loans = this.loans.filter((l) => l.id !== loanId);
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'Boek teruggebracht',
          life: 3000,
        });
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Probeer opnieuw',
          life: 3000,
        });
        this.loading = false;
      },
    });
  }
}
