export interface CampusBookDetail {
  id: number;
  campus_id: number;
  book_id: number;
  book_title: string;
  author_name: string;
  book_cover: string;
  amount: number;
  current_amount: number;
  location: string;
}

export interface CampusStats {
  total_books: number;
  available_books: number;
}
