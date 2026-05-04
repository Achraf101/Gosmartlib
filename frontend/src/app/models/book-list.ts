import { BookResult } from './book';

export interface BookList {
  id: number;
  owner_id: number;
  name: string;
  share_token: string;
  created_at: string;
}

export interface BookListItem {
  id: number;
  book_list_id: number;
  books: BookResult[];
  added_at: string;
}

export interface CreateListRequest {
  name: string;
}

export interface SharedListResponse {
  list: BookList;
  books: BookResult[];
}
