import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
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
import { BookCardComponent } from '../misc/book-card/book-card';
import { BookmarkedService } from '../../services/bookmarked-service';
import { ButtonModule } from 'primeng/button';
import { IftaLabel } from 'primeng/iftalabel';
import { DialogModule } from 'primeng/dialog';
import { DatePickerModule } from 'primeng/datepicker';
import { InputNumber } from 'primeng/inputnumber';
import { CreateLoanDTO, LoanStatus } from '../../models/loan';
import { LoanService } from '../../services/loan';
import { CreateLoanBookDTO } from '../../models/loanBook';
import { DatePipe } from '@angular/common';
import { LoanCartService } from '../../services/loan-cart';
import { CarouselModule } from 'primeng/carousel';
import { ProgressSpinner } from 'primeng/progressspinner';
import { DelayedLoader } from '../../utils/delayed-loader';
import { BookList } from '../../models/book-list';
import { BookListService } from '../../services/book-list';
import { Button } from 'primeng/button';
import { CartBook } from '../../models/cartBook';
import { Campus } from '../../models/campus';
import { CampusService } from '../../services/campus';
import { CampusBook } from '../../models/CampusBook';
import { CampusBookService } from '../../services/campusbook';
import { Message } from 'primeng/message';
import { ReviewSectionComponent } from '../misc/review-section/review-section';
import { BookService } from '../../services/book';
import { Material } from '../../models/material';
import { MaterialService } from '../../services/material';
import { UploadService } from '../../services/upload';
import { FileSelectEvent, FileUploadModule } from 'primeng/fileupload';
import { MaterialComponent } from '../material/material';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-book-detail-page',
  imports: [
    ImageModule,
    RatingModule,
    FormsModule,
    NavBarComponent,
    AccordionModule,
    ButtonModule,
    IftaLabel,
    DialogModule,
    ReactiveFormsModule,
    DatePickerModule,
    InputNumber,
    DatePipe,
    BookCardComponent,
    CarouselModule,
    ProgressSpinner,
    Button,
    Message,
    ReviewSectionComponent,
    FileUploadModule,
    MaterialComponent,
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
  materials?: Material[];
  materialFetched = false;
  isBookmarked = false;
  genresString = '';
  themesString = '';
  loanFormVisible = false;
  cartDialogVisible = false;
  today = new Date();
  endDate = new Date();
  loading = new DelayedLoader();
  lists: BookList[] = [];
  showDropdown = false;
  campus?: Campus;
  campusBook?: CampusBook;

  userId = 1;
  campusId = 1;

  readonly placeholder = '/assets/no-cover.svg';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly bookService: BookService,
    private readonly messageService: MessageService,
    private readonly bookmarkedService: BookmarkedService,
    private readonly bookListService: BookListService,
    private readonly loanService: LoanService,
    private readonly loanCartService: LoanCartService,
    private readonly campusService: CampusService,
    private readonly campusBookService: CampusBookService,
    private readonly materialService: MaterialService,
    private readonly uploadService: UploadService,
    public auth: AuthService,
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      this.bookId = Number(params.get('id'));
      this.loadBook();
      this.showDropdown = false;
      this.bookListService.getListsWithoutBook(this.bookId).subscribe((lists) => {
        this.lists = lists;
      });
      this.loadCampusData();
    });
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
    this.loading.start();
    this.error = '';
    this.book = undefined;

    this.bookService.getById(this.bookId).subscribe({
      next: (book) => {
        this.book = book;
        this.ratingValue = this.book?.rating_count! > 0 ? this.book?.rating! : 0;
        this.ratingValueStars = this.ratingValue;

        this.genresString = (this.book?.genres || []).map((i) => i.name).join(', ');
        this.themesString = (this.book?.themes || []).map((t) => t.name).join(', ');

        this.bookmarkedService.isBookmarked(this.userId, this.bookId).subscribe({
          next: (result) => (this.isBookmarked = result),
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

        this.loading.stop();
      },
      error: () => {
        this.error = 'Boek niet gevonden.';
        this.loading.stop();
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Boek niet gevonden.',
          life: 3000,
        });
      },
    });
  }

  toggleDropdown() {
    this.showDropdown = !this.showDropdown;
  }

  addToList(listId: number) {
    if (!this.book) return;
    const list = this.lists.find((l) => l.id === listId);
    this.bookListService.addBook(listId, this.book.id).subscribe({
      next: () => {
        this.showDropdown = false;
        this.messageService.add({
          severity: 'success',
          summary: 'succes',
          detail: `Boek succesvol toegevoegd aan lijst: "${list?.name}"`,
          life: 3000,
        });
      },
    });
    this.lists = this.lists.filter((l) => l.id !== listId);
  }
  loanForm = new FormGroup({
    start: new FormControl<Date | null>(null, Validators.required),
    end: new FormControl<Date | null>(null, Validators.required),
    requestedAmount: new FormControl<number | null>(null, [
      Validators.required,
      Validators.min(1),
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
    end.setDate(end.getDate() + (this.campus?.borrowPeriod ?? 14));
    this.loanForm.controls.end.setValue(end);
  }

  createLoan(): void {
    if (this.loanForm.invalid || !this.book) return;
    const rawValue = this.loanForm.value;

    const book: CreateLoanBookDTO = {
      bookId: this.bookId,
      requestedAmount: rawValue.requestedAmount ?? 1,
      receivedAmount: 0,
      returnedAmount: 0,
    };

    const loan: CreateLoanDTO = {
      userId: this.userId,
      campusId: this.campusId,
      extended: 0,
      start: this.formatDate(rawValue.start ?? new Date()),
      end: this.formatDate(rawValue.end ?? new Date()),
      note: '',
      status: LoanStatus.REQUESTED,
      closed: false,
      books: [book],
    };

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
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Er is iets misgegaan bij het versturen van dit ontleenverzoek, probeer opnieuw.',
          life: 3000,
        });
      },
    });
  }
  cancelLoan(): void {
    this.loanFormVisible = false;
    this.loanForm.reset();
  }
  addToCart(): void {
    if (this.cartForm.invalid || !this.book) return;

    const book: CartBook = {
      id: this.bookId,
      bookId: this.bookId,
      title: this.book.title,
      author: this.book.author,
      author_name: this.book.author_name,
      cover: this.book.cover,
      requestedAmount: this.cartForm.value.requestedAmount ?? 1,
      receivedAmount: 0,
      returnedAmount: 0,
    };
    try {
      this.loanCartService.addBook(book);
      this.messageService.add({
        severity: 'success',
        summary: 'Toegevoegd',
        detail: `"${this.book.title}" is toegevoegd aan uw ontleenlijst.`,
        life: 3000,
      });
      this.cartForm.reset();
      this.cartDialogVisible = false;
    } catch (e) {
      const message =
        e instanceof Error && e.message === 'BORROW_LIMIT_REACHED'
          ? 'U heeft het maximum aantal boeken bereikt.'
          : 'Dit boek staat al in uw ontleenlijst.';
      this.messageService.add({
        severity: 'error',
        summary: 'Fout',
        detail: message,
        life: 3750,
      });
    }
  }

  cancelCart(): void {
    this.cartDialogVisible = false;
    this.cartForm.reset();
  }

  private loadCampusData(): void {
    this.campusService.getById(this.campusId).subscribe({
      next: (campus) => {
        this.campus = campus;
      },
    });
    this.campusBookService.getCampusBook(this.campusId, this.bookId).subscribe({
      next: (campusBook) => {
        this.campusBook = campusBook;
        this.setAmountValidators(campusBook.current_amount);
      },
      error: () =>
        this.messageService.add({
          severity: 'info',
          summary: '',
          detail: 'Uw campus heeft dit boek niet of het is niet meer beschikbaar.',
          life: 3000,
        }),
    });
  }

  getMaterial(): void {
    if (this.materialFetched === true) {
      return;
    }

    // fetch the materials
    this.materialService.getAll(this.bookId).subscribe({
      next: (materials) => {
        this.materials = materials;
        this.materialFetched = true;
      },
    });
  }

  uploadMaterial($event: FileSelectEvent, fileUploader: any): void {
    if (!this.bookId) return;
    const formData = new FormData();
    formData.append('file', $event.files[0]);
    formData.append('book_id', this.bookId.toString());

    this.uploadService.addMaterial(formData).subscribe({
      next: () => {
        fileUploader.clear();
      },
      error: () =>
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Probleem met het uploaden van lesmateriaal.',
          life: 3200,
        }),
    });
  }

  private formatDate(d: Date): string {
    return d.toLocaleDateString('en-CA');
  }

  private setAmountValidators(maxAmount: number): void {
    const validators = [
      Validators.required,
      Validators.min(1),
      Validators.max(maxAmount),
      Validators.pattern('^[0-9]*$'),
    ];
    this.loanForm.controls.requestedAmount.setValidators(validators);
    this.cartForm.controls.requestedAmount.setValidators(validators);
    this.loanForm.controls.requestedAmount.updateValueAndValidity();
    this.cartForm.controls.requestedAmount.updateValueAndValidity();
  }
  getStarFill(position: number): number {
    if (this.ratingValue >= position) return 100;
    if (this.ratingValue <= position - 1) return 0;
    return (this.ratingValue - (position - 1)) * 100;
  }
}
