import { Component, Input } from '@angular/core';
import { BookCard } from '../../../models/book';
import { RouterLink } from '@angular/router';
import { BookCover } from '../book-cover/book-cover';

@Component({
  selector: 'b-card',
  imports: [RouterLink, BookCover],
  templateUrl: './book-card.html',
  styleUrl: './book-card.css',
})
export class BookCardComponent {
  @Input() bookCard!: BookCard;
  @Input() size?: Size;

  scrollTop() {
    window.scrollTo({ top: 0, left: 0, behavior: 'smooth' });
  }
}

type Size = 'small' | 'medium' | 'large';
