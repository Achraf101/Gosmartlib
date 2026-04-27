import { BookCard } from './book';

export interface CartBook extends BookCard {
  bookId: number;
  requestedAmount: number;
  receivedAmount: number;
  returnedAmount: number;
}
