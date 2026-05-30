import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TagModule } from 'primeng/tag';
import { DividerModule } from 'primeng/divider';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { InputNumberModule } from 'primeng/inputnumber';
import { MultiSelectModule } from 'primeng/multiselect';
import { BookService } from '../../services/book';
import { SectionService } from '../../services/section';
import { ApiService } from '../../services/api';
import { BookCard, BookFilter, BookResult } from '../../models/book';
import { Genre } from '../../models/genre';
import { Language } from '../../models/language';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { CommonModule } from '@angular/common';
import { SearchBar } from '../misc/search-bar/search-bar';
import { BookResult as BookResultComponent } from '../misc/book-result/book-result';
import { RouterLink } from '@angular/router';
import { DelayedLoader } from '../../utils/delayed-loader';
import { Theme } from '../../models/theme';
import { ThemeService } from '../../services/theme';
import { BookCover } from '../misc/book-cover/book-cover';
import { AuthService } from '../../services/auth';
import { RadioButton } from 'primeng/radiobutton';
import { Location } from '../../models/location';
import { BookListService } from '../../services/book-list';
import { MessageService } from 'primeng/api';
import { SchoolService } from '../../services/school';
import { School } from '../../models/school';

@Component({
  selector: 'app-catalogue',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TagModule,
    ProgressSpinnerModule,
    PaginatorModule,
    NavBarComponent,
    DividerModule,
    DialogModule,
    ButtonModule,
    SelectModule,
    InputNumberModule,
    MultiSelectModule,
    SearchBar,
    RouterLink,
    BookResultComponent,
    BookCover,
    RadioButton,
  ],
  templateUrl: './catalogue.html',
  styleUrl: './catalogue.css',
})
export class CatalogueComponent implements OnInit {
  books: BookResult[] = [];
  loading = new DelayedLoader();
  totalRecords = 0;
  rows = 5;
  currentPage = 0;
  searchQuery = '';
  activeFilters: BookFilter | null = null;
  hideLocation = false;

  genres: Genre[] = [];
  themes: Theme[] = [];
  languages: Language[] = [];
  locations: Location[] = [];
  oneLocation: boolean = false;
  sidebarLocation: number | null = null;
  sidebarGenres: number[] = [];
  sidebarThemes: number[] = [];
  sidebarDidactic: boolean | null = null;
  sidebarLanguage: number | null = null;
  sidebarPagesMin: number | null = null;
  sidebarPagesMax: number | null = null;

  viewMode: 'list' | 'grid' = 'list';
  rowsPerPageOptions = [5, 10, 20];

  selectMode = false;
  sectionId: number | null = null;
  grade: number | null = null;
  selectedBook: BookResult | null = null;
  ranking: number | null = null;
  listId: number | null = null;
  showDialog = false;
  school?: School;

  constructor(
    private bookService: BookService,
    private sectionService: SectionService,
    private apiService: ApiService,
    private route: ActivatedRoute,
    private router: Router,
    private themeService: ThemeService,
    public auth: AuthService,
    public bookListService: BookListService,
    public messageService: MessageService,
    public schoolService: SchoolService,
  ) {}

