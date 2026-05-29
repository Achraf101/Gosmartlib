import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { LoanService } from '../../services/loan';
import { MessageService } from 'primeng/api';
import { LoanDTO, LoanStatus } from '../../models/loan';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { Skeleton } from 'primeng/skeleton';
import { TableModule } from 'primeng/table';
import { BookCover } from '../misc/book-cover/book-cover';
import { DatePipe } from '@angular/common';
import { Button } from 'primeng/button';
import { FormsModule } from '@angular/forms';
import { LocationBookService } from '../../services/locationbook';

@Component({
  selector: 'app-pick-up-page',
  imports: [NavBarComponent, Skeleton, TableModule, BookCover, DatePipe, Button, FormsModule],
  templateUrl: './pick-up-page.html',
  styleUrl: './pick-up-page.css',
})
export class PickUpPageComponent implements OnInit {
  loans: LoanDTO[] = [];
  loading = true;
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

  constructor(
    private loanService: LoanService,
    private messageService: MessageService,
    private locationBookService: LocationBookService,
  ) {}

  ngOnInit(): void {
    this.getRecords();
  }

  private getRecords() {
    this.loanService.getByState(LoanStatus.ACCEPTED).subscribe({
      next: (data) => {
        this.loans = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  pickupLoan(loanId: number, bookCopyId?: number): void {
    this.loanService.pickupLoan(loanId, bookCopyId).subscribe({
      next: () => {
        this.loans = this.loans.filter((l) => l.id !== loanId);
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'Boek opgehaald',
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
        const loan = this.loans.find((l) => l.books.some((b) => b.bookId === copy.book_id));
        if (!loan) {
          this.messageService.add({
            severity: 'warn',
            summary: 'Niet gevonden',
            detail: `Geen uitlening gevonden voor exemplaar ${id}.`,
            life: 3000,
          });
          return;
        }
        this.pickupLoan(loan.id, copy.id);
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
