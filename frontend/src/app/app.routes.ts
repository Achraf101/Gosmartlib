import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home';
import { SchoolComponent } from './components/school/school';
import { BookformComponent } from './components/bookform/bookform';
import { CatalogueComponent } from './components/catalogue/catalogue';

export const routes: Routes = [
  { path: '', component: HomeComponent, pathMatch: 'full' },
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
