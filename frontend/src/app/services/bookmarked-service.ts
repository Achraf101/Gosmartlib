import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { map, Observable } from 'rxjs';
import { Bookmarked } from '../models/bookmarked';
import { BookCard } from '../models/book';

@Injectable({
  providedIn: 'root',
})
export class BookmarkedService {
  private endpoint = 'bookmarked';

  constructor(private apiService: ApiService) {}

  getBookmarked(userId: number): Observable<BookCard[]> {
    return this.apiService.get<Bookmarked[]>(`${this.endpoint}/${userId}`).pipe(
      map((books) =>
        books.map((book) => ({
          id: book.book_id,
          title: book.title,
          cover: book.cover,
          author: book.author,
          author_name: book.author.name,
        })),
      ),
    );
  }

  isBookmarked(userId: number, bookId: number): Observable<boolean> {
    return this.apiService.get<boolean>(`${this.endpoint}/${userId}/${bookId}`);
  }

  toggleBookmarked(userId: number, bookId: number): Observable<boolean> {
    return this.apiService.post<boolean>(`${this.endpoint}/${userId}/${bookId}`, {});
  }
}
