import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
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
import { TooltipModule } from 'primeng/tooltip';

@Component({
  selector: 'app-pick-up-page',
  imports: [
    NavBarComponent,
    Skeleton,
    TableModule,
    BookCover,
    DatePipe,
    Button,
    FormsModule,
    TooltipModule,
  ],
  templateUrl: './pick-up-page.html',
  styleUrl: './pick-up-page.css',
})
export class PickUpPageComponent implements OnInit {
  loans: LoanDTO[] = [];
  loading = true;
  query = '';

  // Barcode scanner state
  barcodeInput = '';
  barcodeScanning = false;
  scannedLoanId: number | null = null;

  @ViewChild('barcodeField') barcodeField!: ElementRef<HTMLInputElement>;

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
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Fout bij laden van de ontleenverzoeken',
          life: 3000,
        });
        this.loading = false;
      },
    });
  }

  setState(loanId: number) {
    this.loanService.changeStatus(loanId, LoanStatus.RECEIVED).subscribe({
      next: () => {
        this.loans = this.loans.filter((l) => l.id !== loanId);
        this.scannedLoanId = null;
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

  /** Called when the librarian scans (or types + presses Enter) a barcode. */
  onBarcodeScan(): void {
    const barcode = this.barcodeInput.trim();
    if (!barcode) return;

    this.barcodeScanning = true;
    this.scannedLoanId = null;

    this.loanService.findByBarcode(barcode, LoanStatus.ACCEPTED).subscribe({
      next: (loan) => {
        this.barcodeScanning = false;
        this.barcodeInput = '';

        // Check if the loan is already in our list (it should be)
        const existing = this.loans.find((l) => l.id === loan.id);
        if (existing) {
          this.scannedLoanId = loan.id;
          this.messageService.add({
            severity: 'info',
            summary: 'Boek gevonden',
            detail: `"${loan.books[0]?.bookTitle ?? 'boek'}" voor ${loan.username} — klik Opgehaald om te bevestigen`,
            life: 5000,
          });
        } else {
          // Loan is valid but wasn't in our current list — reload
          this.loans = [loan, ...this.loans];
          this.scannedLoanId = loan.id;
          this.messageService.add({
            severity: 'info',
            summary: 'Boek gevonden',
            detail: `"${loan.books[0]?.bookTitle ?? 'boek'}" voor ${loan.username}`,
            life: 5000,
          });
        }
      },
      error: (err) => {
        this.barcodeScanning = false;
        this.barcodeInput = '';
        const detail =
          err.status === 404
            ? 'Geen openstaand ophaalverzoek gevonden voor dit boek'
            : 'Fout bij het opzoeken van de barcode';
        this.messageService.add({
          severity: 'warn',
          summary: 'Niet gevonden',
          detail,
          life: 4000,
        });
      },
    });
  }

  clearBarcodeScan(): void {
    this.barcodeInput = '';
    this.scannedLoanId = null;
    this.barcodeField?.nativeElement.focus();
  }

  isHighlighted(loanId: number): boolean {
    return this.scannedLoanId === loanId;
  }
}
