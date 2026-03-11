import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { BookService } from '../../services/book-service';
import { BookCard, BookDetail, BookResult } from '../../models/book';
import { ImageModule } from 'primeng/image';
import { RatingModule } from 'primeng/rating';
import { FormsModule } from '@angular/forms';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-book-detail-page',
  imports: [ImageModule, RatingModule, FormsModule, NavBarComponent, RouterLink],
  templateUrl: './book-detail-page.html',
  styleUrl: './book-detail-page.css',
})
export class BookDetailPage implements OnInit {
  book?: BookDetail;
  bookId!: number;
  error = '';
  ratingValue = 0;
  ratingValueStars = 0;
  relatedBooks?: BookCard[];
  bookmarked = false;
  genresString = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly bookService: BookService,
    private readonly messageService: MessageService,
  ) {}

  ngOnInit(): void {
    // let bookId = parseInt(this.route.snapshot.paramMap.get('id')!);
    this.route.paramMap.subscribe((params) => {
      this.bookId = Number(params.get('id'));
      this.loadBook();
    });

    if (!this.bookId) return;
  }

  public bookMark() {
    this.bookmarked = !this.bookmarked;
  }

  scrollTop() {
    window.scrollTo({ top: 0, left: 0, behavior: 'smooth' });
  }

  public loadBook() {
    this.bookService.getById(this.bookId).subscribe({
      next: (book) => (this.book = book),
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Boek niet gevonden.',
          life: 3000,
        });
      },
    });

    this.ratingValue = Math.round((this.book?.rating_total! / this.book?.rating_count!) * 10) / 10;
    this.ratingValueStars = Math.round(this.ratingValue);

    this.genresString = (this.book?.genres || []).map((i) => i.name).join(', ');

    this.bookService.getRelated(this.bookId).subscribe({
      next: (relatedBooks) => (this.relatedBooks = relatedBooks),
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Probleem met het zoeken van gelijkaardige boeken.',
          life: 3750,
        });
      },
    });
  }
}
