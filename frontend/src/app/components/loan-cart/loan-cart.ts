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
import { Location } from '../../models/location';
import { LocationService } from '../../services/location';
import { AuthService } from '../../services/auth';
import { LocationBookService } from '../../services/locationbook';
import { LocationBook } from '../../models/locationBook';
import { TableModule } from 'primeng/table';
import { Message } from 'primeng/message';
import { SchoolService } from '../../services/school';
import { School } from '../../models/school';
import { TooltipModule } from 'primeng/tooltip';
import { SelectModule } from 'primeng/select';

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
    TooltipModule,
    SelectModule,
  ],
  templateUrl: './loan-cart.html',
  styleUrl: './loan-cart.css',
})
export class LoanCartComponent {
  constructor(
    private readonly loanService: LoanService,
    private readonly messageService: MessageService,
    private readonly locationBookService: LocationBookService,
    public authService: AuthService,
    private readonly schoolService: SchoolService,
  ) {}

  readonly cartService = inject(LoanCartService);

  visible = false;
  today = new Date();
  items = this.cartService.items();
  maxAmounts: Map<number, number> = new Map();
  locations: Location[] = [];
  selectedLocationId: number | null = null;

  school?: School;
  protected locationId: number | null = null;

  private get userId(): number {
    return this.authService.currentUser?.userId ?? 0;
  }

  private get schoolId(): number {
    return this.authService.currentUser?.schoolId ?? 0;
  }

  ngOnInit(): void {
    this.schoolService.getById(this.schoolId).subscribe({
      next: (school) => {
        this.school = school;
        this.cartService.setBorrowLimit(school.borrowLimit);
        if (school.locations?.length === 1) {
          this.locationId = school.locations[0].id;
        } else {
          this.locations = school.locations ?? [];
          this.selectedLocationId = this.locationId;
        }

        console.log('locationId na init:', this.locationId);
        console.log('locations na init:', this.locations);
        this.loadMaxAmounts();
      },
    });
    this.checkoutForm.controls.start.valueChanges.subscribe((date) => {
      if (!date || !this.school) return;
      const end = new Date(date);
      end.setDate(end.getDate() + this.school.borrowPeriod);
      this.calculatedEnd = end;
    });
  }

  onLocationChange(locationId: number): void {
    this.selectedLocationId = locationId;
    this.loadMaxAmounts();
  }

  loadMaxAmounts(): void {
    const effectiveLocationId = this.locationId ?? this.selectedLocationId;
    if (!effectiveLocationId) return;
    for (const item of this.cartService.items()) {
      this.locationBookService.getLocationBook(effectiveLocationId, item.bookId).subscribe({
        next: (locationBook: LocationBook) => {
          this.maxAmounts.set(item.bookId, locationBook.current_amount);

          if (item.requestedAmount > locationBook.current_amount) {
            this.cartService.updateAmount(item.bookId, locationBook.current_amount);
          }
        },
        error: () => {
          this.maxAmounts.set(item.bookId, 0);
          this.cartService.updateAmount(item.bookId, 0);
          this.messageService.add({
            severity: 'warn',
            summary: 'Niet beschikbaar',
            detail: `Een boek in je lijst is niet beschikbaar op deze locatie.`,
            life: 3000,
          });
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
    const effectiveLocationId = this.locationId ?? this.selectedLocationId;

    if (!effectiveLocationId) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Geen locatie',
        detail: 'Selecteer een locatie voor je ontlening.',
        life: 3000,
      });
      return;
    }

    if (this.checkoutForm.invalid || this.cartService.isEmpty() || !this.calculatedEnd) return;

    if (this.school && this.cartService.items().length > this.school.borrowLimit) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Limiet overschreden',
        detail: `Je mag maximaal ${this.school.borrowLimit} verschillende boeken per ontlening aanvragen.`,
        life: 4000,
      });
      return;
    }

    const loan: CreateLoanDTO = {
      userId: this.userId,
      locationId: effectiveLocationId,
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
          detail: `Ontleenverzoek succesvol verzonden!`,
          life: 3000,
        });
        this.cartService.clear();
        this.checkoutForm.reset();
        this.visible = false;
      },
    });
  }
  private formatDate(d: Date): string {
    return d.toLocaleDateString('en-CA');
  }
}
