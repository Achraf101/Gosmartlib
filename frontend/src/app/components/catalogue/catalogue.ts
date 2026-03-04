import { Component, OnInit } from '@angular/core';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { PaginatorModule } from 'primeng/paginator';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { BookService } from '../../services/book';
import { Book } from '../../models/book';

@Component({
  selector: 'app-catalogue',
  standalone: true,
  imports: [CardModule, TagModule, PaginatorModule, ProgressSpinnerModule],
  templateUrl: './catalogue.html',
  styleUrl: './catalogue.css',
})
export class CatalogueComponent implements OnInit {
  books: Book[] = [];
  paginatedBooks: Book[] = [];
  loading = true;
  first = 0;
  rows = 8;

  readonly placeholder = 'https://placehold.co/200x300/e2e8f0/64748b?text=Geen+Cover';

  constructor(private bookService: BookService) {}

  ngOnInit(): void {
    this.bookService.getAll().subscribe({
      next: (data) => {
        this.books = data;
        this.paginatedBooks = data.slice(0, this.rows);
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
    this.paginatedBooks = this.books.slice(this.first, this.first + this.rows);
  }
}
