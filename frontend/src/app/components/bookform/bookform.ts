import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import {
  FormsModule,
  ReactiveFormsModule,
  FormControl,
  FormGroup,
  Validators,
} from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { DialogModule } from 'primeng/dialog';
import { TextareaModule } from 'primeng/textarea';
import { IftaLabelModule } from 'primeng/iftalabel';
import { FluidModule } from 'primeng/fluid';
import { RadioButtonModule } from 'primeng/radiobutton';
import { MultiSelectModule } from 'primeng/multiselect';
import { FileSelectEvent, FileUploadModule } from 'primeng/fileupload';
import { InputNumberModule } from 'primeng/inputnumber';
import { StepperModule } from 'primeng/stepper';
import { MessageService } from 'primeng/api';
import { Message } from 'primeng/message';
import { ToastModule } from 'primeng/toast';
import { MessageModule } from 'primeng/message';
import { Router } from '@angular/router';

import { isbnValidator, maxEntries } from '../../utils/validator';
import { BookCard, BookLookupDTO, CreateBook } from '../../models/book';
import { Author } from '../../models/author';
import { Genre } from '../../models/genre';
import { Publisher } from '../../models/publisher';
import { Language } from '../../models/language';
import { BookType } from '../../models/book-type';
import { Series } from '../../models/series';

import { GenreService } from '../../services/genre';
import { AuthorService } from '../../services/author';
import { PublisherService } from '../../services/publisher';
import { BookService } from '../../services/book';
import { LanguageService } from '../../services/language';
import { BookTypeService } from '../../services/book-type';
import { UploadService } from '../../services/upload';
import { SeriesService } from '../../services/series';

import { CharCounterComponent } from '../char-counter/char-counter';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { BulkUpload } from '../bulk-upload/bulk-upload';
import { Theme } from '../../models/theme';
import { ThemeService } from '../../services/theme';
// import { JsonPipe } from '@angular/common';
import { BookCardComponent } from '../misc/book-card/book-card';

@Component({
  selector: 'app-bookform',
  standalone: true,
  imports: [
    ButtonModule,
    InputTextModule,
    TextareaModule,
    ReactiveFormsModule,
    SelectModule,
    DialogModule,
    IftaLabelModule,
    FluidModule,
    RadioButtonModule,
    MultiSelectModule,
    FormsModule,
    FileUploadModule,
    StepperModule,
    CharCounterComponent,
    NavBarComponent,
    Message,
    InputNumberModule,
    ToastModule,
    MessageModule,
    BulkUpload,
    // JsonPipe,
    BookCardComponent,
  ],
  templateUrl: './bookform.html',
  styleUrl: './bookform.css',
  providers: [MessageService],
})
export class BookformComponent implements OnInit {
  bookForm = new FormGroup({
    title: new FormControl('', Validators.required),
    author: new FormControl<number | null>(null, Validators.required),
    cover: new FormControl<string | null>(null),
    isbn: new FormControl<string | null>(null, isbnValidator),
    book_type: new FormControl<number | null>(null, Validators.required),
    clib: new FormControl<string | null>(null),
    series: new FormControl<number | null>(null),
    series_count: new FormControl<number | null>(null),
    contributors: new FormControl<number[]>([]),
    didactic: new FormControl<boolean>(false, Validators.required),
    publisher: new FormControl<number | null>(null),
    fiction: new FormControl<boolean>(true, Validators.required),
    genres: new FormControl<number[] | null>([], [maxEntries(5), Validators.required]),
    themes: new FormControl<number[] | null>([], [maxEntries(5)]),
    description: new FormControl<string | null>(null, [
      Validators.maxLength(500),
      Validators.required,
    ]),
    published: new FormControl<number | null>(null, [Validators.min(1)]),
    language: new FormControl<number | null>(null, Validators.required),
    pages: new FormControl<number | null>(null, [Validators.min(1)]),
    font_size: new FormControl<string | null>(null),
    school: new FormControl<boolean>(false, Validators.required),
  });
  get invalidControls() {
    return Object.entries(this.bookForm.controls)
      .filter(([, c]) => c.invalid)
      .map(([k]) => k);
  }

  authorForm = new FormGroup({
    name: new FormControl<string>('', Validators.required),
    description: new FormControl<string | null>(null),
  });

  publisherForm = new FormGroup({
    name: new FormControl<string>('', Validators.required),
    description: new FormControl<string | null>(null),
  });

  seriesForm = new FormGroup({
    name: new FormControl<string>('', Validators.required),
    description: new FormControl<string | null>(null),
  });

