import { Component, OnInit } from '@angular/core';
import { Bookmarked } from '../../models/bookmarked';
import { BookmarkedService } from '../../services/bookmarked-service';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { Router } from '@angular/router';
import { ProgressSpinner } from 'primeng/progressspinner';
import { BookCardComponent } from '../misc/book-card/book-card';
import { CarouselModule } from "primeng/carousel";

@Component({
  selector: 'app-bookmarked',
  imports: [NavBarComponent, ProgressSpinner, BookCardComponent, CarouselModule],
  templateUrl: './favorite-page.html',
  styleUrl: './favorite-page.css',
})
export class FavoritePage implements OnInit {
  bookmarked: Bookmarked[] = [];
  loading = true;
  readonly placeholder = '/assets/no-cover.svg';

  // TODO: replace with actual logged in user id once auth is done
  private userId = 1;

  constructor(
    private bookmarkedService: BookmarkedService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.bookmarkedService.getBookmarked(this.userId).subscribe({
      next: (data) => {
        this.bookmarked = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  toggleBookmarked(bookId: number) {
    this.bookmarkedService.toggleBookmarked(this.userId, bookId).subscribe({
      next: () => {
        this.bookmarked = this.bookmarked.filter((f) => f.book_id !== bookId);
      },
    });
  }

  onBookClick(bookId: number): void {
    this.router.navigate(['/boek', bookId]);
  }
}
