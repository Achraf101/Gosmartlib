import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { DialogModule } from 'primeng/dialog';
import { TextareaModule } from 'primeng/textarea';
import { IftaLabelModule } from 'primeng/iftalabel';
import { FluidModule } from 'primeng/fluid';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { TooltipModule } from 'primeng/tooltip';
import { RadioButtonModule } from 'primeng/radiobutton';
import { MultiSelectModule } from 'primeng/multiselect';
import { DatePickerModule } from 'primeng/datepicker';
import { FileSelectEvent, FileUploadModule } from 'primeng/fileupload';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CheckboxModule } from 'primeng/checkbox';
import { isbnValidator, maxEntries } from '../../utils/validator';
import { CreateBook } from '../../models/book';
import { Publisher } from '../../models/publisher';
import { Author } from '../../models/author';
import { Genre } from '../../models/genre';
import { GenreService } from '../../services/genre';
import { MessageService } from 'primeng/api';
import { Language } from '../../models/language';
import { AuthorService } from '../../services/author';
import { PublisherService } from '../../services/publisher';
import { BookService } from '../../services/book';
import { LanguageService } from '../../services/language';
import { BookTypeService } from '../../services/book-type';
import { BookType } from '../../models/book-type';
import { UpLoadService } from '../../services/upload';
import { StepperModule } from 'primeng/stepper';
import { CharCounterComponent } from '../char-counter/char-counter';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { Router } from '@angular/router';
import { Message } from 'primeng/message';
import { InputNumberModule } from 'primeng/inputnumber';

@Component({
  selector: 'app-bookform',
  imports: [
    ButtonModule,
    InputTextModule,
    TextareaModule,
    ReactiveFormsModule,
    SelectModule,
    DialogModule,
    IftaLabelModule,
    FluidModule,
    ToggleSwitchModule,
    TooltipModule,
    RadioButtonModule,
    MultiSelectModule,
    DatePickerModule,
    FormsModule,
    CheckboxModule,
    FileUploadModule,
    StepperModule,
    CharCounterComponent,
    NavBarComponent,
    Message,
    InputNumberModule,
  ],
  templateUrl: './bookform.html',
  styleUrl: './bookform.css',
})
export class BookformComponent {
  bookForm = new FormGroup({
    title: new FormControl('', Validators.required),
    author: new FormControl<number | null>(null, Validators.required),
    cover: new FormControl<string | null>(null),
    isbn: new FormControl<string | null>(null, isbnValidator),
    book_type: new FormControl<number | null>(null, Validators.required),
    cLIB: new FormControl('', Validators.required),
    series: new FormControl<number | null>(null),
    series_count: new FormControl<number | null>(null),
    didactic_material: new FormControl<boolean>(true, Validators.required),

    contributors: new FormControl<number[]>([], maxEntries(5)),
    publisher: new FormControl<number | null>(null),

    fiction: new FormControl<boolean>(true, Validators.required),
    genres: new FormControl<number[]>([], [maxEntries(5), Validators.required]),

    description: new FormControl<string | null>(null, [
      Validators.maxLength(500),
      Validators.required,
    ]),

    published: new FormControl<number | null>(null, [
      Validators.min(1),
      Validators.pattern('^[0-9]*$'),
    ]),
    language: new FormControl<number | null>(null, Validators.required),

    pages: new FormControl<number | null>(null, [
      Validators.min(1),
      Validators.pattern('^[0-9]*$'),
    ]),
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

  genres: Genre[] | undefined;
  languages: Language[] | undefined;
  publishers: Publisher[] | undefined;
  authors: Author[] | undefined;
  bookTypes: BookType[] | undefined;
  newBookId: number | undefined; // this will be populated when the bookform is submitted
  coverUploaded: boolean = false;
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
    private router: Router,
  ) {}

  ngOnInit() {
    this.genreService.getAll().subscribe({
      next: (genres) => {
        this.genres = genres;
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Probleem met het laden van genres.',
          life: 3000,
        });
      },
    });

    this.publisherService.getAll().subscribe({
      next: (publishers) => {
        this.publishers = publishers;
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Probleem met het laden van uitgevers.',
          life: 3000,
        });
      },
    });

    this.authorService.getAll().subscribe({
      next: (authors) => {
        this.authors = authors;
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Probleem met het laden van auteurs.',
          life: 3000,
        });
      },
    });

    this.languageService.getAll().subscribe({
      next: (languages) => {
        this.languages = languages;
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Probleem met het laden van talen.',
          life: 3000,
        });
      },
    });

    this.bookTypeService.getAll().subscribe({
      next: (bookTypes) => {
        this.bookTypes = bookTypes;
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Probleem met het laden van boektypes.',
          life: 3000,
        });
      },
    });
  }

  addBook(activateCallback: (step: number) => void): void {
    if (this.bookForm.valid) {
      const book: CreateBook = this.bookForm.value as CreateBook;
      this.bookForm.reset({ school: false, fiction: false });
      this.bookService.addBook(book).subscribe({
        next: (book) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succes',
            detail: 'Boek is opgeslagen.',
            life: 3000,
          });
          // allow cover upload
          this.coverDisabled = false;
          this.newBookId = book.id;
          activateCallback(3);
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Fout',
            detail: 'Probleem met het opslaan van het boek.',
            life: 3000,
          });
        },
      });
    }
  }

  authorFormVisible: boolean = false;
  addAuthor(): void {
    // BookService;
    if (this.authorForm.valid) {
      const author: Author = this.authorForm.value as Author;
      this.authorForm.reset();
      this.authorFormVisible = false;
      this.authorService.addAuthor(author).subscribe({
        next: (author) => {
          this.authors?.push(author);
          this.messageService.add({
            severity: 'success',
            summary: 'Succes',
            detail: 'Auteur is opgeslagen.',
            life: 3000,
          });
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Fout',
            detail: 'Probleem met het opslaan van auteur.',
            life: 3000,
          });
        },
      });
    }
  }

  publisherFormVisible: boolean = false;
  addPublisher(): void {
    // publisher service;
    if (this.publisherForm.valid) {
      const publisher: Publisher = this.publisherForm.value as Publisher;
      this.publisherForm.reset();
      this.publisherFormVisible = false;
      this.publisherService.addPublisher(publisher).subscribe({
        next: (publisher) => {
          this.publishers?.push(publisher);
          this.messageService.add({
            severity: 'success',
            summary: 'Succes',
            detail: 'Uitgever is opgeslagen.',
            life: 3000,
          });
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Fout',
            detail: 'Probleem met het opslaan van uitgever.',
            life: 3000,
          });
        },
      });
    }
  }

  coverDisabled: boolean = true;
  coverUpload($event: FileSelectEvent, fileUploader: any) {
    if (this.newBookId == undefined) {
      return;
    }
    const formData = new FormData();
    formData.append('file', $event?.files[0]);
    formData.append('book_id', this.newBookId.toString());

    this.upLoadService.addCover(formData).subscribe({
      next: () => {
        this.router.navigate(['/boek', this.newBookId]);
        this.coverUploaded = true;
        fileUploader.clear();
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'Boekomslag is toegevoegd.',
          life: 3000,
        });
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Boekomslag is niet toegevoegd.',
        });
      },
    });
  }
}
