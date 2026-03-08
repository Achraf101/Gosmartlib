import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

interface Book {
  id: number;
  title: string;
  cover: string;
  description: string | null;
  pages: number | null;
  author: { name: string } | null;
  genres: { name: string }[] | null;
}

@Component({
  selector: 'app-book-section',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './book-section.html',
  styleUrl: './book-section.css',
})
export class BookSectionComponent implements OnInit {
  activeTab: 'featured' | 'monthly' = 'featured';
  featuredBooks: Book[] = [];
  monthlyBook: Book | null = null;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadFeaturedBooks();
    this.loadMonthlyBook();
  }

  loadFeaturedBooks(): void {
  this.http.get<Book[]>('http://localhost:8080/api/book/featured').subscribe({
    next: (books) => this.featuredBooks = books,
    error: (err) => console.error('Fout bij laden van boeken:', err)
  });
}

loadMonthlyBook(): void {
  this.http.get<Book>('http://localhost:8080/api/book/monthly').subscribe({
    next: (book) => this.monthlyBook = book,
    error: (err) => console.error('Fout bij laden van boek van de maand:', err)
  });
}

  setTab(tab: 'featured' | 'monthly'): void {
    this.activeTab = tab;
  }
}