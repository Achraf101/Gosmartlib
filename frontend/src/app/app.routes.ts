import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home';
import { SchoolAddComponent } from './components/school-add/school-add';
import { BookformComponent } from './components/bookform/bookform';
import { BookDetailPage } from './components/book-detail-page/book-detail-page';
import { CatalogueComponent } from './components/catalogue/catalogue';
import { LocationComponent } from './components/location/location';
import { LoginComponent } from './components/login/login';
import { authGuard } from './guards/auth.guard';
import { LocationDetailPageComponent } from './components/location-detail-page/location-detail-page';
import { FilterPage } from './components/filter-page/filter-page';
import { SchoolComponent } from './components/school/school';
import { FavoritePage } from './components/favorite-page/favorite-page';
import { AcceptDeclineReservationsPageComponent } from './components/accept-decline-reservations-page/accept-decline-reservations-page';
import { UserLoansPageComponent } from './components/user-loans-page/user-loans-page';
import { DashboardLibraryManager } from './components/dashboard-library-manager/dashboard-library-manager';
import { TeacherClassesPageComponent } from './components/teacher-classes-page/teacher-classes-page';
import { TeacherClassDetailPageComponent } from './components/teacher-class-detail-page/teacher-class-detail-page';
import { TeacherStudentReportPageComponent } from './components/teacher-student-report-page/teacher-student-report-page';
import { BookEdit } from './components/book-edit/book-edit';
import { ReviewModerationPageComponent } from './components/review-moderation-page/review-moderation-page';
import { PickUpPageComponent } from './components/pick-up-page/pick-up-page';
import { ReturnPageComponent } from './components/return-page/return-page';
import { SchoolEditComponent } from './components/school-edit/school-edit';
import { BegeleidingComponent } from './components/begeleiding/begeleiding';
import { PromotePageComponent } from './components/promote-page/promote-page';
import { AccountPage } from './components/account-page/account-page';
export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', redirectTo: 'startpagina', pathMatch: 'full' },
  { path: '', component: HomeComponent, canActivate: [authGuard] },
  { path: 'school', component: SchoolComponent, canActivate: [authGuard(['ADMIN', 'LEERKRACHT'])] },
  {
    path: 'school/leerkrachten/:schoolId',
    component: PromotePageComponent,
    canActivate: [authGuard(['ADMIN'])],
  },
  { path: 'school/toevoegen', component: SchoolAddComponent, canActivate: [authGuard] },
  { path: 'locatie/toevoegen/:schoolId', component: LocationComponent, canActivate: [authGuard] },
  { path: 'boek/toevoegen', component: BookformComponent, canActivate: [authGuard] },
  { path: 'catalogus/filter', component: FilterPage, canActivate: [authGuard] },
  { path: 'catalogus', component: CatalogueComponent, canActivate: [authGuard] },
  { path: 'boek/:id', component: BookDetailPage, canActivate: [authGuard] },
  { path: 'locatie', component: LocationDetailPageComponent, canActivate: [authGuard] },
  { path: 'begeleiding', component: BegeleidingComponent, canActivate: [authGuard] },
  { path: 'favorieten', component: FavoritePage, canActivate: [authGuard] },
  {
    path: 'uitleenaanvragen',
    component: AcceptDeclineReservationsPageComponent,
    canActivate: [authGuard],
  },
  { path: 'lijst/:token/delen', component: FavoritePage, canActivate: [authGuard] },
  { path: 'uitleningen', component: UserLoansPageComponent, canActivate: [authGuard] },
  {
    path: 'dashboard/bibliotheek-beheerder',
    component: DashboardLibraryManager,
    canActivate: [authGuard(['ADMIN', 'BIBLIOTHEEKBEHEERDER'])],
  },
  {
    path: 'reviews/moderatie',
    component: ReviewModerationPageComponent,
    canActivate: [authGuard(['BIBLIOTHEEKBEHEERDER'])],
  },
  {
    path: 'leerkracht/klassen',
    component: TeacherClassesPageComponent,
    canActivate: [authGuard(['LEERKRACHT'])],
  },
  {
    path: 'leerkracht/klassen/:id',
    component: TeacherClassDetailPageComponent,
    canActivate: [authGuard(['LEERKRACHT'])],
  },
  {
    path: 'leerkracht/leerlingen/:id',
    component: TeacherStudentReportPageComponent,
    canActivate: [authGuard(['LEERKRACHT'])],
  },
  { path: 'boek/:id/bewerken', component: BookEdit },
  { path: 'ophalen', component: PickUpPageComponent },
  { path: 'terugbrengen', component: ReturnPageComponent },
  {
    path: 'school/instellingen',
    component: SchoolEditComponent,
    canActivate: [authGuard(['BIBLIOTHEEKBEHEERDER'])],
  },
  { path: 'account', component: AccountPage, canActivate: [authGuard(['ADMIN'])] },

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
