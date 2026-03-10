import { Routes } from '@angular/router';
import { SchoolComponent } from './components/school/school';
import { CampusComponent } from './components/campus/campus';

export const routes: Routes = [
  { path: 'school/toevoegen', component: SchoolComponent },
  { path: 'campus/toevoegen', component: CampusComponent },
];
