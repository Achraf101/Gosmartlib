import { AfterViewInit, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { FormsModule } from '@angular/forms';
import { Skeleton } from 'primeng/skeleton';
import { TableModule } from 'primeng/table';
import { BookCover } from '../misc/book-cover/book-cover';
import { Button } from 'primeng/button';
import { DatePipe } from '@angular/common';
import { LoanDTO, LoanStatus } from '../../models/loan';
import { LoanService } from '../../services/loan';
import { MessageService } from 'primeng/api';
import { TooltipModule } from 'primeng/tooltip';
import { NotificationService } from '../../services/notification';
import { LocationBookService } from '../../services/locationbook';
import { DialogModule } from 'primeng/dialog';
import { BookCopyDetail } from '../../models/bookCopy';

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
    DialogModule,
  ],
  templateUrl: './return-page.html',
  styleUrl: './return-page.css',
})
export class ReturnPageComponent implements OnInit, AfterViewInit {
  @ViewChild('scanInputRef') scanInputRef!: ElementRef<HTMLInputElement>;
  loans: LoanDTO[] = [];
  loading = true;
  today: Date = new Date();
  query = '';
  scanInput = '';

  manualDialogVisible = false;
  manualAccessionId = 'LIB-';
  pendingLoanId: number | null = null;

  disambigDialogVisible = false;
  disambigLoans: LoanDTO[] = [];
  pendingCopy: BookCopyDetail | null = null;

  conditionDialogVisible = false;
  conditionNote = '';
  conditionDamaged = false;
  conditionHasExisting = false;
  private pendingReturn: { loanId: number; copyId: number; fromManual: boolean } | null = null;

  private returnedCopyIds = new Map<number, Set<number>>();

  get pendingLoan(): LoanDTO | undefined {
    return this.loans.find((l) => l.id === this.pendingLoanId);
  }

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
    private notificationService: NotificationService,
    private locationBookService: LocationBookService,
  ) {}

  ngOnInit(): void {
    this.getRecords();
  }

  ngAfterViewInit(): void {
    this.scanInputRef?.nativeElement?.focus();
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

  private processScanReturn(
    loanId: number,
    copyId: number,
    note: string,
    damaged: boolean,
    fromManual: boolean,
  ): void {
    const loanSet = this.returnedCopyIds.get(loanId) ?? new Set<number>();
    if (loanSet.has(copyId)) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Al gescand',
        detail: 'Dit exemplaar is al teruggebracht voor deze uitlening.',
        life: 3000,
      });
      return;
    }
    loanSet.add(copyId);
    this.returnedCopyIds.set(loanId, loanSet);

    this.loanService.scanReturn(loanId, copyId, note, damaged).subscribe({
      next: (updatedLoan) => {
        if (updatedLoan.status === LoanStatus.RETURNED) {
          this.loans = this.loans.filter((l) => l.id !== updatedLoan.id);
          this.returnedCopyIds.delete(loanId);
          this.messageService.add({
            severity: 'success',
            summary: 'Succes',
            detail: 'Boek teruggebracht',
            life: 3000,
          });
        } else {
          const idx = this.loans.findIndex((l) => l.id === updatedLoan.id);
          if (idx !== -1) this.loans[idx] = updatedLoan;
          const book = updatedLoan.books[0];
          this.messageService.add({
            severity: 'info',
            summary: 'Exemplaar gescand',
            detail: `${book.returnedAmount}/${book.receivedAmount} exemplaren teruggebracht`,
            life: 3000,
          });
          if (fromManual) {
            this.pendingLoanId = loanId;
            this.manualAccessionId = 'LIB-';
            this.manualDialogVisible = true;
          } else {
            setTimeout(() => this.scanInputRef?.nativeElement?.focus(), 0);
          }
        }
      },
      error: () => {
        loanSet.delete(copyId);
        if (fromManual) {
          this.pendingLoanId = loanId;
          this.manualAccessionId = 'LIB-';
          this.manualDialogVisible = true;
        } else {
          setTimeout(() => this.scanInputRef?.nativeElement?.focus(), 0);
        }
      },
    });
  }

  openConditionDialog(
    loanId: number,
    copyId: number,
    fromManual: boolean,
    existingNote = '',
    existingDamaged = false,
  ): void {
    this.pendingReturn = { loanId, copyId, fromManual };
    this.conditionNote = existingNote;
    this.conditionDamaged = existingDamaged;
    this.conditionHasExisting = !!(existingNote || existingDamaged);
    this.conditionDialogVisible = true;
  }

  confirmConditionDialog(): void {
    if (!this.pendingReturn) return;
    const { loanId, copyId, fromManual } = this.pendingReturn;
    this.pendingReturn = null;
    this.conditionDialogVisible = false;
    this.processScanReturn(loanId, copyId, this.conditionNote, this.conditionDamaged, fromManual);
  }

  closeConditionDialog(): void {
    if (!this.pendingReturn) return;
    const { fromManual, loanId } = this.pendingReturn;
    this.conditionDialogVisible = false;
    this.pendingReturn = null;
    if (fromManual) {
      this.pendingLoanId = loanId;
      this.manualAccessionId = 'LIB-';
      this.manualDialogVisible = true;
    } else {
      setTimeout(() => this.scanInputRef?.nativeElement?.focus(), 0);
    }
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
    if (!this.conditionDialogVisible) {
      setTimeout(() => this.scanInputRef?.nativeElement?.focus(), 0);
    }
  }

  confirmManualReturn(): void {
    const id = this.manualAccessionId.trim().toUpperCase();
    if (!id || id === 'LIB-') return;

    this.locationBookService.getCopyByAccessionId(id).subscribe({
      next: (copy) => {
        const loanId = this.pendingLoanId!;
        this.openConditionDialog(loanId, copy.id, true, copy.note ?? '', copy.status === 'DAMAGED');
        this.manualDialogVisible = false;
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
          l.books.some((b) => b.bookId === copy.book_id && b.returnedAmount < b.receivedAmount),
        );
        if (matchingLoans.length === 0) {
          this.messageService.add({
            severity: 'warn',
            summary: 'Niet gevonden',
            detail: `Geen actieve uitlening gevonden voor exemplaar ${id}.`,
            life: 3000,
          });
          return;
        }
        if (matchingLoans.length === 1) {
          this.openConditionDialog(
            matchingLoans[0].id,
            copy.id,
            false,
            copy.note ?? '',
            copy.status === 'DAMAGED',
          );
          return;
        }
        this.pendingCopy = copy;
        this.disambigLoans = matchingLoans;
        this.disambigDialogVisible = true;
      },
    });
  }

  selectDisambigLoan(loan: LoanDTO): void {
    const copy = this.pendingCopy!;
    this.openConditionDialog(loan.id, copy.id, false, copy.note ?? '', copy.status === 'DAMAGED');
    this.closeDisambigDialog();
  }

  closeDisambigDialog(): void {
    this.disambigDialogVisible = false;
    this.disambigLoans = [];
    this.pendingCopy = null;
    if (!this.conditionDialogVisible) {
      setTimeout(() => this.scanInputRef?.nativeElement?.focus(), 0);
    }
  }

  // notify
  notify(loanId: number) {
    this.notificationService.notify(loanId).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          detail: `Bericht verzonden`,
          life: 3000,
        });
      },
    });
  }
}
