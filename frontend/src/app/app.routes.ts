import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home';
import { SchoolComponent } from './components/school/school';
import { BookformComponent } from './components/bookform/bookform';
import { CatalogueComponent } from './components/catalogue/catalogue';

export const routes: Routes = [
  { path: '', redirectTo: 'startpagina', pathMatch: 'full' },
  { path: '', component: HomeComponent },
  { path: 'catalogus', component: CatalogueComponent },
  {
    path: 'boek',
    component: BookformComponent,
    children: [
      {
        path: 'toevoegen',
        component: BookformComponent,
      },
    ],
  },
  { path: 'school/toevoegen', component: SchoolComponent },
];
