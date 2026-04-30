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
import { BookList } from '../../models/book-list';
import { BookListService } from '../../services/book-list';
import { Button } from 'primeng/button';

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
    Button,
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
  lists: BookList[] = [];
  showDropdown = false;

  userId = 1;

  readonly placeholder = '/assets/no-cover.svg';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly bookService: BookService,
    private readonly messageService: MessageService,
    private readonly bookmarkedService: BookmarkedService,
    private readonly bookListService: BookListService,
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      this.bookId = Number(params.get('id'));
      this.loadBook();
      this.showDropdown = false;
      this.bookListService.getListsWithoutBook(this.userId, this.bookId).subscribe((lists) => {
        this.lists = lists;
      });
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

  toggleDropdown() {
    this.showDropdown = !this.showDropdown;
  }

  addToList(listId: number) {
    if (!this.book) return;
    const list = this.lists.find((l) => l.id === listId);
    this.bookListService.addBook(this.userId, listId, this.book.id).subscribe({
      next: () => {
        this.showDropdown = false;
        this.messageService.add({
          severity: 'success',
          summary: 'succes',
          detail: `Boek succesvol toegevoegd aan lijst: "${list?.name}"`,
          life: 3000,
        });
      },
    });
    this.lists = this.lists.filter((l) => l.id !== listId);
  }
}
