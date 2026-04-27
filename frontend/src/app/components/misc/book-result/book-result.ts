import { Component, EventEmitter, Input, Output } from '@angular/core';
import { BookResult as BookResultType } from '../../../models/book';
import { Tag } from 'primeng/tag';
import { Divider } from 'primeng/divider';
import { BookCover } from '../book-cover/book-cover';
import { DecimalPipe } from '@angular/common';


@Component({
  selector: 'b-result',
  imports: [Tag, Divider, BookCover,DecimalPipe],
  templateUrl: './book-result.html',
  styleUrl: './book-result.css',
})
export class BookResult {
  @Input() book!: BookResultType;

  @Output() onBookClick = new EventEmitter<BookResultType>();

  handleClick() {
    this.onBookClick.emit(this.book);
  }
}
