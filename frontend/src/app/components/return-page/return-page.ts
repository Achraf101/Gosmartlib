import { Component, OnInit } from '@angular/core';
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
import { LocationBookService } from '../../services/locationbook';

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
export class ReturnPageComponent implements OnInit {
  loans: LoanDTO[] = [];
  loading = true;
  today: Date = new Date();
  query = '';
  scanInput = '';

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
    private locationBookService: LocationBookService,
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
        this.loading = false;
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
        this.loading = false;
      },
    });
  }

  scanAndProcess(): void {
    const id = this.scanInput.trim().toUpperCase();
    this.scanInput = '';
    if (!id) return;

    this.locationBookService.getCopyByAccessionId(id).subscribe({
      next: (copy) => {
        // Find loan where this exact copy was assigned at pickup
        const exactLoan = this.loans.find((l) =>
          l.books.some((b) => b.bookCopyId === copy.id),
        );
        if (exactLoan) {
          this.setState(exactLoan.id);
          return;
        }

        // Copy is from the right book but not the assigned copy
        const wrongCopyLoan = this.loans.find((l) =>
          l.books.some((b) => b.bookId === copy.book_id),
        );
        if (wrongCopyLoan) {
          this.messageService.add({
            severity: 'error',
            summary: 'Verkeerd exemplaar',
            detail: `Dit exemplaar (${id}) is niet het exemplaar dat werd ontleend.`,
            life: 5000,
          });
          return;
        }

        this.messageService.add({
          severity: 'warn',
          summary: 'Niet gevonden',
          detail: `Geen actieve uitlening gevonden voor exemplaar ${id}.`,
          life: 3000,
        });
      },
      error: () => {
        this.messageService.add({
          severity: 'warn',
          summary: 'Niet gevonden',
          detail: `Exemplaar ${id} niet gevonden.`,
          life: 3000,
        });
      },
    });
  }
}
