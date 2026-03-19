import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TagModule } from 'primeng/tag';
import { DividerModule } from 'primeng/divider';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { BookService } from '../../services/book';
import { BookFilter, BookResult } from '../../models/book';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-catalogue',
  standalone: true,
  imports: [
    FormsModule,
    TagModule,
    ProgressSpinnerModule,
    PaginatorModule,
    NavBarComponent,
    RouterLink,
    DividerModule,
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

  readonly placeholder = '/assets/no-cover.svg';

  constructor(
    private bookService: BookService,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      this.searchQuery = params['q'] || '';
      this.currentPage = 0;

      const hasFilters = params['genres'] || params['language'] || params['fiction'] !== undefined
        || params['authorIds'] || params['pagesMin'] || params['pagesMax'];

      if (hasFilters) {
        this.activeFilters = {
          genre: params['genres'] ? params['genres'].split(',').map(Number) : undefined,
          language: params['language'] ? Number(params['language']) : undefined,
          fiction: params['fiction'] !== undefined ? params['fiction'] === 'true' : undefined,
          author: params['authorIds'] ? params['authorIds'].split(',').map(Number) : undefined,
          pages: (params['pagesMin'] || params['pagesMax']) ? [
            params['pagesMin'] ? Number(params['pagesMin']) : 0,
            params['pagesMax'] ? Number(params['pagesMax']) : 999999
          ] : undefined,
        };
      } else {
        this.activeFilters = null;
      }

      this.loadBooks();
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
    this.loadBooks();
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.currentPage = 0;
    this.loadBooks();
  }

  onPageChange(event: PaginatorState): void {
    this.currentPage = event.page ?? 0;
    this.rows = event.rows ?? 5;
    this.loadBooks();
  }
  get activeQueryParams(): any {
  return this.route.snapshot.queryParams;
}
}