import { Component, OnInit } from '@angular/core';
import {
  FormsModule,
  ReactiveFormsModule,
  FormControl,
  FormGroup,
  Validators,
} from '@angular/forms';
import { CardModule } from 'primeng/card';
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
import { BookLookupDTO, CreateBook } from '../../models/book';
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
import { UpLoadService } from '../../services/upload';
import { SeriesService } from '../../services/series';

import { CharCounterComponent } from '../char-counter/char-counter';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { BulkUpload } from '../bulk-upload/bulk-upload';

@Component({
  selector: 'app-bookform',
  standalone: true,
  imports: [
    CardModule,
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
    didactic_material: new FormControl<boolean>(false, Validators.required),
    publisher: new FormControl<number | null>(null),
    fiction: new FormControl<boolean>(true, Validators.required),
    genres: new FormControl<number[]>([], [maxEntries(5), Validators.required]),
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
  languages: Language[] | undefined;
  publishers: Publisher[] | undefined;
  authors: Author[] | undefined;
  bookTypes: BookType[] | undefined;
  series: Series[] | undefined;
  uploadMode: 'manual' | 'bulk' = 'manual';
  lookupMethod: null | 'auto' | 'manual' = null;
  isbnLookupValue: string = '';
  isLookingUp: boolean = false;

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
    private upLoadService: UpLoadService,
    private seriesService: SeriesService,
    private router: Router,
  ) {}

  ngOnInit() {
    this.genreService.getAll().subscribe((g) => (this.genres = g));
    this.publisherService.getAll().subscribe((p) => (this.publishers = p));
    this.authorService.getAll().subscribe((a) => (this.authors = a));
    this.languageService.getAll().subscribe((l) => (this.languages = l));
    this.bookTypeService.getAll().subscribe((bt) => (this.bookTypes = bt));
    this.seriesService.getAll().subscribe((s) => (this.series = s));
  }

  private showError(detail: string) {
    this.messageService.add({ severity: 'error', summary: 'Fout', detail, life: 3000 });
  }

  addBook(activateCallback: (step: number) => void): void {
    if (this.bookForm.valid) {
      const book: CreateBook = this.bookForm.value as any;
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

    this.upLoadService.addCover(formData).subscribe({
      next: () => {
        this.router.navigate(['/boek', this.newBookId]);
        fileUploader.clear();
      },
      error: () => this.showError('Boekomslag niet toegevoegd.'),
    });
  }

  setMode(mode: 'manual' | 'bulk'): void {
    this.uploadMode = mode;
    this.lookupMethod = null;
  }

  selectMethod(method: 'auto' | 'manual'): void {
    this.lookupMethod = method;
  }

  lookupIsbn(): void {
    if (isbnValidator({ value: this.isbnLookupValue } as any) !== null) {
      this.showError('Ongeldig ISBN. Voer een geldig ISBN-10 of ISBN-13 in.');
      return;
    }
    this.isLookingUp = true;
    this.bookService.lookupByIsbn(this.isbnLookupValue).subscribe({
      next: (result) => {
        this.prefillForm(result);
        this.lookupMethod = 'manual';
        this.isLookingUp = false;
      },
      error: () => {
        this.showError('ISBN niet gevonden. Probeer het opnieuw of vul het boek manueel in.');
        this.isLookingUp = false;
      },
    });
  }

  private prefillForm(data: BookLookupDTO): void {
    this.bookForm.patchValue({
      title: data.title ?? '',
      isbn: data.isbn,
      description: data.description ?? null,
      pages: data.pages ?? null,
      published: data.publishedYear ?? null,
    });

    if (data.authorName && this.authors) {
      const match = this.authors.find(
        (a) => a.name.toLowerCase() === data.authorName!.toLowerCase(),
      );
      if (match) this.bookForm.patchValue({ author: match.id });
    }

    if (data.publisherName && this.publishers) {
      const match = this.publishers.find(
        (p) => p.name.toLowerCase() === data.publisherName!.toLowerCase(),
      );
      if (match) this.bookForm.patchValue({ publisher: match.id });
    }

    if (data.languageCode && this.languages) {
      const match = this.languages.find(
        (l) => l.code.toLowerCase() === data.languageCode!.toLowerCase(),
      );
      if (match) this.bookForm.patchValue({ language: match.id });
    }

    if (data.genres?.length && this.genres) {
      const matchedIds = data.genres
        .map((gName) =>
          this.genres!.find((g) => g.name.toLowerCase() === gName.toLowerCase()),
        )
        .filter((g): g is Genre => g !== undefined)
        .map((g) => g.id)
        .slice(0, 5);
      if (matchedIds.length > 0) this.bookForm.patchValue({ genres: matchedIds });
    }
  }
}
