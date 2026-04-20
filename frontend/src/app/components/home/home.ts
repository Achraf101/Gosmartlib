import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { BookSectionComponent } from '../book-section/book-section';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, NavBarComponent, BookSectionComponent],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class HomeComponent {
  searchQuery: string = '';

  constructor(private router: Router) {}

  onSearch(event: Event) {
    event.preventDefault();
    if (!this.searchQuery.trim()) return;
    this.router.navigate(['/catalogus'], { queryParams: { q: this.searchQuery.trim() } });
  }

  onSearchInput(event: Event) {
    this.searchQuery = (event.target as HTMLInputElement).value;
  }
}
