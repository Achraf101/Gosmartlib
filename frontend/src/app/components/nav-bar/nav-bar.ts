import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AsyncPipe, CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth';
import { LoanCartComponent } from '../loan-cart/loan-cart';
import { PopoverModule } from 'primeng/popover';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-nav-bar',
  standalone: true,
  imports: [RouterModule, AsyncPipe, LoanCartComponent, CommonModule, PopoverModule, ButtonModule],
  templateUrl: './nav-bar.html',
  styleUrl: './nav-bar.css',
})
export class NavBarComponent {
  menuOpen = false;

  constructor(public authService: AuthService) {}

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  logout(): void {
    this.authService.logout().subscribe();
  }
}
