import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home';
import { SchoolComponent } from './components/school/school';
import { BookformComponent } from './components/bookform/bookform';
import { BookDetailPage } from './components/book-detail-page/book-detail-page';

export const routes: Routes = [
  { path: '', redirectTo: 'startpagina', pathMatch: 'full' },
  { path: '', component: HomeComponent },
  { path: 'school/toevoegen', component: SchoolComponent },
  { path: 'boek/toevoegen', component: BookformComponent },
  { path: 'boek/:id', component: BookDetailPage },

  // {
  //   path: 'boek',
  //   component: BookformComponent,
  //   children: [
  //     {
  //       path: ':id',
  //       component: BookDetailPage,
  //     },
  //     {
  //       path: 'toevoegen',
  //       component: BookformComponent,
  //     },
  //   ],
  // },
];
