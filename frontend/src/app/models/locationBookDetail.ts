export interface LocationBookDetail {
  id: number;
  location_id: number;
  book_id: number;
  book_title: string;
  author_name: string;
  book_cover: string;
  amount: number;
  current_amount: number;
  /** ISBN (EAN-13) when the book has one; otherwise "LB{id}" (Code 128 label). */
  barcode: string;
}

export interface SchoolStats {
  total_books: number;
  available_books: number;
}