  genres: Genre[] | undefined;
  themes: Theme[] | undefined;
  languages: Language[] | undefined;
  publishers: Publisher[] | undefined;
  authors: Author[] | undefined;
  bookTypes: BookType[] | undefined;
  series: Series[] | undefined;
  uploadMode: 'manual' | 'bulk' = 'manual';
  lookupMethod: 'auto' | 'manual' = 'auto';
  isbnLookupValue: string = '';
  isLookingUp: boolean = false;
  lookupCoverUrl: string | null = null;
  lookupResult: BookLookupDTO | null = null;
  suggestionCard: BookCard | null = null;

  newBookId: number | undefined;
  coverDisabled: boolean = true;
  authorFormVisible: boolean = false;
  publisherFormVisible: boolean = false;
  seriesFormVisible: boolean = false;

  font_sizes = [
    { name: 'Klein', value: 'KLEIN' },
    { name: 'Medium', value: 'MEDIUM' },
    { name: 'Groot', value: 'GROOT' },
  ];
  cLIBS = ['A', 'B', 'C', 'D'];

  constructor(
    private genreService: GenreService,
    private messageService: MessageService,
    private authorService: AuthorService,
    private publisherService: PublisherService,
    private bookService: BookService,
    private languageService: LanguageService,
    private bookTypeService: BookTypeService,
    private uploadService: UploadService,
    private seriesService: SeriesService,
    private themeService: ThemeService,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    this.genreService.getAll().subscribe((g) => (this.genres = g));
    this.themeService.getAll().subscribe((t) => (this.themes = t));
    this.publisherService.getAll().subscribe((p) => (this.publishers = p));
    this.authorService.getAll().subscribe((a) => (this.authors = a));
    this.languageService.getAll().subscribe((l) => (this.languages = l));
    this.bookTypeService.getAll().subscribe((bt) => {
      this.bookTypes = bt;
      this.setDefaultBookType();
    });
    this.seriesService.getAll().subscribe((s) => (this.series = s));
  }

  private setDefaultBookType(): void {
    if (this.bookForm.value.book_type !== null || !this.bookTypes?.length) return;
    const defaultType = this.bookTypes.find(
      (type) => type.name.toLowerCase() === 'boek' || type.name.toLowerCase() === 'book',
    );
    if (defaultType) this.bookForm.patchValue({ book_type: defaultType.id });
  }

  private showError(detail: string) {
    this.messageService.add({ severity: 'error', summary: 'Fout', detail, life: 3000 });
  }

