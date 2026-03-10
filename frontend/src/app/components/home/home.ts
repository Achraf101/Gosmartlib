import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { Footer } from '../footer/footer';

interface Book {
  id: number;
  title: string;
  cover: string;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, NavBarComponent, Footer],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class HomeComponent implements OnInit {
  searchQuery: string = '';
  featuredBooks: Book[] = [];

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.http.get<Book[]>(`/api/book/featured`).subscribe({
      next: (books) => {
        this.featuredBooks = books;
      },
      error: (err) => {
        console.error('Fout bij laden van boeken:', err);
        this.featuredBooks = [
          {
            id: 1,
            title: 'Het TikTok Kamp',
            cover: 'https://standaarduitgeverij.be/cover/cover.php?isbn=9789493236554',
          },
          {
            id: 2,
            title: 'Het Leven Van Een Loser',
            cover: 'https://www.boekhandelpardoes.be/assets/uploads/2019/03/9789026125690.jpg',
          },
          {
            id: 3,
            title: 'Romantische Kerst',
            cover: 'https://media.s-bol.com/R6gLwk2lrzKY/550x825.jpg',
          },
          {
            id: 4,
            title: 'Harry Potter And The Goblet Of Fire',
            cover: 'https://covers.openlibrary.org/b/isbn/9780439139595-L.jpg',
          },
        ];
      },
    });
  }

  onSearch(event: Event) {
    event.preventDefault();
    if (!this.searchQuery.trim()) return;
    this.router.navigate(['/catalogus'], { queryParams: { q: this.searchQuery.trim() } });
  }

  onSearchInput(event: Event) {
    this.searchQuery = (event.target as HTMLInputElement).value;
  }
}
