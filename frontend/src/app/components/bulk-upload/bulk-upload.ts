import { Component } from '@angular/core';
import { HttpClient, HttpEventType } from '@angular/common/http';
import { timeout } from 'rxjs/operators';
import { BulkUpload as BulkUpload_1, BulkPreviewResult } from '../../services/BulkUpload';
import { ButtonModule } from 'primeng/button';
import { FormsModule } from '@angular/forms';
import { BookService } from '../../services/book';
import { CreateBook } from '../../models/book';
import { AuthorService } from '../../services/author';
import { PublisherService } from '../../services/publisher';
import { BookTypeService } from '../../services/book-type';
import { GenreService } from '../../services/genre';
import { LanguageService } from '../../services/language';
import { forkJoin, Observable } from 'rxjs';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { MessageService } from 'primeng/api';

interface RowIssue {
  row: number;
  message: string;
}

interface IncompleteBookDTO {
  row: number;
  isbn: string;
  title: string;
  author_name: string;
  description: string | null;
  publisher_name: string | null;
  published_year: number | null;
  pages: number | null;
  cover_url: string | null;
  language_code: string | null;
  missing_fields: string[];
}

interface BulkUploadResult {
  added: number;
  skipped: RowIssue[];
  errors: RowIssue[];
  incomplete: IncompleteBookDTO[];
  skippedCount: number;
  errorCount: number;
  incompleteCount: number;
  total_processed: number;
  fullSuccess: boolean;
}

const LOOKUP_TIMEOUT_MS = 5 * 60 * 1000;

@Component({
  selector: 'app-bulk-upload',
  templateUrl: './bulk-upload.html',
  imports: [
    ButtonModule,
    FormsModule,
    ButtonModule,
    CardModule,
    TagModule,
    InputTextModule,
    InputTextModule,
    SelectModule,
  ],
  styleUrls: ['./bulk-upload.css'],
})
export class BulkUpload {
  selectedFile: File | null = null;
  isDragging = false;
  isUploading = false;
  isPreviewing = false;
  uploadProgress = 0;
  preview: BulkPreviewResult | null = null;
  result: BulkUploadResult | null = null;
  uploadError: string | null = null;
  completions: Record<string, any> = {};

