import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { catchError, forkJoin, Observable, of, switchMap } from 'rxjs';
import { BookService } from '../../services/book-service';
import { BookDetail, BookResult } from '../../models/book';
import { ImageModule } from 'primeng/image';
import { AsyncPipe } from '@angular/common';
import { RatingModule } from 'primeng/rating';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-book-detail-page',
  imports: [AsyncPipe, ImageModule, RatingModule, FormsModule],
  templateUrl: './book-detail-page.html',
  styleUrl: './book-detail-page.css',
})
export class BookDetailPage implements OnInit {
  book$!: Observable<BookDetail | null>;
  error = '';
  ratingValue = 0;
  relatedBooks$!: Observable<BookResult[] | null>;
  bookmarked = false;

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
      this.ratingValue = book?.rating_total! / book?.rating_count!;
    });

    const requests = Array.from({ length: 8 }, () => {
      const randomId = Math.floor(Math.random() * 1);
      return this.service.getById(randomId);
    });

    this.relatedBooks$ = forkJoin(requests);
  }

  public bookMarkClicked() {
    this.bookmarked = !this.bookmarked;
  }
}
