export interface LocationBookDetail {
  id: number;
  location_id: number;
  book_id: number;
  book_title: string;
  author_name: string;
  book_cover: string;
  amount: number;
  current_amount: number;
}

export interface LocationStats {
  total_books: number;
  available_books: number;
}
