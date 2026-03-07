import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, forkJoin, Observable, of, switchMap } from 'rxjs';
import { BookService } from '../../services/book-service';
import { BookDetail, BookResult } from '../../models/book';
import { ImageModule } from 'primeng/image';
import { AsyncPipe } from '@angular/common';
import { RatingModule } from 'primeng/rating';
import { FormsModule } from '@angular/forms';
import { NavBarComponent } from "../nav-bar/nav-bar";

@Component({
  selector: 'app-book-detail-page',
  imports: [AsyncPipe, ImageModule, RatingModule, FormsModule, NavBarComponent, RouterLink],
  templateUrl: './book-detail-page.html',
  styleUrl: './book-detail-page.css',
})
export class BookDetailPage implements OnInit {
  book$!: Observable<BookDetail | null>;
  error = '';
  ratingValue = 0;
  ratingValueStars = 0;
  relatedBooks$!: Observable<BookResult[] | null>;
  bookmarked = false;
  genresString = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly service: BookService,
  ) {}

  ngOnInit(): void {
    this.book$ = this.route.paramMap.pipe(
      switchMap((params) => this.service.getById(Number(params.get('id')))),
      catchError(() => {
        this.error = 'Kon data niet laden';
        return of(null);
      }),
    );

    this.book$.forEach((book) => {
      this.ratingValue = Math.round((book?.rating_total! / book?.rating_count!) * 10) / 10;
      this.ratingValueStars = Math.round(this.ratingValue);

      this.genresString = (book?.genres || []).map((i) => i.name).join(', ');
    });

    const requests = Array.from({ length: 16 }, () => {
      const randomId = Math.ceil(Math.random() * 4);
      return this.service.getById(randomId);
    });

    this.relatedBooks$ = forkJoin(requests);
  }

  public bookMark() {
    this.bookmarked = !this.bookmarked;
  }
}
