import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AsyncPipe, CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth';
import { LoanCartComponent } from '../loan-cart/loan-cart';
import { PopoverModule } from 'primeng/popover';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-nav-bar',
  standalone: true,
  imports: [
    RouterModule,
    AsyncPipe,
    LoanCartComponent,
    CommonModule,
    PopoverModule,
    ButtonModule,
    SelectModule,
    FormsModule,
  ],
  templateUrl: './nav-bar.html',
  styleUrl: './nav-bar.css',
})
export class NavBarComponent {
  menuOpen = false;
  roles = ['ADMIN', 'BIBLIOTHEEKBEHEERDER', 'LEERKRACHT'];
  selectRole: string | undefined; // check what role user currently has

  changeRole() {
    // post update to controller
    if(this.selectRole)
      this.updateRole(this.selectRole);
    
  }

  setCurrentRole(userRoles: string[] | undefined): void {
    if (userRoles === undefined) return;
    if (userRoles.includes('BIBLIOTHEEKBEHEERDER')) {
      this.selectRole = 'BIBLIOTHEEKBEHEERDER';
    } else if (userRoles.includes('LEERKRACHT')) {
      this.selectRole = 'LEERKRACHT';
    } else {
      this.selectRole = 'ADMIN';
    }
    return;
  }

  updateRole(role: string) {
    this.http.post(`/api/session/${role}`, null).subscribe({
      next: () => this.router.navigate(['/']),
      error: (err) => console.error(err),
    });
  }

  constructor(
    public authService: AuthService,
    private http: HttpClient,
    private router: Router,
  ) {
    this.setCurrentRole(this.authService.currentUser?.roles);
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  logout(): void {
    this.authService.logout().subscribe();
  }
}