  ngOnInit(): void {
    this.apiService.get<Genre[]>('genre').subscribe((g) => (this.genres = g));
    this.themeService.getAll().subscribe({
      next: (data) => (this.themes = data),
    });
    this.apiService.get<Language[]>('language').subscribe((l) => (this.languages = l));

    this.schoolService.getById(this.auth.currentUser?.schoolId ?? 1).subscribe({
      next: (data) => {
        this.school = data;
        if (this.school.locations && this.school.locations.length > 1) {
          this.locations = this.school.locations;
        } else if (this.school.locations?.length === 1) {
          this.oneLocation = true;
          this.sidebarLocation = this.school.locations[0].id;
          if (this.activeFilters) {
            this.activeFilters.location = this.sidebarLocation;
          } else {
            this.activeFilters = { location: this.sidebarLocation };
          }
        }
      },
    });

    this.route.queryParams.subscribe((params) => {
      this.searchQuery = params['q'] || '';
      this.currentPage = params['pagina'] ? Number(params['pagina']) - 1 : 0;
      this.selectMode = params['selectMode'] === 'true';
      this.sectionId = params['sectionId'] ? Number(params['sectionId']) : null;
      this.grade = params['grade'] ? Number(params['grade']) : null;
      this.ranking = params['ranking'] ? Number(params['ranking']) : null;
      this.listId = params['listId'] ? Number(params['listId']) : null;

      this.sidebarLocation = params['location'] ? Number(params['location']) : null;
      this.sidebarGenres = params['genres'] ? params['genres'].split(',').map(Number) : [];
      this.sidebarThemes = params['themes'] ? params['themes'].split(',').map(Number) : [];
      this.sidebarLanguage = params['language'] ? Number(params['language']) : null;
      this.sidebarPagesMin = params['pagesMin'] ? Number(params['pagesMin']) : null;
      this.sidebarPagesMax = params['pagesMax'] ? Number(params['pagesMax']) : null;
      this.sidebarDidactic =
        params['didactic'] !== undefined ? params['didactic'] === 'true' : null;

      const hasFilters =
        params['genres'] ||
        params['themes'] ||
        params['language'] ||
        params['fiction'] !== undefined ||
        params['authorIds'] ||
        params['seriesIds'] ||
        params['pagesMin'] ||
        params['pagesMax'] ||
        params['didactic'] ||
        params['location'] ||
        params['clibs'];

      if (hasFilters) {
        this.activeFilters = {
          genre: params['genres'] ? params['genres'].split(',').map(Number) : undefined,
          theme: params['themes'] ? params['themes'].split(',').map(Number) : undefined,
          language: params['language'] ? Number(params['language']) : undefined,
          fiction: params['fiction'] !== undefined ? params['fiction'] === 'true' : undefined,
          author: params['authorIds'] ? params['authorIds'].split(',').map(Number) : undefined,
          series: params['seriesIds'] ? params['seriesIds'].split(',').map(Number) : undefined,
          clibs: params['clibs'] ? params['clibs'].split(',') : undefined,
          didactic: params['didactic'] !== undefined ? params['didactic'] === 'true' : undefined,
          location: params['location'] !== undefined ? Number(params['location']) : undefined,
          pages:
            params['pagesMin'] || params['pagesMax']
              ? [
                  params['pagesMin'] ? Number(params['pagesMin']) : 0,
                  params['pagesMax'] ? Number(params['pagesMax']) : 999999,
                ]
              : undefined,
        };
      } else {
        this.activeFilters = null;
      }

      this.loadBooks();
    });
  }

  applyFilters(): void {
    const params: any = { ...this.route.snapshot.queryParams };
    if (this.sidebarGenres.length > 0) {
      params['genres'] = this.sidebarGenres.join(',');
    } else {
      delete params['genres'];
    }
    if (this.sidebarThemes.length > 0) {
      params['themes'] = this.sidebarThemes.join(',');
    } else {
      delete params['themes'];
    }
    if (this.sidebarLanguage) {
      params['language'] = this.sidebarLanguage;
    } else {
      delete params['language'];
    }
    if (this.sidebarPagesMin !== null) {
      params['pagesMin'] = this.sidebarPagesMin;
    } else {
      delete params['pagesMin'];
    }
    if (this.sidebarPagesMax !== null) {
      params['pagesMax'] = this.sidebarPagesMax;
    } else {
      delete params['pagesMax'];
    }
    if (this.sidebarDidactic !== null) {
      params['didactic'] = this.sidebarDidactic;
    } else {
      delete params['didactic'];
    }
    if (this.sidebarLocation !== null) {
      params['location'] = this.sidebarLocation;
    } else {
      delete params['location'];
    }
    params['pagina'] = 1;
    this.router.navigate([], { relativeTo: this.route, queryParams: params });
  }

