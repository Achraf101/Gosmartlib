import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TagModule } from 'primeng/tag';
import { DividerModule } from 'primeng/divider';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { BookService } from '../../services/book';
import { SectionService } from '../../services/section';
import { BookFilter, BookResult } from '../../models/book';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

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
    RouterLink,
    DividerModule,
    DialogModule,
    ButtonModule,
  ],
  templateUrl: './catalogue.html',
  styleUrl: './catalogue.css',
})
export class CatalogueComponent implements OnInit {
  books: BookResult[] = [];
  loading = true;
  totalRecords = 0;
  rows = 5;
  currentPage = 0;
  searchQuery = '';
  activeFilters: BookFilter | null = null;

  // Select mode
  selectMode = false;
  sectionId: number | null = null;
  grade: number | null = null;
  selectedBook: BookResult | null = null;
  showDialog = false;

  readonly placeholder = '/assets/no-cover.svg';

  constructor(
    private bookService: BookService,
    private sectionService: SectionService,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      this.searchQuery = params['q'] || '';
      // Fix voor paginering: gebruik 'pagina' of 'page' consistent
      this.currentPage = params['pagina'] ? Number(params['pagina']) - 1 : 0;
      this.selectMode = params['selectMode'] === 'true';
      this.sectionId = params['sectionId'] ? Number(params['sectionId']) : null;
      this.grade = params['grade'] ? Number(params['grade']) : null;

      
      const hasFilters =
        params['genres'] ||
        params['language'] ||
        params['fiction'] !== undefined ||
        params['authorIds'] ||
        params['seriesIds'] || 
        params['pagesMin'] ||
        params['pagesMax'] ||
        params['clibs'];

      if (hasFilters) {
        this.activeFilters = {
  genre: params['genres'] ? params['genres'].split(',').map(Number) : undefined,
  language: params['language'] ? Number(params['language']) : undefined,
  fiction: params['fiction'] !== undefined ? params['fiction'] === 'true' : undefined,
  author: params['authorIds'] ? params['authorIds'].split(',').map(Number) : undefined,
  series: params['seriesIds'] ? params['seriesIds'].split(',').map(Number) : undefined, 
  clibs: params['clibs'] ? params['clibs'].split(',') : undefined,
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

  onBookClick(book: BookResult): void {
    if (this.selectMode) {
      this.selectedBook = book;
      this.showDialog = true;
    } else {
      this.router.navigate(['/boek', book.id]);
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
      }
    });
  }

  loadBooks(): void {
    this.loading = true;
    const request = this.activeFilters
      ? this.bookService.filter(this.activeFilters, this.currentPage, this.rows)
      : this.searchQuery.trim()
        ? this.bookService.search(this.searchQuery.trim(), this.currentPage, this.rows)
        : this.bookService.getAll(this.currentPage, this.rows);

    request.subscribe({
      next: (page) => {
        this.books = page.content;
        this.totalRecords = page.total_elements;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  onSearch(): void {
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
}