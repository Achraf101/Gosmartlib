import { Component, OnInit } from '@angular/core';
import { BookmarkedService } from '../../services/bookmarked-service';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { ProgressSpinner } from 'primeng/progressspinner';
import { BookCardComponent } from '../misc/book-card/book-card';
import { CarouselModule } from 'primeng/carousel';
import { DelayedLoader } from '../../utils/delayed-loader';
import { BookListService } from '../../services/book-list';
import { FormsModule } from '@angular/forms';
import { BookList, SharedListResponse } from '../../models/book-list';
import { BookCard, BookResult } from '../../models/book';
import { Button } from 'primeng/button';
import { Divider } from 'primeng/divider';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ConfirmPopupModule } from 'primeng/confirmpopup';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';

@Component({
  selector: 'app-bookmarked',
  imports: [
    NavBarComponent,
    ProgressSpinner,
    BookCardComponent,
    CarouselModule,
    FormsModule,
    Button,
    Divider,
    ConfirmPopupModule,
    ToastModule,
    TooltipModule
  ],
  providers: [ConfirmationService],
  templateUrl: './favorite-page.html',
  styleUrl: './favorite-page.css',
})
export class FavoritePage implements OnInit {
  bookmarked: BookCard[] = [];
  loading = new DelayedLoader();
  listName = '';
  lists: BookList[] = [];
  listItems: Map<number, BookResult[]> = new Map();
  savedListItems: Map<number, BookResult[]> = new Map();
  isEditing = false;
  showConfirmPopup = false;
  pendingToken: string | null = null;
  pendingList: SharedListResponse | null = null;
  searchToken = '';
  savedLists: SharedListResponse[] = [];

  carouselResponsiveOptions = [
    { breakpoint: '1024px', numVisible: 3, numScroll: 2 },
    { breakpoint: '768px', numVisible: 2, numScroll: 1 },
    { breakpoint: '480px', numVisible: 1, numScroll: 1 },
  ];

  private userId = 1;

  constructor(
    private bookmarkedService: BookmarkedService,
    private router: Router,
    private bookListService: BookListService,
    private route: ActivatedRoute,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.paramMap.get('token');
    if (token) {
      this.pendingToken = token;
      this.bookListService.getSharedList(token).subscribe({
        next: (data) => {
          this.pendingList = data;
          this.showConfirmPopup = true;
        },
        error: () =>
          this.messageService.add({
            severity: 'error',
            summary: 'Fout',
            detail: 'Lijst niet gevonden',
            life: 3000,
          }),
      });
    }

    this.loading.start();
    this.bookmarkedService.getBookmarked(this.userId).subscribe({
      next: (data) => {
        this.bookmarked = data;
      },
    });

    this.bookListService.getMyLists().subscribe({
      next: (data) => {
        this.lists = data;
        data.forEach((d) => {
          this.bookListService.getBooksInList(d.id).subscribe((items) => {
            this.listItems.set(d.id, items);
          });
        });
      },
    });

    this.loadSavedLists();

    this.loading.stop();
  }

  toggleBookmarked(bookId: number) {
    this.bookmarkedService.toggleBookmarked(this.userId, bookId).subscribe({
      next: () => {
        this.bookmarked = this.bookmarked.filter((f) => f.id !== bookId);
      },
    });
  }

  onBookClick(bookId: number): void {
    this.router.navigate(['/boek', bookId]);
  }

