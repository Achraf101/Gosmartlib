import { Component, inject, signal } from '@angular/core';
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
import { BadgeModule } from 'primeng/badge';
import { IftaLabel } from 'primeng/iftalabel';
import { InputNumber } from 'primeng/inputnumber';
import { LoanCartService } from '../../services/loan-cart';

@Component({
  selector: 'app-loan-cart',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    DialogModule,
    DatePickerModule,
    BadgeModule,
    IftaLabel,
    InputNumber,
    FormsModule,
  ],
  templateUrl: './loan-cart.html',
  styleUrl: './loan-cart.css',
})
export class LoanCartComponent {
  constructor(
    readonly cartService: LoanCartService,
    private readonly loanService: LoanService,
    private readonly messageService: MessageService,
  ) {}

  visible = false;
  today = new Date();
  endDate = new Date();

  // TODO: vervang met werkelijke ingelogde gebruiker zodra auth klaar is
  private readonly userId = 1;

  checkoutForm = new FormGroup({
    start: new FormControl<Date | null>(null, Validators.required),
    end: new FormControl<Date | null>(null, Validators.required),
  });

  onStartDateSelect(date: Date) {
    const end = new Date(date);
    end.setDate(end.getDate() + 14);
    this.checkoutForm.controls.end.setValue(end);
  }

  updateAmount(bookId: number, amount: number): void {
    if (amount < 1) return;
    this.cartService.updateAmount(bookId, amount);
  }

  submitLoan(): void {
    if (this.checkoutForm.invalid || this.cartService.isEmpty()) return;

    const { start, end } = this.checkoutForm.value;

    const loan: CreateLoanDTO = {
      userId: this.userId,
      extended: 0,
      start: start!,
      end: end!,
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
}
