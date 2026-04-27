import { Injectable, signal, computed } from '@angular/core';
import { CartBook } from '../models/cartBook';

@Injectable({ providedIn: 'root' })
export class LoanCartService {
  private static readonly CART_KEY = 'loan_cart';

  private borrowLimit = signal<number>(100);
  private _items = signal<CartBook[]>(this.loadFromStorage());

  readonly items = this._items.asReadonly();
  readonly count = computed(() => this._items().length);
  readonly isEmpty = computed(() => this.count() === 0);

  setBorrowLimit(limit: number): void {
    if (limit < 1) throw new Error('INVALID_BORROW_LIMIT');
    this.borrowLimit.set(limit);
  }

  addBook(book: CartBook): void {
    const current = this._items();
    if (current.length >= this.borrowLimit()) throw new Error('BORROW_LIMIT_REACHED');
    if (current.some((b) => b.bookId === book.bookId)) throw new Error('ALREADY_IN_CART');
    this._items.set([...current, book]);
    this.persist();
  }

  removeBook(bookId: number): void {
    const exists = this._items().some((b) => b.bookId === bookId);
    if (!exists) throw new Error('BOOK_NOT_IN_CART');
    this._items.set(this._items().filter((b) => b.bookId !== bookId));
    this.persist();
  }

  updateAmount(bookId: number, requestedAmount: number): void {
    if (requestedAmount < 1) throw new Error('INVALID_AMOUNT');
    const exists = this._items().some((b) => b.bookId === bookId);
    if (!exists) throw new Error('BOOK_NOT_IN_CART');
    this._items.set(
      this._items().map((b) => (b.bookId === bookId ? { ...b, requestedAmount } : b)),
    );
    this.persist();
  }

  clear(): void {
    this._items.set([]);
    this.persist();
  }

  private persist(): void {
    localStorage.setItem(LoanCartService.CART_KEY, JSON.stringify(this._items()));
  }

  private loadFromStorage(): CartBook[] {
    try {
      const raw = localStorage.getItem(LoanCartService.CART_KEY);
      if (!raw) return [];
      const parsed = JSON.parse(raw);
      if (!Array.isArray(parsed)) return [];
      return parsed as CartBook[];
    } catch {
      return [];
    }
  }
}
