import { Component, Input } from '@angular/core';
import { BookCard } from '../../../models/book';
import { BookCoverInput } from '../../../models/bookCoverInput';

@Component({
  selector: 'b-cover',
  imports: [],
  templateUrl: './book-cover.html',
  styleUrl: './book-cover.css',
})
export class BookCover {
  @Input() bookInput!: BookCoverInput;

  get coverSrc(): string {
    const c = this.bookInput.cover;
    if (!c) return '';
    return /^https?:\/\//i.test(c) ? c : `/static/cover/${c}`;
  }
}
