import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home';
import { SchoolAddComponent } from './components/school-add/school-add';
import { BookformComponent } from './components/bookform/bookform';
import { BookDetailPage } from './components/book-detail-page/book-detail-page';
import { CatalogueComponent } from './components/catalogue/catalogue';
import { CampusComponent } from './components/campus/campus';
import { CampusDetailPageComponent } from './components/campus-detail-page/campus-detail-page';
import { SchoolComponent } from './components/school/school';

export const routes: Routes = [
  { path: '', redirectTo: 'startpagina', pathMatch: 'full' },
  { path: '', component: HomeComponent },
  { path: 'school', component: SchoolComponent },
  { path: 'school/toevoegen', component: SchoolAddComponent },
  { path: 'campus/toevoegen/:schoolId', component: CampusComponent },
  { path: 'boek/toevoegen', component: BookformComponent },
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
