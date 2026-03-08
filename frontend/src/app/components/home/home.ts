import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { Footer } from '../footer/footer';
import { BookSectionComponent } from '../book-section/book-section';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, NavBarComponent, Footer, BookSectionComponent],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class HomeComponent {
  searchQuery: string = '';

  onSearch(event: Event) {
    event.preventDefault();
    if (!this.searchQuery.trim()) return;
    console.log('Zoeken naar:', this.searchQuery);
  }

  onSearchInput(event: Event) {
    this.searchQuery = (event.target as HTMLInputElement).value;
  }
}