import { Component, Input } from '@angular/core';
import { BookCard } from '../../../models/book';

@Component({
  selector: 'b-cover',
  imports: [],
  templateUrl: './book-cover.html',
  styleUrl: './book-cover.css',
})
export class BookCover {
  @Input() bookCard!: BookCard;

  get coverSrc(): string {
    const c = this.bookCard.cover;
    if (!c) return '';
    return /^https?:\/\//i.test(c) ? c : `/static/cover/${c}`;
  }
}
