export type CopyStatus = 'AVAILABLE' | 'DAMAGED';

export interface BookCopyDetail {
  id: number;
  accession_id: string;
  status: CopyStatus;
  location_book_id: number;
  book_id: number;
  book_title: string;
  author_name: string;
  book_cover: string;
  isbn: string;
  location_id: number;
  location_name: string;
  amount: number;
  current_amount: number;
}
