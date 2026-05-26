import { Component } from '@angular/core';
import { HttpClient, HttpEventType } from '@angular/common/http';
import { timeout } from 'rxjs/operators';
import { BulkUpload as BulkUpload_1 } from '../../services/BulkUpload';
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
import { IftaLabel } from 'primeng/iftalabel';
import { Genre } from '../../models/genre';
import { MultiSelectModule } from 'primeng/multiselect';
import { Language } from '../../models/language';
import { BookType } from '../../models/book-type';
import { Theme } from '../../models/theme';
import { ThemeService } from '../../services/theme';
import { BulkPreviewResult } from '../../models/bulk';

interface RowIssue {
  row: number;
  message: string;
}

interface IncompleteBookDTO {
  row: number;
  isbn: string | null;
  title: string;
  author_name: string;
  description: string | null;
  publisher_name: string | null;
  published_year: number | null;
  pages: number | null;
  cover_url: string | null;
  language_code: string | null;
  language_name: string | null;
  book_type_name: string | null;
  genres_raw: string | null;
  themes_raw: string | null;
  font_size: string | null;
  fiction: string;
  didactic: string;
  school_only: string;
  clib: string | null;
  missing_fields: string[];
  invalid_fields: string[];
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
    MultiSelectModule,
    IftaLabel,
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
  genres: Genre[] | undefined;
  bookTypes: BookType[] | undefined;
  languages: Language[] | undefined;
  themes: Theme[] | undefined;

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
    private themeService: ThemeService,
  ) {
    this.genreService.getAll().subscribe((g) => (this.genres = g));
    this.bookTypeService.getAll().subscribe((bt) => {
      this.bookTypes = bt;
    });
    this.languageService.getAll().subscribe((l) => (this.languages = l));
    this.themeService.getAll().subscribe((g) => (this.themes = g));
  }

  private setDefaultBookType(key: string): void {
    if (this.completions[key].bookTypes?.length || !this.bookTypes?.length) return;

    const defaultType = this.bookTypes.find(
      (t) => t.name.toLowerCase() === 'boek' || t.name.toLowerCase() === 'book',
    );

    if (defaultType) this.completions[key].bookTypes = [defaultType.id];
  }

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
      this.uploadError = 'Alleen .xlsx-bestanden zijn toegestaan.';
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
          console.log('Preview result:', result);
          console.log(
            'Found:',
            result.foundCount,
            'Not found:',
            result.notFoundCount,
            'Total:',
            result.total,
          );

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
              : (err?.error?.message ?? 'Uploaden mislukt. Probeer opnieuw.');
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
      const key = this.getCompletionKey(book);
      this.completions[key] = {
        bookTypeName: book.book_type_name ?? '',
        genreIds: [] as number[],
        themesRaw: book.themes_raw ?? '',
        language: [] as number[],
        description: book.description ?? '',
        fiction: book.fiction ?? 'JA',
        didactic: book.didactic ?? 'NEE',
        schoolOnly: book.school_only ?? 'NEE',
        clib: book.clib ?? '',
        fontSize: book.font_size ?? '',
        publisherName: book.publisher_name ?? '',
        pages: book.pages ?? '',
        year: book.published_year ?? '',
        cover: book.cover_url ?? '',
      };

      if (book.language_code && this.languages) {
        const match = this.languages.find((l) => l.code === book.language_code);
        if (match) {
          this.completions[key].language = [match.id];
          this.completions[key].languageName = match.name;
        }
      }

      if (!this.completions[key].language.length && book.language_name && this.languages) {
        const match = this.languages.find(
          (l) => l.name.toLowerCase() === book.language_name!.toLowerCase(),
        );
        if (match) {
          this.completions[key].language = [match.id];
          this.completions[key].languageName = match.name;
        }
      }

      if (book.genres_raw && this.genres) {
        const names = book.genres_raw
          .trim()
          .split(/\s+/)
          .map((n) => n.toLowerCase());
        this.completions[key].genreIds = this.genres
          .filter((g) => names.includes(g.name.toLowerCase()))
          .map((g) => g.id);
      }

      if (book.themes_raw && this.themes) {
        console.log('themes_raw:', book.themes_raw, 'themes:', this.themes);

        const names = book.themes_raw
          .split(',')
          .map((n) => n.trim().toLowerCase())
          .filter((n) => n.length > 0);
        this.completions[key].themes = this.themes
          .filter((t) => names.includes(t.name.toLowerCase()))
          .map((t) => t.id);
      }

      if (book.book_type_name && this.bookTypes) {
        const match = this.bookTypes.find(
          (bt) => bt.name.toLowerCase() === book.book_type_name!.toLowerCase(),
        );
        if (match) {
          this.completions[key].bookTypes = [match.id];
          this.completions[key].bookTypeName = match.name;
        }
      }

      this.setDefaultBookType(key);
      this.autoSubmitIfComplete(book);
    }
  }

  submitCompletion(book: IncompleteBookDTO): void {
    console.log('isbn:', book.isbn, 'missing_fields:', book.missing_fields);

    const key = this.getCompletionKey(book);
    const c = this.completions[key];

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

    forkJoin(lookups).subscribe({
      next: (results) => {
        const author = results['author']?.[0];
        const bookType = this.bookTypes?.find((bt) => bt.id === c.bookTypes?.[0]);
        const language = this.languages?.find((l) => l.id === c.language?.[0]);
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

        if (c.genreIds.length === 0) {
          this.messageService.add({
            severity: 'error',
            summary: 'Genres niet gevonden',
            detail: 'Geen van de opgegeven genres bestaat in de database',
            life: 5000,
          });
          return;
        }

        const body: CreateBook = {
          isbn: book.isbn || undefined,
          title: book.title,
          author: author.id,
          book_type: bookType.id,
          language: language.id,
          genres: c.genreIds,
          description: c.description || book.description,
          fiction: c.fiction === 'JA',
          published: book.published_year ?? undefined,
          pages: book.pages ?? undefined,
          cover_url: book.cover_url ?? undefined,
          publisher: publisher?.id,
          themes: c.themes?.length ? c.themes : undefined,
          clib: c.clib?.trim() || undefined,
          font_size: c.fontSize ? c.fontSize.toUpperCase() : undefined,
          didactic: false,
          school: false,
        };

        this.bookService.addBook(body).subscribe({
          next: () => {
            this.result!.incomplete = this.result!.incomplete.filter(
              (b) => this.getCompletionKey(b) !== key,
            );
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

  getCompletionKey(book: IncompleteBookDTO): string {
    return book.isbn || `row_${book.row}`;
  }

  isFieldMissing(book: IncompleteBookDTO, field: string): boolean {
    if (!book.missing_fields.includes(field)) return false;
    const key = this.getCompletionKey(book);
    const c = this.completions[key];

    switch (field) {
      case 'beschrijving':
        return !c.description?.trim();
      case 'boektype':
        return !c.bookTypes?.length;
      case 'genres':
        return !c.genreIds?.length;
      case 'taal':
        return !c.language?.length;
      case 'fictie':
        return !c.fiction;
      default:
        return false;
    }
  }

  private autoSubmitIfComplete(book: IncompleteBookDTO): void {
    const key = this.getCompletionKey(book);
    const c = this.completions[key];

    const hasDescription = c.description?.trim() && c.description.trim().length > 0;
    const hasBookType = c.bookTypes?.length > 0;
    const hasGenres = c.genreIds?.length > 0;
    const hasLanguage = c.language?.length > 0;
    const hasFiction = !!c.fiction;

    if (hasDescription && hasBookType && hasGenres && hasLanguage && hasFiction) {
      console.log('Auto-submitting complete book:', book.title);
      this.submitCompletion(book);
    }
  }
}
