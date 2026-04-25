import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { BookService } from '../../services/book-service';
import { BookCard, BookDetail } from '../../models/book';
import { ImageModule } from 'primeng/image';
import { RatingModule } from 'primeng/rating';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { MessageService } from 'primeng/api';
import { AccordionModule } from 'primeng/accordion';
import { BookmarkedService } from '../../services/bookmarked-service';
import { ButtonModule } from 'primeng/button';
import { Message } from 'primeng/message';
import { IftaLabel } from 'primeng/iftalabel';
import { DialogModule } from 'primeng/dialog';
import { DatePickerModule } from 'primeng/datepicker';
import { InputNumber } from 'primeng/inputnumber';
import { CreateLoanDTO, LoanStatus } from '../../models/loan';
import { LoanService } from '../../services/loan';
import { CreateLoanBookDTO } from '../../models/loanBook';
import { DatePipe } from '@angular/common';
import { LoanCartService } from '../../services/loan-cart';

@Component({
  selector: 'app-book-detail-page',
  imports: [
    ImageModule,
    RatingModule,
    FormsModule,
    NavBarComponent,
    RouterLink,
    AccordionModule,
    ButtonModule,
    Message,
    IftaLabel,
    DialogModule,
    ReactiveFormsModule,
    DatePickerModule,
    InputNumber,
    DatePipe,
  ],
  templateUrl: './book-detail-page.html',
  styleUrl: './book-detail-page.css',
})
export class BookDetailPage implements OnInit {
  book?: BookDetail;
  bookId!: number;
  error = '';
  ratingValue = 0;
  ratingValueStars = 0;
  relatedBooks?: BookCard[];
  isBookmarked = false;
  genresString = '';
  loanFormVisible = false;
  cartDialogVisible = false;
  today = new Date();
  endDate = new Date();

  // TODO: replace with actual logged in user id once auth is done
  userId = 1;

  readonly placeholder = '/assets/no-cover.svg';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly bookService: BookService,
    private readonly messageService: MessageService,
    private readonly bookmarkedService: BookmarkedService,
    private readonly loanService: LoanService,
    private readonly loanCartService: LoanCartService,
  ) {}

  loanForm = new FormGroup({
    start: new FormControl<Date | null>(null, Validators.required),
    end: new FormControl<Date | null>(null, Validators.required),
    requestedAmount: new FormControl<number | null>(null, [
      Validators.required,
      Validators.min(0),
      Validators.max(100),
      Validators.pattern('^[0-9]*$'),
    ]),
  });

  cartForm = new FormGroup({
    requestedAmount: new FormControl<number | null>(null, [
      Validators.required,
      Validators.min(1),
      Validators.max(100),
      Validators.pattern('^[0-9]*$'),
    ]),
  });

  onStartDateSelect(date: Date) {
    const end = new Date(date);
    end.setDate(end.getDate() + 14);
    this.loanForm.controls.end.setValue(end);
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      this.bookId = Number(params.get('id'));
      this.loadBook();
    });
    this.bookmarkedService.isBookmarked(this.userId, this.bookId).subscribe({
      next: (result) => (this.isBookmarked = result),
    });

    if (!this.bookId) return;
  }

  public toggleFavorite() {
    this.isBookmarked = !this.isBookmarked;
    this.bookmarkedService.toggleBookmarked(this.userId, this.bookId).subscribe({
      next: (isAdded) => {
        this.isBookmarked = isAdded;
      },
    });
  }

  scrollTop() {
    window.scrollTo({ top: 0, left: 0, behavior: 'smooth' });
  }

  public loadBook() {
    this.bookService.getById(this.bookId).subscribe({
      next: (book) => {
        this.book = book;
        this.ratingValue =
          Math.round((this.book?.rating_total! / this.book?.rating_count!) * 10) / 10;
        this.ratingValueStars = Math.round(this.ratingValue);

        this.genresString = (this.book?.genres || []).map((i) => i.name).join(', ');

        this.bookmarkedService.isBookmarked(this.userId, this.bookId).subscribe({
          next: (result) => (this.isBookmarked = result),
        });
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Boek niet gevonden.',
          life: 3000,
        });
      },
    });

    this.bookService.getRelated(this.bookId).subscribe({
      next: (relatedBooks) => (this.relatedBooks = relatedBooks),
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Probleem met het zoeken van gelijkaardige boeken.',
          life: 3750,
        });
      },
    });
  }

  createLoan(): void {
    const rawValue = this.loanForm.value;

    const book: CreateLoanBookDTO = {
      bookId: this.bookId,
      requestedAmount: rawValue.requestedAmount ?? 1,
      receivedAmount: 0,
      returnedAmount: 0,
    };

    const loan: CreateLoanDTO = {
      userId: this.userId,
      // campusId: 1,
      extended: 0,
      start: rawValue.start ?? new Date(),
      end: rawValue.end ?? new Date(),
      note: '',
      status: LoanStatus.REQUESTED,
      closed: false,
      books: [book],
    };
    console.log('Sending to API:', loan);

    this.loanService.createLoan(loan).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'Ontleenverzoek succesvol verzonden!',
          life: 3000,
        });

        this.loanForm.reset();
        this.loanFormVisible = false;
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Er is iets misgegaan bij het versturen van dit ontleenverzoek, probeer opnieuw.',
          life: 3000,
        });

        console.error(err);
      },
    });
  }
  cancelLoan(): void {
    this.loanFormVisible = false;
    this.loanForm.reset();
  }
  addToCart(): void {
    if (this.cartForm.invalid || !this.book) return;

    const book: CreateLoanBookDTO = {
      bookId: this.bookId,
      requestedAmount: this.cartForm.value.requestedAmount ?? 1,
      receivedAmount: 0,
      returnedAmount: 0,
    };
    const added = this.loanCartService.addBook(book);
    if (!added) return;

    this.messageService.add({
      severity: 'success',
      summary: 'Toegevoegd',
      detail: `"${this.book.title}" is toegevoegd aan uw ontleenlijst.`,
      life: 3000,
    });

    this.cartForm.reset();
    this.cartDialogVisible = false;
  }

  cancelCart(): void {
    this.cartDialogVisible = false;
    this.cartForm.reset();
  }
}
