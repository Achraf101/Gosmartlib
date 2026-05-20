import { Author } from './author';
import { Genre } from './genre';
import { Language } from './language';
import { Publisher } from './publisher';
import { Series } from './series';
import { BookType } from './book-type';
import { Theme } from './theme';

export interface BookBase {
  id: number;
  title: string;
  author: Author;
  cover?: string;
}

export interface BookCard extends BookBase {
  author_name: string;
}

export interface BookResult extends BookBase {
  author_name: string;
  book_type: BookType;
  series_id?: number;
  series_name?: string;
  series_number?: number;
  language?: Language;
  published?: number;
  description?: string;
  genres: Genre[];
  themes?: Theme[];
  fiction?: boolean;
  clib?: string;
  pages?: number;
  rating?: number;
  rating_count?: number;
}

export interface BookDetail extends BookBase {
  author_name: string;
  book_type: BookType;
  isbn?: string;
  series_name?: string;
  series_number?: number;
  contributors?: Author[];
  publisher?: Publisher;
  genres: Genre[];
  themes?: Theme[];
  description?: string;
  fiction: boolean;
  published?: number;
  cover?: string;
  language: Language;
  clib?: string;
  pages?: number;
  font_size?: string;
  rating: number;
  rating_total: number;
  rating_count: number;
  school?: number;
  added: string;
  didactic: boolean;
}

export interface CreateBook {
  book_type: number;
  title: string;
  author: number;
  cover?: string;
  cover_url?: string;
  isbn?: string;
  series?: number;
  series_count?: number;
  contributors?: number[];
  publisher?: number;
  genres: number[];
  themes?: Theme[];
  description?: string;
  fiction: boolean;
  published?: number;
  language: number;
  clib?: string;
  pages?: number;
  font_size?: string;
  school?: boolean;
  didactic: boolean;
}

export interface BookLookupDTO {
  title: string | null;
  author_name: string | null;
  publisher_name: string | null;
  description: string | null;
  published_year: number | null;
  pages: number | null;
  language_code: string | null;
  contributors: string[] | null;
  isbn: string;
  cover_url: string | null;
}

export interface BookFilter {
  type?: number[];
  didactic?: boolean;
  genre?: number[];
  theme?: Theme[];
  fiction?: boolean;
  language?: number;
  pages?: [number, number];
  series?: number[];
  author?: number[];
  clibs?: string[];
  published?: [number, number];
  publisher?: number[];
  location?: number;
}
