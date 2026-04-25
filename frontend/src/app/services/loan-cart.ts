import { Injectable, signal, computed } from '@angular/core';
import { CreateLoanBookDTO } from '../models/loanBook';
import { MessageService } from 'primeng/api';

const CART_KEY = 'loan_cart';

@Injectable({ providedIn: 'root' })
export class LoanCartService {
  constructor(private messageService: MessageService) {}

  private _items = signal<CreateLoanBookDTO[]>(this.loadFromStorage());

  readonly items = this._items.asReadonly();
  readonly count = computed(() => this._items().length);
  readonly isEmpty = computed(() => this.count() === 0);

  addBook(book: CreateLoanBookDTO): Boolean {
    const current = this._items();

    if (current.some((b) => b.bookId === book.bookId)) {
      this.messageService.add({
        severity: 'error',
        summary: 'Fout',
        detail: 'Boek staat al in de ontleenlijst',
        life: 3750,
      });
      return false;
    }
    this._items.set([...current, book]);
    this.persist();
    return true;
  }

  removeBook(bookId: number): void {
    this._items.set(this._items().filter((b) => b.bookId !== bookId));
    this.persist();
  }

  updateAmount(bookId: number, requestedAmount: number): void {
    this._items.set(
      this._items().map((b) => (b.bookId === bookId ? { ...b, requestedAmount } : b)),
    );
    this.persist();
  }

  clear(): void {
    this._items.set([]);
    localStorage.removeItem(CART_KEY);
  }

  private persist(): void {
    localStorage.setItem(CART_KEY, JSON.stringify(this._items()));
  }

  private loadFromStorage(): CreateLoanBookDTO[] {
    try {
      const raw = localStorage.getItem(CART_KEY);
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  }
}
