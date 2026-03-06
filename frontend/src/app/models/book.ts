import { Author } from './author';
import { Genre } from './genre';
import { Language } from './language';
import { Publisher } from './publisher';
import { Series } from './series';

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
  series?: Series;
  series_count?: number;
  language?: Language;
  published?: number;
  description?: string;
  genre?: Genre[];
}

export interface BookDetail extends BookBase {
  isbn?: string;
  series?: Series;
  series_count?: number;
  contributors?: Author[];
  publisher?: Publisher;
  genre?: Genre[];
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
  school_id?: number;
  added: string;
}

export interface CreateBook {
  title: string;
  author?: number | null;
  // author?: number | null;
  cover?: string;
  isbn?: string;
  series?: number;
  series_count?: number;
  contributors?: number[];
  publisher?: number;
  genre?: number[];
  description?: string;
  fiction: boolean | null;
  // fiction: boolean | null;
  published?: number;
  language: number | null;
  // language: number | null;
  age_start?: number;
  age_end?: number;
  pages?: number;
  font_size?: string;
  school: boolean;
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
