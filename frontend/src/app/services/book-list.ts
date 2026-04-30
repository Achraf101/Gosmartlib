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

  createList(userId: number, name: string): Observable<BookList> {
    return this.apiService.post<BookList>(`${this.endpoint}/${userId}`, { name });
  }

  getMyLists(userId: number): Observable<BookList[]> {
    return this.apiService.get<BookList[]>(`${this.endpoint}/${userId}`);
  }

  addBook(userId: number, listId: number, bookId: number): Observable<BookListItem> {
    return this.apiService.post<BookListItem>(`${this.endpoint}/${userId}/${listId}/books`, {
      bookId,
    });
  }

  removeBook(userId: number, listId: number, bookId: number): Observable<void> {
    return this.apiService.delete<void>(`${this.endpoint}/${userId}/${listId}/books/${bookId}`);
  }

  renameList(userId: number, listId: number, name: string): Observable<BookList> {
    return this.apiService.put<BookList>(`${this.endpoint}/${userId}/${listId}`, { name });
  }

  deleteList(userId: number, listId: number): Observable<void> {
    return this.apiService.delete<void>(`${this.endpoint}/${userId}/${listId}`);
  }

  getSharedList(token: string): Observable<SharedListResponse> {
    return this.apiService.get<SharedListResponse>(`${this.endpoint}/shared/${token}`);
  }

  getBooksInList(userId: number, listId: number): Observable<BookResult[]> {
    return this.apiService.get<BookResult[]>(`${this.endpoint}/${userId}/${listId}/books`);
  }

  saveList(userId: number, token: string): Observable<SavedList> {
    return this.apiService.post<SavedList>(`saved-lists/${userId}/${token}`, {});
  }

  getSavedLists(userId: number): Observable<SharedListResponse[]> {
    return this.apiService.get<SharedListResponse[]>(`saved-lists/${userId}`);
  }

  getListsWithoutBook(userId: number, bookId: number): Observable<BookList[]> {
    return this.apiService.get<BookList[]>(`${this.endpoint}/${userId}/exclude-book/${bookId}`);
  }

  isSaved(userId: number, bookListId: number): Observable<boolean> {
    return this.apiService.get<boolean>(`saved-lists/${userId}/${bookListId}/exists`);
  }

  unsaveList(userId: number, bookListId: number): Observable<void> {
    return this.apiService.delete<void>(`saved-lists/${userId}/${bookListId}`);
  }

  generateShareToken(userId: number, listId: number): Observable<BookList> {
    return this.apiService.post<BookList>(`${this.endpoint}/${userId}/${listId}/share`, {});
  }

  removeShareToken(userId: number, listId: number): Observable<BookList> {
    return this.apiService.delete<BookList>(`${this.endpoint}/${userId}/${listId}/share`);
  }
}
