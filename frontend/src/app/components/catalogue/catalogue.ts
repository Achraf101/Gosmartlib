import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TagModule } from 'primeng/tag';
import { DividerModule } from 'primeng/divider';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { BookService } from '../../services/book';
import { BookResult } from '../../models/book';
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

  readonly placeholder = '/assets/no-cover.svg';

  constructor(
    private bookService: BookService,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      this.searchQuery = params['q'] || '';
      this.currentPage = 0;
      this.loadBooks();
    });
  }

  loadBooks(): void {
    this.loading = true;
    const request = this.searchQuery.trim()
      ? this.bookService.search(this.searchQuery.trim(), this.currentPage, this.rows)
      : this.bookService.getAllBookResults(this.currentPage, this.rows);

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
}
