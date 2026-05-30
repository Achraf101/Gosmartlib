import {
  AfterViewChecked,
  Component,
  ElementRef,
  OnInit,
  QueryList,
  ViewChild,
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
import { BookCopyDetail } from '../../models/bookCopy';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { BookService } from '../../services/book';
import { ProgressSpinner } from 'primeng/progressspinner';
import { DelayedLoader } from '../../utils/delayed-loader';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { LocationBookDetail } from '../../models/locationBookDetail';
import { SearchBar } from '../misc/search-bar/search-bar';
import { DialogModule } from 'primeng/dialog';
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

  // Delete copy dialog
  deleteDialogVisible = false;
  deleteAccessionInput = '';
  foundCopyForDeletion: BookCopyDetail | null = null;
  deleteLookupLoading = false;

  @ViewChildren('barcodesvg') barcodeSvgs!: QueryList<ElementRef<SVGElement>>;
  @ViewChild('deleteInput') deleteInputRef!: ElementRef<HTMLInputElement>;

  constructor(
    private locationService: LocationService,
    private messageService: MessageService,
    private bookService: BookService,
    private locationBookService: LocationBookService,
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
    const svgElements = this.barcodeSvgs.toArray();
    const printWindow = window.open('', '_blank', 'width=800,height=600');
    if (!printWindow) return;

    const cards = this.newAccessionIds
      .map((id, i) => {
        const svgHtml = svgElements[i]?.nativeElement?.outerHTML ?? '';
        return `<div class="barcode-card">${svgHtml}<span class="accession-label">${id}</span></div>`;
      })
      .join('');

    printWindow.document.write(`<!DOCTYPE html>
<html><head><title>Barcodes afdrukken</title><style>
  body { margin: 1rem; font-family: monospace; }
  .barcode-grid { display: flex; flex-wrap: wrap; gap: 1rem; }
  .barcode-card { display: flex; flex-direction: column; align-items: center; gap: 0.25rem; padding: 0.5rem 0.75rem; border: 1px dashed #d1d5db; border-radius: 6px; page-break-inside: avoid; break-inside: avoid; }
  .accession-label { font-size: 0.8rem; letter-spacing: 0.05em; }
</style></head><body><div class="barcode-grid">${cards}</div></body></html>`);
    printWindow.document.close();
    printWindow.focus();
    printWindow.print();
    printWindow.close();
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

  openDeleteDialog(): void {
    this.deleteDialogVisible = true;
    this.deleteAccessionInput = '';
    this.foundCopyForDeletion = null;
    setTimeout(() => this.deleteInputRef?.nativeElement?.focus(), 100);
  }

  lookupCopyForDeletion(): void {
    const id = this.deleteAccessionInput.trim();
    if (!id) return;
    this.deleteLookupLoading = true;
    this.foundCopyForDeletion = null;
    this.locationBookService.getCopyByAccessionId(id).subscribe({
      next: (copy) => {
        this.foundCopyForDeletion = copy;
        this.deleteLookupLoading = false;
      },
      error: () => {
        this.deleteLookupLoading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Niet gevonden',
          detail: `Exemplaar '${id}' niet gevonden.`,
          life: 3000,
        });
      },
    });
  }

  confirmDeleteCopy(): void {
    if (!this.foundCopyForDeletion) return;
    const accessionId = this.foundCopyForDeletion.accession_id;
    this.locationBookService.deleteCopyByAccessionId(accessionId).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Verwijderd',
          detail: `Exemplaar ${accessionId} is verwijderd.`,
          life: 3000,
        });
        this.closeDeleteDialog();
        this.loadLocationBooks();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Verwijderen mislukt. Probeer opnieuw.',
          life: 3000,
        });
      },
    });
  }

  closeDeleteDialog(): void {
    this.deleteDialogVisible = false;
    this.deleteAccessionInput = '';
    this.foundCopyForDeletion = null;
  }

}
