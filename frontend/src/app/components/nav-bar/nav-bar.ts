import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AsyncPipe,CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth';
import { LoanCartComponent } from '../loan-cart/loan-cart';
import { GamificationService } from '../../services/gamification.service';

@Component({
  selector: 'app-nav-bar',
  standalone: true,
  imports: [RouterModule, AsyncPipe, LoanCartComponent,CommonModule],
  templateUrl: './nav-bar.html',
  styleUrl: './nav-bar.css',
})
export class NavBarComponent implements OnInit {
  constructor(public authService: AuthService, public gamificationService: GamificationService) {}

  ngOnInit(): void {
    this.gamificationService.load();
  }

  logout(): void {
    this.authService.logout().subscribe();
  }
}