  clearSidebarFilters(): void {
    this.sidebarGenres = [];
    this.sidebarThemes = [];
    this.sidebarDidactic = null;
    this.sidebarLanguage = null;
    this.sidebarPagesMin = null;
    this.sidebarPagesMax = null;
    if (!this.oneLocation) {
      this.sidebarLocation = null;
    }
    const params: any = { ...this.route.snapshot.queryParams };
    delete params['genres'];
    delete params['themes'];
    delete params['language'];
    delete params['pagesMin'];
    delete params['pagesMax'];
    delete params['didactic'];
    if (!this.oneLocation) delete params['location'];
    params['pagina'] = 1;
    this.router.navigate([], { relativeTo: this.route, queryParams: params });
  }

  onBookClick(book: BookResult): void {
    if (this.selectMode) {
      this.selectedBook = book;
      this.showDialog = true;
    } else {
      this.router.navigate(['/boek', book.id], {
        queryParams: this.sidebarLocation ? { location: this.sidebarLocation } : {},
      });
    }
  }

  viewBookDetails(): void {
    this.showDialog = false;
    this.router.navigate(['/boek', this.selectedBook!.id]);
  }

  setAsBookOfMonth(): void {
    if (!this.sectionId || !this.grade || !this.selectedBook) return;
    this.sectionService.setBookOfMonth(this.sectionId, this.selectedBook.id, this.grade).subscribe({
      next: () => {
        this.showDialog = false;
        this.router.navigate(['/']);
      },
      error: () => {
        this.showDialog = false;
      },
    });
  }

  setAsSpotlight(): void {
    if (!this.sectionId || !this.ranking || !this.selectedBook) return;
    this.sectionService
      .setSpotlightBook(this.sectionId, this.selectedBook.id, this.ranking)
      .subscribe({
        next: () => {
          this.showDialog = false;
          this.router.navigate(['/dashboard/bibliotheek-beheerder']);
        },
        error: () => {
          this.showDialog = false;
        },
      });
  }

  loadBooks(): void {
    this.loading.start();

    const filters: BookFilter = {
      ...(this.activeFilters ?? {}),
      query: this.searchQuery.trim() || undefined,
    };

    if (this.oneLocation && this.sidebarLocation) {
      filters.location = this.sidebarLocation;
    }

    const hasAnything =
      this.activeFilters || this.searchQuery.trim() || (this.oneLocation && this.sidebarLocation);

    const request = hasAnything
      ? this.bookService.filter(filters, this.currentPage, this.rows)
      : this.bookService.getAll(this.currentPage, this.rows);

    request.subscribe({
      next: (page) => {
        this.books = page.content;
        this.totalRecords = page.total_elements;
        this.loading.stop();
      },
      error: () => {
        this.loading.stop();
      },
    });
  }

  onSearch(query: string): void {
    this.searchQuery = query;
    this.currentPage = 0;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { pagina: 1, q: this.searchQuery.trim() || null },
      queryParamsHandling: 'merge',
    });
    this.loadBooks();
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.currentPage = 0;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { pagina: 1, q: null },
      queryParamsHandling: 'merge',
    });
    this.loadBooks();
  }

  setViewMode(mode: 'list' | 'grid'): void {
    if (this.viewMode === mode) return;
    this.viewMode = mode;
    if (mode === 'grid') {
      this.rowsPerPageOptions = [3, 6, 9];
      this.rows = 3;
    } else {
      this.rowsPerPageOptions = [5, 10, 20];
      this.rows = 5;
    }
    this.currentPage = 0;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { pagina: 1 },
      queryParamsHandling: 'merge',
    });
    this.loadBooks();
  }

  onPageChange(event: PaginatorState): void {
    this.currentPage = event.page ?? 0;
    this.rows = event.rows ?? 5;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { pagina: this.currentPage + 1 },
      queryParamsHandling: 'merge',
    });
    this.loadBooks();
  }

  get activeQueryParams(): any {
    return this.route.snapshot.queryParams;
  }

  getAsBookCard(book: BookResult): BookCard {
    return {
      id: book.id,
      title: book.title,
      author: book.author,
      cover: book.cover,
    };
  }

  addBookToList() {
    if (!this.selectedBook || !this.listId) return;

    this.bookListService.addBook(Number(this.listId), this.selectedBook.id).subscribe({
      next: () => {
        this.showDialog = false;
        this.router.navigate(['/boekenlijst', this.listId]);
      },
    });
  }
}