  addBook(activateCallback: (step: number) => void): void {
    if (this.bookForm.valid) {
      const book: CreateBook = {
        ...(this.bookForm.value as any),
        cover_url: this.lookupCoverUrl ?? undefined,
      };
      this.bookService.addBook(book).subscribe({
        next: (savedBook) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succes',
            detail: 'Boek opgeslagen.',
            life: 3000,
          });
          this.coverDisabled = false;
          this.newBookId = savedBook.id;
          activateCallback(3);
        },
        error: () => this.showError('Fout bij opslaan boek.'),
      });
    }
  }

  addAuthor(): void {
    if (this.authorForm.valid) {
      this.authorService.addAuthor(this.authorForm.value as Author).subscribe({
        next: (a) => {
          this.authors = [...(this.authors || []), a];
          this.bookForm.patchValue({ author: a.id });
          this.authorFormVisible = false;
          this.authorForm.reset();
        },
        error: () => this.showError('Fout bij opslaan auteur.'),
      });
    }
  }

  addPublisher(): void {
    if (this.publisherForm.valid) {
      this.publisherService.addPublisher(this.publisherForm.value as Publisher).subscribe({
        next: (p) => {
          this.publishers = [...(this.publishers || []), p];
          this.publisherFormVisible = false;
          this.publisherForm.reset();
        },
        error: () => this.showError('Fout bij opslaan uitgever.'),
      });
    }
  }

  addSeries(): void {
    if (this.seriesForm.valid) {
      const seriesData = {
        name: this.seriesForm.value.name!,
        description: this.seriesForm.value.description!,
        authorId: this.bookForm.value.author,
      };

      this.seriesService.create(seriesData as any).subscribe({
        next: (s) => {
          this.series = [...(this.series || []), s];
          this.seriesFormVisible = false;
          this.bookForm.patchValue({ series: s.id });
          this.seriesForm.reset();
        },
        error: () => this.showError('Fout bij opslaan serie.'),
      });
    }
  }

  coverUpload($event: FileSelectEvent, fileUploader: any) {
    if (!this.newBookId) return;
    const formData = new FormData();
    formData.append('file', $event.files[0]);
    formData.append('book_id', this.newBookId.toString());

    this.uploadService.addCover(formData).subscribe({
      next: () => {
        this.router.navigate(['/boek', this.newBookId]);
        fileUploader.clear();
      },
      error: () => this.showError('Probleem hij het uploaden van cover.'),
    });
  }

  setMode(mode: 'manual' | 'bulk'): void {
    this.uploadMode = mode;
    this.lookupMethod = 'auto';
    this.rejectSuggestion();
  }

  selectMethod(method: 'auto' | 'manual'): void {
    this.lookupMethod = method;
    if (method === 'manual') this.rejectSuggestion();
  }

  lookupIsbn(): void {
    if (this.isLookingUp) return;
    if (isbnValidator({ value: this.isbnLookupValue } as any) !== null) {
      this.showError('Ongeldig ISBN. Voer een geldig ISBN-10 of ISBN-13 in.');
      return;
    }
    this.isLookingUp = true;
    const cleanIsbn = this.isbnLookupValue.replace(/[-\s]/g, '');
    this.bookService.lookupByIsbn(cleanIsbn).subscribe({
      next: (result) => {
        this.lookupResult = result;
        this.suggestionCard = {
          id: 0,
          title: result.title ?? '',
          author: { id: 0, name: result.author_name ?? '' },
          author_name: result.author_name ?? '',
          cover: result.cover_url ?? undefined,
        };
        this.isLookingUp = false;
      },
      error: () => {
        this.showError('ISBN niet gevonden. Probeer het opnieuw of vul het boek manueel in.');
        this.isLookingUp = false;
      },
    });
  }

  acceptSuggestion(): void {
    if (!this.lookupResult) return;
    this.prefillForm(this.lookupResult);
    this.lookupMethod = 'manual';
  }

  rejectSuggestion(): void {
    this.lookupResult = null;
    this.suggestionCard = null;
  }

  resetForm(): void {
    this.bookForm.reset({
      didactic: false,
      fiction: true,
      school: false,
      contributors: [],
    });
    this.lookupMethod = 'auto';
    this.isbnLookupValue = '';
    this.lookupCoverUrl = null;
    this.lookupResult = null;
    this.suggestionCard = null;
    this.newBookId = undefined;
    this.coverDisabled = true;
    this.setDefaultBookType();
    this.cdr.detectChanges();
    document.getElementById('isbn-lookup')?.focus();
  }

  private prefillForm(data: BookLookupDTO): void {
    this.bookForm.patchValue({
      title: data.title ?? '',
      isbn: data.isbn,
      description: data.description ?? null,
      pages: data.pages ?? null,
      published: data.published_year ?? null,
    });

    this.lookupCoverUrl = data.cover_url ?? null;

    if (data.author_name) {
      const target = this.normalizeName(data.author_name);
      const match = this.authors?.find((a) => this.normalizeName(a.name) === target);
      if (match) {
        this.bookForm.patchValue({ author: match.id });
      } else {
        this.authorService.addAuthor({ name: data.author_name } as Author).subscribe({
          next: (a) => {
            this.authors = [...(this.authors || []), a];
            this.bookForm.patchValue({ author: a.id });
            this.matchContributors(data);
          },
          error: () => this.showError('Auteur niet automatisch aangemaakt.'),
        });
      }
    }

    if (data.publisher_name && this.publishers) {
      const match = this.publishers.find(
        (p) => p.name.toLowerCase() === data.publisher_name!.toLowerCase(),
      );
      if (match) this.bookForm.patchValue({ publisher: match.id });
    }

    if (data.language_code && this.languages) {
      const match = this.languages.find(
        (l) => l.code.toLowerCase() === data.language_code!.toLowerCase(),
      );
      if (match) this.bookForm.patchValue({ language: match.id });
    }

    this.matchContributors(data);
  }

  private matchContributors(data: BookLookupDTO): void {
    if (!data.contributors?.length || !this.authors) return;
    const matchedIds = data.contributors
      .map((name) => {
        const target = this.normalizeName(name);
        return this.authors!.find((a) => this.normalizeName(a.name) === target);
      })
      .filter((a): a is Author => a !== undefined)
      .map((a) => a.id)
      .filter((id) => id !== this.bookForm.value.author);
    if (matchedIds.length > 0) this.bookForm.patchValue({ contributors: matchedIds });
  }

  private normalizeName(name: string): string {
    return name.toLowerCase().replace(/\s+/g, '');
  }
}
