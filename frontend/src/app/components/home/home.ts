import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { NavBarHomeComponent } from '../nav-bar-home/nav-bar-home';

interface Book {
  id: number;
  title: string;
  cover: string;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, NavBarHomeComponent],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class HomeComponent implements OnInit {
  searchQuery: string = '';
  featuredBooks: Book[] = [];
  isLoading: boolean = true;
  error: string | null = null;

  private coverColors = ['#2c3e6b', '#e8d5a3', '#8b1a1a', '#1a3a1a'];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<Book[]>(`http://localhost:8080/api/book/featured`).subscribe({
      next: (books) => {
        this.featuredBooks = books;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Fout bij laden van boeken:', err);
        this.error = 'Boeken konden niet worden geladen.';
        this.isLoading = false;
      },
    });
  }

  getCoverColor(index: number): string {
    return this.coverColors[index % this.coverColors.length];
  }

  onSearch(event: Event) {
    event.preventDefault();
    if (!this.searchQuery.trim()) return;
    console.log('Zoeken naar:', this.searchQuery);
  }

  onSearchInput(event: Event) {
    this.searchQuery = (event.target as HTMLInputElement).value;
  }
}
