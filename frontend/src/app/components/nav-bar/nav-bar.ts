import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AsyncPipe } from '@angular/common';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-nav-bar',
  standalone: true,
  imports: [RouterModule, AsyncPipe],
  templateUrl: './nav-bar.html',
  styleUrl: './nav-bar.css'
})
export class NavBarComponent {
  constructor(public authService: AuthService) {}

  logout(): void {
    this.authService.logout().subscribe();
  }
}
