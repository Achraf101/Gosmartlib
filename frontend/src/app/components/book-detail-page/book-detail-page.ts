import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BookService } from '../../services/book-service';
import { BookCard, BookDetail } from '../../models/book';
import { ImageModule } from 'primeng/image';
import { RatingModule } from 'primeng/rating';
import { FormsModule } from '@angular/forms';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { MessageService } from 'primeng/api';
import { AccordionModule } from 'primeng/accordion';
import { BookCardComponent } from '../misc/book-card/book-card';
import { BookmarkedService } from '../../services/bookmarked-service';
import { CarouselModule } from 'primeng/carousel';
import { ProgressSpinner } from 'primeng/progressspinner';
import { DelayedLoader } from '../../utils/delayed-loader';

@Component({
  imports: [
    ImageModule,
    RatingModule,
    FormsModule,
    NavBarComponent,
    AccordionModule,
    BookCardComponent,
    CarouselModule,
    ProgressSpinner,
  ],
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
  isBookmarked = false;
  genresString = '';
  loading = new DelayedLoader();

  userId = 1;

  readonly placeholder = '/assets/no-cover.svg';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly bookService: BookService,
    private readonly messageService: MessageService,
    private readonly bookmarkedService: BookmarkedService,
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      this.bookId = Number(params.get('id'));
      this.loadBook();
    });
  }

  public toggleFavorite() {
    this.isBookmarked = !this.isBookmarked;
    this.bookmarkedService.toggleBookmarked(this.userId, this.bookId).subscribe({
      next: (isAdded) => {
        this.isBookmarked = isAdded;
      },
    });
  }

  scrollTop() {
    window.scrollTo({ top: 0, left: 0, behavior: 'smooth' });
  }

  public loadBook() {
    this.loading.start();
    this.error = '';
    this.book = undefined;

    this.bookService.getById(this.bookId).subscribe({
      next: (book) => {
        this.book = book;
        this.ratingValue =
          Math.round((this.book?.rating_total! / this.book?.rating_count!) * 10) / 10;
        this.ratingValueStars = Math.round(this.ratingValue);

        this.genresString = (this.book?.genres || []).map((i) => i.name).join(', ');

        this.bookmarkedService.isBookmarked(this.userId, this.bookId).subscribe({
          next: (result) => (this.isBookmarked = result),
        });

        this.bookService.getRelated(this.bookId).subscribe({
          next: (relatedBooks) => (this.relatedBooks = relatedBooks),
          error: () => {
            this.messageService.add({
              severity: 'error',
              summary: 'Fout',
              detail: 'Probleem met het zoeken van gelijkaardige boeken.',
              life: 3750,
            });
          },
        });

        this.loading.stop();
      },
      error: () => {
        this.error = 'Boek niet gevonden.';
        this.loading.stop();
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Boek niet gevonden.',
          life: 3000,
        });
      },
    });
  }
}
