import { Routes } from '@angular/router';
import { BookDetailPage } from './components/book-detail-page/book-detail-page';

export const routes: Routes = [
  {
    path: 'book/:id',
    component: BookDetailPage,
  },
];
