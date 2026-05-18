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
        (this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Fout bij laden van de ontleenverzoeken',
          life: 3000,
        }),
          (this.loading = false));
      },
    });
  }

  setState(loanId: number) {
    this.loanService.changeStatus(loanId, LoanStatus.RECEIVED).subscribe({
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
