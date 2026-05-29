import {
  AfterViewChecked,
  Component,
  ElementRef,
  OnInit,
  QueryList,
  ViewChildren,
} from '@angular/core';
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
import { BookCopyDetail, CopyStatus } from '../../models/bookCopy';
import { DialogModule } from 'primeng/dialog';
import { TagModule } from 'primeng/tag';
import { InputTextModule } from 'primeng/inputtext';
import { AuthService } from '../../services/auth';
import JsBarcode from 'jsbarcode';

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
    DialogModule,
    TagModule,
    InputTextModule,
  ],
  templateUrl: './location-detail-page.html',
  styleUrl: './location-detail-page.css',
})
export class LocationDetailPageComponent implements OnInit, AfterViewChecked {
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

  // Barcode dialog
  barcodeDialogVisible = false;
  newAccessionIds: string[] = [];
  private barcodesRendered = false;

  @ViewChildren('barcodesvg') barcodeSvgs!: QueryList<ElementRef<SVGElement>>;

  // Copy lookup
  lookupAccessionId = '';
  foundCopy: BookCopyDetail | null = null;
  lookupLoading = false;

  constructor(
    private locationService: LocationService,
    private messageService: MessageService,
    private bookService: BookService,
    private locationBookService: LocationBookService,
    public authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.locationService.getAll().subscribe({
      next: (locations) => (this.locations = locations),
    });
    this.loadBooks();
  }

  ngAfterViewChecked(): void {
    if (this.barcodeDialogVisible && !this.barcodesRendered && this.barcodeSvgs?.length) {
      this.barcodeSvgs.forEach((ref, i) => {
        const id = this.newAccessionIds[i];
        if (id) {
          JsBarcode(ref.nativeElement, id, {
            format: 'CODE128',
            displayValue: false,
            width: 2,
            height: 60,
          });
        }
      });
      this.barcodesRendered = true;
    }
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
      next: (result) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'Boek is toegevoegd aan locatie.',
          life: 3000,
        });
        this.loadLocationBooks();
        if (result.new_accession_ids?.length) {
          this.newAccessionIds = result.new_accession_ids;
          this.barcodesRendered = false;
          this.barcodeDialogVisible = true;
        }
      },
      error: () => {
        this.addedBooks.delete(book.id!);
      },
    });
  }

  closeBarcodeDialog(): void {
    this.barcodeDialogVisible = false;
    this.newAccessionIds = [];
    this.barcodesRendered = false;
  }

  printBarcodes(): void {
    window.print();
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
      : this.bookService.getAll(this.currentPage, this.rows, true);

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
      });
  }

  onLocationPageChange(event: PaginatorState): void {
    this.locationBooksPage = event.page ?? 0;
    this.locationBooksRows = event.rows ?? 5;
    this.loadLocationBooks();
  }

  lookupCopy(): void {
    const id = this.lookupAccessionId.trim().toUpperCase();
    if (!id) return;
    this.lookupLoading = true;
    this.foundCopy = null;
    this.locationBookService.getCopyByAccessionId(id).subscribe({
      next: (copy) => {
        this.foundCopy = copy;
        this.lookupLoading = false;
      },
      error: () => {
        this.lookupLoading = false;
        this.messageService.add({
          severity: 'warn',
          summary: 'Niet gevonden',
          detail: 'Exemplaar niet gevonden.',
          life: 3000,
        });
      },
    });
  }

  isLibrarian(): boolean {
    return (
      this.authService.hasRole('BIBLIOTHEEKBEHEERDER') || this.authService.hasRole('ADMIN')
    );
  }

  toggleCopyStatus(copy: BookCopyDetail): void {
    const newStatus: CopyStatus = copy.status === 'AVAILABLE' ? 'DAMAGED' : 'AVAILABLE';
    this.locationBookService.updateCopyStatus(copy.id, newStatus).subscribe({
      next: (updated) => {
        this.foundCopy = updated;
      },
    });
  }

  copyStatusSeverity(status: CopyStatus): 'success' | 'warn' {
    return status === 'AVAILABLE' ? 'success' : 'warn';
  }

  copyStatusLabel(status: CopyStatus): string {
    return status === 'AVAILABLE' ? 'Beschikbaar' : 'Beschadigd';
  }
}
