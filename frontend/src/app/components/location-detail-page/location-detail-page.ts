import { Component, OnInit } from '@angular/core';
import { Select } from 'primeng/select';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { RouterModule } from '@angular/router';
import { Location } from '../../models/location';
import { LocationService } from '../../services/location';
import { MessageService } from 'primeng/api';
import { BookBase } from '../../models/book';
import { LocationBook } from '../../models/locationBook';
import { LocationBookService } from '../../services/locationbook';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { BookService } from '../../services/book';
import { ProgressSpinner } from 'primeng/progressspinner';
import { DelayedLoader } from '../../utils/delayed-loader';

import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { LocationBookDetail } from '../../models/locationBookDetail';
import { SearchBar } from '../misc/search-bar/search-bar';

@Component({
  selector: 'app-location-detail-page',
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
    SearchBar,
  ],
  templateUrl: './location-detail-page.html',
  styleUrl: './location-detail-page.css',
})
export class LocationDetailPageComponent implements OnInit {
  locations: Location[] = [];
  books: BookBase[] = [];
  locationBooks: LocationBookDetail[] = [];
  selectedLocationId: number | null = null;
  selectedLocation: Location | null = null;
  selectedBookId: number | null = null;
  searchQuery = '';
  currentPage: number = 0;
  loading = new DelayedLoader();
  rows: number = 5;
  totalRecords: number = 0;
  amounts: { [bookId: number]: number } = {};
  readonly placeholder = '/assets/no-cover.svg';
  locationBooksPage = 0;
  locationBooksRows = 5;
  locationTotalRecords = 0;

  addedBooks: Set<number> = new Set();

  constructor(
    private locationService: LocationService,
    private messageService: MessageService,
    private bookService: BookService,
    private locationBookService: LocationBookService,
  ) {}

  ngOnInit(): void {
    this.locationService.getAll().subscribe({
      next: (locations) => (this.locations = locations),
      error: () =>
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Probleem met het laden van locaties.',
          life: 3000,
        }),
    });
    this.loadBooks();
  }

  onSelect(event: any) {
    this.locationService.getById(event.value).subscribe((data) => {
      this.selectedLocation = data;
      this.loadLocationBooks(data.id);
    });
  }

  addBook(book: BookBase) {
    const amount = this.amounts[book.id];
    if (!this.selectedLocation) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Geen locatie',
        detail: 'Selecteer eerst een locatie.',
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
    const newLocationBook: Omit<LocationBook, 'id'> = {
      location_id: this.selectedLocation.id,
      book_id: book.id,
      amount: amount,
      current_amount: amount,
    };
    this.addedBooks.add(book.id);
    this.locationBookService.createLocationBook(newLocationBook).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'Boek is toegevoegd aan locatie.',
          life: 3000,
        });
        this.loadLocationBooks();
      },
      error: () => {
        this.addedBooks.delete(book.id!);
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Probleem met toevoegen van boek aan locatie.',
          life: 3000,
        });
      },
    });
  }

  onSearch(query: string) {
    this.searchQuery = query;
    this.currentPage = 0;
    this.loadBooks();
  }
  loadBooks() {
    this.loading.start();
    const request = this.searchQuery.trim()
      ? this.bookService.search(this.searchQuery.trim(), this.currentPage, this.rows)
      : this.bookService.getAll(this.currentPage, this.rows);

    request.subscribe({
      next: (page) => {
        this.books = page.content;
        this.totalRecords = page.total_elements;
        this.loading.stop();
      },
      error: () => {
        this.loading.stop();
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Probleem met het laden van boeken.',
          life: 3000,
        });
      },
    });
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
  loadLocationBooks(locationId?: number): void {
    const id = locationId ?? this.selectedLocation!.id;

    if (!id) return;

    this.locationBookService
      .getByLocation(id, this.locationBooksPage, this.locationBooksRows)
      .subscribe({
        next: (page) => {
          this.locationBooks = page.content;
          this.locationTotalRecords = page.total_elements;
        },
        error: () =>
          this.messageService.add({
            severity: 'error',
            summary: 'Fout',
            detail: 'Probleem met het laden van de boeken in deze locatie.',
            life: 3000,
          }),
      });
  }
  onLocationPageChange(event: PaginatorState): void {
    this.locationBooksPage = event.page ?? 0;
    this.locationBooksRows = event.rows ?? 5;
    this.loadLocationBooks();
  }
}
