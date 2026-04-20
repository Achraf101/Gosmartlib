import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { Footer } from '../footer/footer';
import { BookSectionComponent } from '../book-section/book-section';
import { SearchBar } from '../misc/search-bar/search-bar';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, NavBarComponent, Footer, BookSectionComponent, SearchBar],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class HomeComponent {
  searchQuery: string = '';

  constructor(private router: Router) {}

  onSearch(query: string) {
    if (!query.trim()) return;
    this.searchQuery = query.trim();
    this.router.navigate(['/catalogus'], { queryParams: { q: this.searchQuery.trim() } });
  }
}
