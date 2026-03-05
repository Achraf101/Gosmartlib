import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home';
import { SchoolComponent } from './components/school/school';

export const routes: Routes = [
  { path: '', redirectTo: 'startpagina', pathMatch: 'full' },
  { path: 'startpagina', component: HomeComponent },
  { path: 'school/toevoegen', component: SchoolComponent }
];



