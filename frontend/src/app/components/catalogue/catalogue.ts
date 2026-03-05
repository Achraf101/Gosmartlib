import { Component, OnInit } from '@angular/core';
import { TagModule } from 'primeng/tag';
import { PaginatorModule } from 'primeng/paginator';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { BookService } from '../../services/book';
import { Book } from '../../models/book';

@Component({
  selector: 'app-catalogue',
  standalone: true,
  imports: [TagModule, PaginatorModule, ProgressSpinnerModule],
  templateUrl: './catalogue.html',
  styleUrl: './catalogue.css',
})
export class CatalogueComponent implements OnInit {
  books: Book[] = [];
  loading = true;
  first = 0;
  rows = 5;
  totalRecords = 0;

  readonly placeholder = 'https://placehold.co/150x220/e2e8f0/64748b?text=Geen+Cover';

  constructor(private bookService: BookService) {}

  ngOnInit(): void {
    this.loadBooks(0, this.rows);
  }

  loadBooks(page: number, size: number): void {
    this.loading = true;
    this.bookService.getBooks(page, size).subscribe({
      next: (data) => {
        this.books = data.content;
        this.totalRecords = data.total_elements;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  onPageChange(event: any): void {
    this.first = event.first;
    this.rows = event.rows;
    const page = Math.floor(event.first / event.rows);
    this.loadBooks(page, event.rows);
  }
}
