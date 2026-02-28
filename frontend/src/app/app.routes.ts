import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home';

export const routes: Routes = [
  { path: '', redirectTo: 'startpagina', pathMatch: 'full' },
  { path: 'startpagina', component: HomeComponent },
];