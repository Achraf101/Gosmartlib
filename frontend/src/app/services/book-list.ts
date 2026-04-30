import { Injectable } from '@angular/core';
import { ApiService } from './api';
import { Observable } from 'rxjs';
import { BookList, BookListItem, SharedListResponse } from '../models/book-list';
import { SavedList } from '../models/saved-list';
import { BookResult } from '../models/book';

@Injectable({
  providedIn: 'root',
})
export class BookListService {
  private readonly endpoint = 'lists';

  constructor(private apiService: ApiService) {}

  createList(name: string): Observable<BookList> {
    return this.apiService.post<BookList>(`${this.endpoint}`, { name });
  }

  getMyLists(): Observable<BookList[]> {
    return this.apiService.get<BookList[]>(`${this.endpoint}`);
  }

  addBook(listId: number, bookId: number): Observable<BookListItem> {
    return this.apiService.post<BookListItem>(`${this.endpoint}/${listId}/books`, {
      bookId,
    });
  }

  removeBook(listId: number, bookId: number): Observable<void> {
    return this.apiService.delete<void>(`${this.endpoint}/${listId}/books/${bookId}`);
  }

  renameList(listId: number, name: string): Observable<BookList> {
    return this.apiService.put<BookList>(`${this.endpoint}/${listId}`, { name });
  }

  deleteList(listId: number): Observable<void> {
    return this.apiService.delete<void>(`${this.endpoint}/${listId}`);
  }

  getSharedList(token: string): Observable<SharedListResponse> {
    return this.apiService.get<SharedListResponse>(`${this.endpoint}/shared/${token}`);
  }

  getBooksInList(listId: number): Observable<BookResult[]> {
    return this.apiService.get<BookResult[]>(`${this.endpoint}/${listId}/books`);
  }

  saveList(token: string): Observable<SavedList> {
    return this.apiService.post<SavedList>(`saved-lists/${token}`, {});
  }

  getSavedLists(): Observable<SharedListResponse[]> {
    return this.apiService.get<SharedListResponse[]>(`saved-lists`);
  }

  getListsWithoutBook(bookId: number): Observable<BookList[]> {
    return this.apiService.get<BookList[]>(`${this.endpoint}/exclude-book/${bookId}`);
  }

  isSaved(bookListId: number): Observable<boolean> {
    return this.apiService.get<boolean>(`saved-lists/${bookListId}/exists`);
  }

  unsaveList(bookListId: number): Observable<void> {
    return this.apiService.delete<void>(`saved-lists/${bookListId}`);
  }

  generateShareToken(listId: number): Observable<BookList> {
    return this.apiService.post<BookList>(`${this.endpoint}/${listId}/share`, {});
  }

  removeShareToken(listId: number): Observable<BookList> {
    return this.apiService.delete<BookList>(`${this.endpoint}/${listId}/share`);
  }
}
