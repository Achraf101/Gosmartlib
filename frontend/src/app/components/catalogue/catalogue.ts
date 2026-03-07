import { Component, OnInit } from '@angular/core';
import { TagModule } from 'primeng/tag';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { BookService } from '../../services/book';
import { BookResult } from '../../models/book';

@Component({
  selector: 'app-catalogue',
  standalone: true,
  imports: [TagModule, ProgressSpinnerModule, PaginatorModule],
  templateUrl: './catalogue.html',
  styleUrl: './catalogue.css',
})
export class CatalogueComponent implements OnInit {
  books: BookResult[] = [];
  loading = true;
  totalRecords = 0;
  rows = 5;
  currentPage = 0;

  readonly placeholder = 'https://placehold.co/150x220/e2e8f0/64748b?text=Geen+Cover';

  constructor(private bookService: BookService) {}

  ngOnInit(): void {
    this.loadBooks();
  }

  loadBooks(): void {
    this.loading = true;
    this.bookService.getAll(this.currentPage, this.rows).subscribe({
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

  onPageChange(event: PaginatorState): void {
    this.currentPage = event.page ?? 0;
    this.rows = event.rows ?? 5;
    this.loadBooks();
  }
}
