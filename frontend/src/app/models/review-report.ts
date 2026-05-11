export interface ReviewReport {
  id: number;
  review_id: number;
  reporter_user_id: number;
  note: string;
  created_at: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED';
  review_rating: number;
  review_content: string;
  review_added: string;
  review_user_id: number;
  book_id: number;
  book_title: string;
  review_username: string;
  reporter_username: string;
}
