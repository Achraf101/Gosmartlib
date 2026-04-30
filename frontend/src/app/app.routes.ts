import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home';
import { SchoolAddComponent } from './components/school-add/school-add';
import { BookformComponent } from './components/bookform/bookform';
import { BookDetailPage } from './components/book-detail-page/book-detail-page';
import { CatalogueComponent } from './components/catalogue/catalogue';
import { CampusComponent } from './components/campus/campus';
import { LoginComponent } from './components/login/login';
import { authGuard } from './guards/auth.guard';
import { CampusDetailPageComponent } from './components/campus-detail-page/campus-detail-page';
import { FilterPage } from './components/filter-page/filter-page';
import { SchoolComponent } from './components/school/school';
import { FavoritePage } from './components/favorite-page/favorite-page';
import { AcceptDeclineReservationsPageComponent } from './components/accept-decline-reservations-page/accept-decline-reservations-page';
import { UserLoansPageComponent } from './components/user-loans-page/user-loans-page';
export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', redirectTo: 'startpagina', pathMatch: 'full' },
  { path: '', component: HomeComponent, canActivate: [authGuard] },
  { path: 'school', component: SchoolComponent, canActivate: [authGuard(['ADMIN', 'LEERKRACHT'])] },
  { path: 'school/toevoegen', component: SchoolAddComponent, canActivate: [authGuard] },
  { path: 'campus/toevoegen/:schoolId', component: CampusComponent, canActivate: [authGuard] },
  { path: 'boek/toevoegen', component: BookformComponent, canActivate: [authGuard] },
  { path: 'catalogus/filter', component: FilterPage, canActivate: [authGuard] },
  { path: 'catalogus', component: CatalogueComponent, canActivate: [authGuard] },
  { path: 'boek/:id', component: BookDetailPage, canActivate: [authGuard] },
  { path: 'campus', component: CampusDetailPageComponent, canActivate: [authGuard] },
  { path: 'favorieten', component: FavoritePage, canActivate: [authGuard] },
  {
    path: 'uitleenaanvragen',
    component: AcceptDeclineReservationsPageComponent,
    canActivate: [authGuard],
  },
  { path: 'lijst/:token/delen', component: FavoritePage, canActivate: [authGuard] },
  { path: 'uitleningen', component: UserLoansPageComponent, canActivate: [authGuard] },
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
