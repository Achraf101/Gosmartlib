import { Component, inject } from '@angular/core';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { CommonModule } from '@angular/common';
import { LoanService } from '../../services/loan';
import { CreateLoanDTO, LoanStatus } from '../../models/loan';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { DatePickerModule } from 'primeng/datepicker';
import { IftaLabel } from 'primeng/iftalabel';
import { InputNumber } from 'primeng/inputnumber';
import { LoanCartService } from '../../services/loan-cart';
import { BookCardComponent } from '../misc/book-card/book-card';
import { Campus } from '../../models/campus';
import { CampusService } from '../../services/campus';
import { AuthService } from '../../services/auth';
import { CampusBookService } from '../../services/campusbook';
import { CampusBook } from '../../models/CampusBook';
import { TableModule } from 'primeng/table';
import { Message } from 'primeng/message';

@Component({
  selector: 'app-loan-cart',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    DialogModule,
    DatePickerModule,
    IftaLabel,
    InputNumber,
    FormsModule,
    BookCardComponent,
    TableModule,
    Message,
  ],
  templateUrl: './loan-cart.html',
  styleUrl: './loan-cart.css',
})
export class LoanCartComponent {
  constructor(
    private readonly loanService: LoanService,
    private readonly messageService: MessageService,
    private readonly campusService: CampusService,
    private readonly campusBookService: CampusBookService,
    private readonly authService: AuthService,
  ) {}

  readonly cartService = inject(LoanCartService);

  visible = false;
  today = new Date();
  items = this.cartService.items();
  maxAmounts: Map<number, number> = new Map();

  campus?: Campus;

  private get userId(): number {
    return this.authService.currentUser?.userId ?? 0;
  }

  private get campusId(): number {
    return this.authService.currentUser?.campusId ?? 0;
  }

  ngOnInit(): void {
    this.campusService.getById(this.campusId).subscribe({
      next: (campus) => {
        ((this.campus = campus), this.cartService.setBorrowLimit(campus.borrowLimit));
      },
    });
  }

  loadMaxAmounts(): void {
    for (const item of this.cartService.items()) {
      this.campusBookService.getCampusBook(this.campusId, item.bookId).subscribe({
        next: (campusBook: CampusBook) => {
          this.maxAmounts.set(item.bookId, campusBook.current_amount);
        },
      });
    }
  }

  getMaxAmount(bookId: number): number {
    return this.maxAmounts.get(bookId) ?? 1;
  }

  checkoutForm = new FormGroup({
    start: new FormControl<Date | null>(null, Validators.required),
  });

  calculatedEnd: Date | null = null;

  onStartDateSelect(date: Date) {
    const end = new Date(date);
    console.log('campus at select time:', this.campus);
    end.setDate(end.getDate() + (this.campus?.borrowPeriod ?? 14));
    console.log('end result:', end);
    this.calculatedEnd = end;
  }

  updateAmount(bookId: number, amount: number): void {
    console.log('updateAmount aangeroepen', bookId, amount);
    if (amount < 1) return;

    const max = this.getMaxAmount(bookId);

    if (amount > max) {
      this.cartService.updateAmount(bookId, max);
      return;
    }
    this.cartService.updateAmount(bookId, amount);
  }

  submitLoan(): void {
    if (this.checkoutForm.invalid || this.cartService.isEmpty() || !this.calculatedEnd) return;

    if (this.campus && this.cartService.items().length > this.campus.borrowLimit) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Limiet overschreden',
        detail: `U mag maximaal ${this.campus.borrowLimit} verschillende boeken per ontlening aanvragen.`,
        life: 4000,
      });
      return;
    }

    const loan: CreateLoanDTO = {
      userId: this.userId,
      campusId: this.campusId,
      extended: 0,
      start: this.formatDate(this.checkoutForm.value.start!),
      end: this.formatDate(this.calculatedEnd ?? new Date()),
      note: '',
      status: LoanStatus.REQUESTED,
      closed: false,
      books: this.cartService.items(),
    };

    this.loanService.createLoan(loan).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'Ontleenverzoek succesvol verzonden!',
          life: 3000,
        });
        this.cartService.clear();
        this.checkoutForm.reset();
        this.visible = false;
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Er is iets misgegaan, probeer opnieuw.',
          life: 3000,
        });
        console.error(err);
      },
    });
  }
  private formatDate(d: Date): string {
    return d.toLocaleDateString('en-CA');
  }
}
