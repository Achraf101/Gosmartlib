import { Component, OnInit } from '@angular/core';
import { Select } from 'primeng/select';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { RouterModule } from '@angular/router';
import { Campus } from '../../models/campus';
import { CampusService } from '../../services/campus';
import { MessageService } from 'primeng/api';
import { BookBase } from '../../models/book';
import { CampusBook } from '../../models/CampusBook';
import { CampusBookService } from '../../services/campusbook';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { BookService } from '../../services/book';
import { ProgressSpinner } from 'primeng/progressspinner';

import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { CampusBookDetail } from '../../models/CampusBookDetail';

@Component({
  selector: 'app-campus-detail-page',
  imports: [
    Select,
    FormsModule,
    CommonModule,
    ButtonModule,
    RouterModule,
    ReactiveFormsModule,
    NavBarComponent,
    ProgressSpinner,
    PaginatorModule,
  ],
  templateUrl: './campus-detail-page.html',
  styleUrl: './campus-detail-page.css',
})
export class CampusDetailPageComponent implements OnInit {
  campuses: Campus[] = [];
  books: BookBase[] = [];
  campusBooks: CampusBookDetail[] = [];
  selectedCampusId: number | null = null;
  selectedCampus: Campus | null = null;
  selectedBookId: number | null = null;
  searchQuery = '';
  currentPage: number = 0;
  loading: boolean = false;
  rows: number = 5;
  totalRecords: number = 0;
  amounts: { [bookId: number]: number } = {};
  locations: { [bookId: number]: string } = {};
  readonly placeholder = 'https://placehold.co/150x220/e2e8f0/64748b?text=Geen+Cover';
  campusBooksPage = 0;
  campusBooksRows = 5;
  campusTotalRecords = 0;

  addedBooks: Set<number> = new Set();

  constructor(
    private campusService: CampusService,
    private messageService: MessageService,
    private bookService: BookService,
    private campusBookService: CampusBookService,
  ) {}

  ngOnInit(): void {
    this.campusService.getAll().subscribe({
      next: (campuses) => (this.campuses = campuses),
      error: () =>
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Probleem met het laden van campussen.',
          life: 3000,
        }),
    });
    this.loadBooks();
  }

  onSelect(event: any) {
    this.campusService.getById(event.value).subscribe((data) => {
      this.selectedCampus = data;
      this.loadCampusBooks(data.id);
    });
  }

  addBook(book: BookBase) {
    const amount = this.amounts[book.id];
    const location = this.locations[book.id] ?? '';
    if (!this.selectedCampus) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Geen campus',
        detail: 'Selecteer eerst een campus.',
        life: 3000,
      });
      return;
    }
    if (!amount || amount < 1) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Aantal vereist',
        detail: 'Vul een geldig aantal in (minimaal 1).',
        life: 3000,
      });
      return;
    }
    const newCampusBook: Omit<CampusBook, 'id'> = {
      campus_id: this.selectedCampus.id,
      book_id: book.id,
      amount: amount,
      current_amount: amount,
      location: location,
    };
    this.addedBooks.add(book.id);
    this.campusBookService.createCampusBook(newCampusBook).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'Boek is toegevoegd aan campus.',
          life: 3000,
        });
        this.loadCampusBooks();
      },
      error: () => {
        this.addedBooks.delete(book.id!);
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Probleem met toevoegen van boek aan campus.',
          life: 3000,
        });
      },
    });
  }

  onSearch() {
    this.currentPage = 0;
    this.loadBooks();
  }
  loadBooks() {
    this.loading = true;
    const request = this.searchQuery.trim()
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
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Probleem met het laden van boeken.',
          life: 3000,
        });
      },
    });
  }
  clearSearch() {
    this.searchQuery = '';
    this.currentPage = 0;
    this.onSearch();
  }
  onPageChange(event: PaginatorState): void {
    this.currentPage = event.page ?? 0;
    this.rows = event.rows ?? 5;
    this.loadBooks();
  }

  isAdded(bookId: number): boolean {
    return this.addedBooks.has(bookId);
  }

  alreadyAdded(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Fout',
      detail: 'Boek is al toegevoegd!',
      life: 3000,
    });
  }
  loadCampusBooks(campusId?: number): void {
    const id = campusId ?? this.selectedCampus!.id;

    if (!id) return;

    this.campusBookService.getByCampus(id, this.campusBooksPage, this.campusBooksRows).subscribe({
      next: (page) => {
        this.campusBooks = page.content;
        this.campusTotalRecords = page.total_elements;
      },
      error: () =>
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Probleem met het laden van de boeken in deze campus.',
          life: 3000,
        }),
    });
  }
  onCampusPageChange(event: PaginatorState): void {
    this.campusBooksPage = event.page ?? 0;
    this.campusBooksRows = event.rows ?? 5;
    this.loadCampusBooks();
  }
}