  deleteFromList(listId: number, bookId: number) {
    this.bookListService.removeBook(listId, bookId).subscribe({
      next: () => {
        const current = this.listItems.get(listId) ?? [];
        this.listItems.set(
          listId,
          current.filter((book) => book.id !== bookId),
        );
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'Boek succesvol verwijdert',
          life: 3000,
        });
      },
    });
  }

  deleteList(listId: number) {
    this.bookListService.deleteList(listId).subscribe({
      next: () => {
        this.lists = this.lists.filter((list) => list.id !== listId);
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'Lijst succesvol verwijdert',
          life: 3000,
        });
      },
    });
  }

  startEditing() {
    this.isEditing = true;
  }

  stopEditing() {
    this.isEditing = false;
  }

  renameList(listId: number, listName: string) {
    this.bookListService.renameList(listId, listName).subscribe();
    this.stopEditing();
  }

  submitList() {
    if (!this.listName.trim()) return;
    this.bookListService.createList(this.listName).subscribe({
      next: (list) => {
        this.lists.push(list);
        this.listName = '';
      },
      error: (err) => console.error(err),
    });
  }

  shareList(list: BookList) {
    if (list.share_token) {
      const link = `${window.location.origin}/lijst/${list.share_token}/delen`;
      navigator.clipboard.writeText(link);
    } else {
      this.bookListService.generateShareToken(list.id).subscribe({
        next: (updated) => {
          list.share_token = updated.share_token;
          const link = `${window.location.origin}/lijst/${updated.share_token}/delen`;
          navigator.clipboard.writeText(link);
        },
      });
    }
    this.messageService.add({
      severity: 'success',
      summary: 'Succes',
      detail: 'link gecopiëerd',
      life: 3000,
    });
  }

  loadSavedLists() {
    this.bookListService.getSavedLists().subscribe({
      next: (data) => {
        this.savedLists = data;
        data.forEach((saved) => {
          this.savedListItems.set(saved.list.id, saved.books);
          this.savedListItems = new Map(this.savedListItems);
        });
      },
    });
  }

  confirmSave() {
    if (!this.pendingToken) return;
    this.bookListService.saveList(this.pendingToken).subscribe({
      next: () => {
        this.loadSavedLists();
        this.showConfirmPopup = false;
        this.pendingToken = null;
        this.pendingList = null;
        this.router.navigate(['/favorieten']);
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'lijst succesvol toegevoegd',
          life: 3000,
        });
      },
      error: (err) => {
        if (err.status === 409) {
          this.messageService.add({
            severity: 'error',
            summary: 'Fout',
            detail: 'Je hebt deze lijst al in je gedeelde lijsten staan!',
            life: 3000,
          });
          this.router.navigate(['/favorieten']);
        }
      },
    });
  }

  cancelSave() {
    this.showConfirmPopup = false;
    this.pendingToken = null;
    this.pendingList = null;
    this.router.navigate(['/favorieten']);
  }

  deleteSavedList(bookListId: number) {
    this.bookListService.unsaveList(bookListId).subscribe({
      next: () => {
        this.savedLists = this.savedLists.filter((s) => s.list.id !== bookListId);
        this.messageService.add({
          severity: 'success',
          summary: 'Succes',
          detail: 'Lijst succesvol verwijdert',
          life: 3000,
        });
      },
    });
  }

  confirmDeleteList(event: Event, listId: number, listName: string) {
    this.confirmationService.confirm({
      target: event.target as EventTarget,
      message: `Ben je zeker dat je ${listName} wilt verwijderen?`,
      accept: () => this.deleteList(listId),
    });
  }

  confirmDeleteBook(
    event: Event,
    listId: number,
    bookId: number,
    listName: string,
    bookName: string,
  ) {
    this.confirmationService.confirm({
      target: event.target as EventTarget,
      message: `${bookName} verwijderen uit ${listName}?`,
      accept: () => this.deleteFromList(listId, bookId),
    });
  }

  confirmDeleteSavedList(event: Event, bookListId: number, listName: string) {
    this.confirmationService.confirm({
      target: event.target as EventTarget,
      message: `${listName} verwijderen uit je gedeelde lijsten?`,
      accept: () => this.deleteSavedList(bookListId),
    });
  }

  confirmShareList(event: Event, list: BookList) {
    this.confirmationService.confirm({
      target: event.target as EventTarget,
      message: `${list.name} delen`,
      accept: () => this.shareList(list),
    });
  }

  confirmStopSharing(event: Event, list: BookList) {
    this.confirmationService.confirm({
      target: event.target as EventTarget,
      message: `Stoppen met delen van ${list.name}?`,
      accept: () => this.stopSharing(list),
    });
  }

  stopSharing(list: BookList) {
    this.bookListService.removeShareToken(list.id).subscribe({
      next: (updated) => {
        list.share_token = updated.share_token;
      },
    });
  }

  AddToList(list: BookList) {
    this.router.navigate(['/catalogus'], {
      queryParams: {
        selectMode: true,
        listId: list.id,
      },
    });
  }
}
