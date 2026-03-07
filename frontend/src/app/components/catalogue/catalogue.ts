import { Component, OnInit } from '@angular/core';
import { TagModule } from 'primeng/tag';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { BookService } from '../../services/book';
import { BookResult } from '../../models/book';

@Component({
  selector: 'app-catalogue',
  standalone: true,
  imports: [TagModule, ProgressSpinnerModule],
  templateUrl: './catalogue.html',
  styleUrl: './catalogue.css',
})
export class CatalogueComponent implements OnInit {
  books: BookResult[] = [];
  loading = true;

  readonly placeholder = 'https://placehold.co/150x220/e2e8f0/64748b?text=Geen+Cover';

  constructor(private bookService: BookService) {}

  ngOnInit(): void {
    this.loadBooks();
  }

  loadBooks(): void {
    this.loading = true;
    this.bookService.getAll().subscribe({
      next: (data) => {
        this.books = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }
}
