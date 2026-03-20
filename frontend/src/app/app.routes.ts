import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home';
import { SchoolComponent } from './components/school/school';
import { BookformComponent } from './components/bookform/bookform';
import { BookDetailPage } from './components/book-detail-page/book-detail-page';
import { CatalogueComponent } from './components/catalogue/catalogue';
import { CampusComponent } from './components/campus/campus';
import { AdminLoginComponent } from './components/admin-login/admin-login';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: AdminLoginComponent },
  { path: '', redirectTo: 'startpagina', pathMatch: 'full' },
  { path: '', component: HomeComponent, canActivate: [authGuard] },
  { path: 'school/toevoegen', component: SchoolComponent, canActivate: [authGuard] },
  { path: 'campus/toevoegen', component: CampusComponent, canActivate: [authGuard] },
  { path: 'boek/toevoegen', component: BookformComponent, canActivate: [authGuard] },
  { path: 'catalogus', component: CatalogueComponent, canActivate: [authGuard] },
  { path: 'boek/:id', component: BookDetailPage, canActivate: [authGuard] },
];
