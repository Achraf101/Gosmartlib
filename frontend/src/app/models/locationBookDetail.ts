export interface LocationBookDetail {
  id: number;
  location_id: number;
  book_id: number;
  book_title: string;
  author_name: string;
  book_cover: string;
  amount: number;
  current_amount: number;
  new_accession_ids?: string[];
}

export interface SchoolStats {
  total_books: number;
  available_books: number;
}

export interface LocationAvailability {
  location_book_id: number;
  location_id: number;
  location_name: string;
  amount: number;
  current_amount: number;
  damaged_count: number;
  noted_count: number;
}
