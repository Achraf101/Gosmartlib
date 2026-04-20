import { Author } from './author';
import { Genre } from './genre';
import { Language } from './language';
import { Publisher } from './publisher';
import { Series } from './series';
import { BookType } from './book-type';

export interface BookBase {
  id: number;
  title: string;
  author: Author;
  cover?: string;
}

// optionally add fields later
export interface BookCard extends BookBase {}

// the item displayed after search
export interface BookResult extends BookBase {
  author_name: String;
  book_type: BookType;
  series?: Series;
  series_count?: number;
  language?: Language;
  published?: number;
  description?: string;
  genres: Genre[];
  fiction?: boolean;
  age_start?: number;
  age_end?: number;
  pages?: number;
  rating?: number;
  rating_count?: number;
}

export interface BookDetail extends BookBase {
  book_type: BookType;
  isbn?: string;
  series?: Series;
  series_count?: number;
  author_name: String;
  contributors?: Author[];
  publisher?: Publisher;
  genres: Genre[];
  description?: string;
  fiction: boolean;
  published?: number;
  cover?: string;
  language: Language;
  age_start?: number;
  age_end?: number;
  pages?: number;
  font_size?: string;
  rating_total: number;
  rating_count: number;
  school?: number;
  added: string;
}

export interface CreateBook {
  book_type: number;
  title: string;
  author: number;
  cover?: string;
  isbn?: string;
  series?: number;
  series_count?: number;
  contributors?: number[];
  publisher?: number;
  genres: number[];
  description?: string;
  fiction: boolean;
  published?: number;
  language: number;
  age_start?: number;
  age_end?: number;
  pages?: number;
  font_size?: string;
  school?: boolean;
  CLIB: string;
  Didactic_material: boolean;
}

// interface to search with filters
export interface BookFilter {
  type?: number[];
  genre?: number[];
  fiction?: boolean;
  language?: number;
  pages?: [number, number];
  series?: number[];
  author?: number[];
  age?: [number, number];
  published?: [number, number];
  publisher?: number[];
}
