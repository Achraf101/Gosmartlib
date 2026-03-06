import { Routes } from '@angular/router';
import { BookformComponent } from './components/bookform/bookform';

export const routes: Routes = [
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
];
