import { AfterViewInit, Component, ElementRef, EventEmitter, OnInit, Output, ViewChild } from '@angular/core';
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
import { DialogModule } from 'primeng/dialog';
import { BookCopyDetail } from '../../models/bookCopy';

@Component({
  selector: 'app-pick-up-page',
  imports: [NavBarComponent, Skeleton, TableModule, BookCover, DatePipe, Button, FormsModule, DialogModule],
  templateUrl: './pick-up-page.html',
  styleUrl: './pick-up-page.css',
})
export class PickUpPageComponent implements OnInit, AfterViewInit {
  @ViewChild('scanInputRef') scanInputRef!: ElementRef<HTMLInputElement>;
  loans: LoanDTO[] = [];
  loading = true;
  query = '';
  scanInput = '';

  manualDialogVisible = false;
  manualAccessionId = 'LIB-';
  pendingLoanId: number | null = null;

  disambigDialogVisible = false;
  disambigLoans: LoanDTO[] = [];
  pendingCopy: BookCopyDetail | null = null;

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

  ngAfterViewInit(): void {
    this.scanInputRef?.nativeElement?.focus();
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

  openManualDialog(loanId: number): void {
    this.pendingLoanId = loanId;
    this.manualAccessionId = 'LIB-';
    this.manualDialogVisible = true;
  }

  closeManualDialog(): void {
    this.manualDialogVisible = false;
    this.pendingLoanId = null;
    this.manualAccessionId = 'LIB-';
    setTimeout(() => this.scanInputRef?.nativeElement?.focus(), 0);
  }

  confirmManualPickup(): void {
    const id = this.manualAccessionId.trim().toUpperCase();
    if (!id || id === 'LIB-') return;

    this.locationBookService.getCopyByAccessionId(id).subscribe({
      next: (copy) => {
        this.pickupLoan(this.pendingLoanId!, copy.id);
        this.closeManualDialog();
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
        const matchingLoans = this.loans.filter((l) =>
          l.books.some((b) => b.bookId === copy.book_id),
        );
        if (matchingLoans.length === 0) {
          this.messageService.add({
            severity: 'warn',
            summary: 'Niet gevonden',
            detail: `Geen uitlening gevonden voor exemplaar ${id}.`,
            life: 3000,
          });
          return;
        }
        if (matchingLoans.length === 1) {
          this.pickupLoan(matchingLoans[0].id, copy.id);
          return;
        }
        this.pendingCopy = copy;
        this.disambigLoans = matchingLoans;
        this.disambigDialogVisible = true;
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

  selectDisambigLoan(loan: LoanDTO): void {
    this.pickupLoan(loan.id, this.pendingCopy!.id);
    this.closeDisambigDialog();
  }

  closeDisambigDialog(): void {
    this.disambigDialogVisible = false;
    this.disambigLoans = [];
    this.pendingCopy = null;
    setTimeout(() => this.scanInputRef?.nativeElement?.focus(), 0);
  }
}
