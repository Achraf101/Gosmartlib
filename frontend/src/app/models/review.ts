export interface Review {
  id?: number;
  book_id?: number;
  rating: number;
  content: string;
  added?: string;
  user_id?: number;
}