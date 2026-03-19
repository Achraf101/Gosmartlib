import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home';
import { SchoolComponent } from './components/school/school';
import { BookformComponent } from './components/bookform/bookform';
import { BookDetailPage } from './components/book-detail-page/book-detail-page';
import { CatalogueComponent } from './components/catalogue/catalogue';
import { CampusComponent } from './components/campus/campus';
import { CampusDetailPageComponent } from './components/campus-detail-page/campus-detail-page';
import { FilterPage } from './components/filter-page/filter-page';

export const routes: Routes = [
  { path: '', redirectTo: 'startpagina', pathMatch: 'full' },
  { path: '', component: HomeComponent },
  { path: 'school/toevoegen', component: SchoolComponent },
  { path: 'campus/toevoegen', component: CampusComponent },
  { path: 'boek/toevoegen', component: BookformComponent },
  { path: 'catalogus/filter', component: FilterPage },
  { path: 'catalogus', component: CatalogueComponent },
  { path: 'boek/:id', component: BookDetailPage },
  { path: 'campus', component: CampusDetailPageComponent },

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
