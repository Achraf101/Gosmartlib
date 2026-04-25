import { Author } from "./author";

export interface Bookmarked {
  id: number;
  book_id: number;
  title: string;
  cover: string;
  author: Author;
  added: string;
}