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
  ],
  templateUrl: './bookform.html',
  styleUrl: './bookform.css',
})
export class BookformComponent {
  bookForm = new FormGroup({
    title: new FormControl('', Validators.required),
    author: new FormControl<number | null>(null),
    cover: new FormControl<string | null>(null),
    isbn: new FormControl<string | null>(null, isbnValidator),
    book_type: new FormControl<number | null>(null, Validators.required),

    series: new FormControl<number | null>(null),
    series_count: new FormControl<number | null>(null),

    contributors: new FormControl<number[]>([], maxEntries(5)),
    publisher: new FormControl<number | null>(null),

    fiction: new FormControl<boolean>(false, Validators.required),
    genre: new FormControl<number[]>([], maxEntries(5)),

    description: new FormControl<string | null>(null),

    published: new FormControl<number | null>(null),
    language: new FormControl<number | null>(null, Validators.required),

    age_start: new FormControl<number | null>(null),
    age_end: new FormControl<number | null>(null),

    pages: new FormControl<number | null>(null),
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

  font_sizes = [
    { name: 'Klein', value: 'KLEIN' },
    { name: 'Medium', value: 'MEDIUM' },
    { name: 'Groot', value: 'GROOT' },
  ];

  constructor(
    private genreService: GenreService,
    private messageService: MessageService,
    private authorService: AuthorService,
    private publisherService: PublisherService,
    private bookService: BookService,
    private languageService: LanguageService,
    private bookTypeService: BookTypeService,
  ) {}

  ngOnInit() {
    this.genreService.getAll().subscribe({
      next: (genres) => {
        this.genres = genres;
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Probleem met het laden van genres.',
          life: 3000,
        });
      },
    });

    this.publisherService.getAll().subscribe({
      next: (publishers) => {
        this.publishers = publishers;
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Probleem met het laden van uitgevers.',
          life: 3000,
        });
      },
    });

    this.authorService.getAll().subscribe({
      next: (authors) => {
        this.authors = authors;
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Probleem met het laden van auteurs.',
          life: 3000,
        });
      },
    });

    this.languageService.getAll().subscribe({
      next: (languages) => {
        this.languages = languages;
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Probleem met het laden van talen.',
          life: 3000,
        });
      },
    });

    this.bookTypeService.getAll().subscribe({
      next: (bookTypes) => {
        this.bookTypes = bookTypes;
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Probleem met het laden van boek types.',
          life: 3000,
        });
      },
    });
  }

  addBook(): void {
    if (this.bookForm.valid) {
      const book: CreateBook = this.bookForm.value as CreateBook;
      this.bookForm.reset({ school: false, fiction: false });
      this.bookService.addBook(book).subscribe({
        next: (book) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Boek is opgeslagen.',
            life: 3000,
          });
        },
        error: (err) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Probleem met het opslagen van het boek.',
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
          // this.bookForm.patchValue({
          //   author: author.id,
          // });
          this.authors?.push(author);
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Auter is opgeslagen.',
            life: 3000,
          });
        },
        error: (err) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Probleem met het opslagen van auteur.',
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
          // this.bookForm.patchValue({
          //   publisher: publisher.id,
          // });
          this.publishers?.push(publisher);
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Uitgever is opgeslagen.',
            life: 3000,
          });
        },
        error: (err) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Probleem met het opslagen van uitgever.',
            life: 3000,
          });
        },
      });
    }
  }
}