  constructor(
    private http: HttpClient,
    private bulkUpload: BulkUpload_1,
    private bookService: BookService,
    private authorService: AuthorService,
    private publisherService: PublisherService,
    private bookTypeService: BookTypeService,
    private genreService: GenreService,
    private languageService: LanguageService,
    private messageService: MessageService,
  ) {}

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = true;
  }

  onDragLeave(): void {
    this.isDragging = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = false;
    const file = event.dataTransfer?.files[0];
    if (file) this.setFile(file);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) this.setFile(file);
  }

  private setFile(file: File): void {
    if (!file.name.endsWith('.xlsx')) {
      this.uploadError = 'Only .xlsx files are accepted.';
      return;
    }
    this.selectedFile = file;
    this.uploadError = null;
    this.preview = null;
    this.result = null;
  }

  clearFile(): void {
    this.selectedFile = null;
    this.uploadProgress = 0;
    this.preview = null;
    this.result = null;
    this.uploadError = null;
  }

  runPreview(): void {
    if (!this.selectedFile || this.isPreviewing) return;

    this.isPreviewing = true;
    this.uploadError = null;
    this.preview = null;

    this.bulkUpload
      .preview(this.selectedFile)
      .pipe(timeout(LOOKUP_TIMEOUT_MS))
      .subscribe({
        next: (result) => {
          this.preview = result;
          this.isPreviewing = false;
        },
        error: (err) => {
          this.uploadError =
            err?.name === 'TimeoutError'
              ? 'ISBN-opzoeking duurde te lang. Probeer opnieuw.'
              : (err?.error?.message ?? 'Voorbeeld mislukt. Probeer opnieuw.');
          this.isPreviewing = false;
        },
      });
  }

  upload(): void {
    if (!this.selectedFile) return;

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    this.isUploading = true;
    this.uploadProgress = 0;
    this.uploadError = null;

    this.http
      .post<BulkUploadResult>('/api/excel/book/bulk-upload', formData, {
        reportProgress: true,
        observe: 'events',
      })
      .pipe(timeout(LOOKUP_TIMEOUT_MS))
      .subscribe({
        next: (event) => {
          if (event.type === HttpEventType.UploadProgress && event.total) {
            this.uploadProgress = Math.round((100 * event.loaded) / event.total);
          } else if (event.type === HttpEventType.Response) {
            this.result = event.body;
            this.initCompletions();
            this.isUploading = false;
          }
        },
        error: (err) => {
          this.uploadError =
            err?.name === 'TimeoutError'
              ? 'Import duurde te lang. Probeer opnieuw.'
              : (err?.error?.message ?? 'Upload failed. Please try again.');
          this.isUploading = false;
        },
      });
  }

  reset(): void {
    this.selectedFile = null;
    this.preview = null;
    this.result = null;
    this.uploadProgress = 0;
    this.uploadError = null;
  }

  initCompletions(): void {
    if (!this.result?.incomplete) return;

    for (const book of this.result.incomplete) {
      book.missing_fields = book.missing_fields || [];

      this.completions[book.isbn] = {
        bookTypeName: '',
        genresRaw: '',
        languageName: '',
        description: book.description ?? '',
        fiction: 'JA',
      };

      if (book.language_code) {
        console.log(`Looking up language code: ${book.language_code} for ISBN ${book.isbn}`);

        this.languageService.getByCode(book.language_code).subscribe({
          next: (languages) => {
            console.log(`Language lookup result for ${book.language_code}:`, languages);

            if (languages) {
              this.completions[book.isbn].languageName = languages.name;
              console.log(`Auto-filled language for ${book.isbn}: ${languages.name}`);
              console.log('Updated completions:', this.completions[book.isbn]);
            } else {
              console.warn(`No language found for code: ${book.language_code}`);
            }
          },
          error: (err) => {
            console.error(`Failed to lookup language for code ${book.language_code}:`, err);
          },
        });
      } else {
        console.log(`No language_code for ISBN ${book.isbn}`);
      }
    }

    console.log('Initialized completions:', this.completions);
    console.log('Incomplete books:', this.result.incomplete);
  }

  submitCompletion(book: IncompleteBookDTO): void {
    const c = this.completions[book.isbn];

    if (book.missing_fields.includes('beschrijving')) {
      const description = c.description?.trim();
      if (!description || description.length === 0) {
        this.messageService.add({
          severity: 'error',
          summary: 'Beschrijving vereist',
          detail: 'Beschrijving mag niet leeg zijn',
          life: 5000,
        });
        return;
      }
    }

    const lookups: { [key: string]: Observable<any> } = {};

    lookups['author'] = this.authorService.getByName(book.author_name);

    if (c.bookTypeName) {
      lookups['bookType'] = this.bookTypeService.getByName(c.bookTypeName);
    }
    if (c.languageName) {
      lookups['language'] = this.languageService.getByName(c.languageName);
    }
    if (book.publisher_name) {
      lookups['publisher'] = this.publisherService.getByName(book.publisher_name);
    }

    if (c.genresRaw) {
      const genreNames = c.genresRaw.trim().split(/\s+/);
      genreNames.forEach((name: string, index: number) => {
        lookups[`genre_${index}`] = this.genreService.getByName(name);
      });
    }

    forkJoin(lookups).subscribe({
      next: (results) => {
        const author = results['author']?.[0];
        const bookType = results['bookType']?.[0];
        const language = results['language']?.[0];
        const publisher = results['publisher']?.[0];

        console.log('Extracted:', { author, bookType, language, publisher });

        if (!author) {
          this.messageService.add({
            severity: 'error',
            summary: 'Auteur niet gevonden',
            detail: `Auteur "${book.author_name}" bestaat niet in de database`,
            life: 5000,
          });
          return;
        }
        if (!bookType) {
          this.messageService.add({
            severity: 'error',
            summary: 'Boektype niet gevonden',
            detail: `Boektype "${c.bookTypeName}" bestaat niet in de database`,
            life: 5000,
          });
          return;
        }
        if (!language) {
          this.messageService.add({
            severity: 'error',
            summary: 'Taal niet gevonden',
            detail: `Taal "${c.languageName}" bestaat niet in de database`,
            life: 5000,
          });
          return;
        }

        const genreIds: number[] = [];
        Object.keys(results).forEach((key) => {
          if (key.startsWith('genre_')) {
            const genreArray = results[key];
            if (genreArray && genreArray.length > 0) {
              genreIds.push(genreArray[0].id);
            }
          }
        });

        if (genreIds.length === 0) {
          this.messageService.add({
            severity: 'error',
            summary: 'Genres niet gevonden',
            detail: 'Geen van de opgegeven genres bestaat in de database',
            life: 5000,
          });
          return;
        }
        const body: CreateBook = {
          isbn: book.isbn,
          title: book.title,
          author: author.id,
          book_type: bookType.id,
          language: language.id,
          genres: genreIds,
          description: c.description || book.description,
          fiction: c.fiction === 'JA',
          published: book.published_year ?? undefined,
          pages: book.pages ?? undefined,
          cover_url: book.cover_url ?? undefined,
          publisher: publisher?.id,
          didactic: false,
          school: false,
        };

        this.bookService.addBook(body).subscribe({
          next: () => {
            this.result!.incomplete = this.result!.incomplete.filter((b) => b.isbn !== book.isbn);
            this.result!.added++;

            this.messageService.add({
              severity: 'success',
              summary: 'Boek opgeslagen',
              detail: `"${book.title}" succesvol toegevoegd`,
              life: 3000,
            });
          },
          error: (err) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Fout bij opslaan',
              detail: err.error?.message ?? 'Onbekende fout opgetreden',
              life: 5000,
            });
          },
        });
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout bij ophalen gegevens',
          detail:
            err.error?.message ?? err.message ?? 'Controleer of alle velden correct zijn ingevuld',
          life: 5000,
        });
      },
    });
  }
}
