import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';

interface Book {
  id: number;
  title: string;
  cover: string;
  description: string | null;
  pages: number | null;
  author: { name: string } | null;
  genres: { name: string }[] | null;
}

interface Section {
  id: number;
  title: string;
  ranking: number;
  hidden: boolean;
}

@Component({
  selector: 'app-book-section',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './book-section.html',
  styleUrl: './book-section.css',
})
export class BookSectionComponent implements OnInit {
  sections: Section[] = [];
  activeSection: Section | null = null;
  books: Book[] = [];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<Section[]>('http://localhost:8080/api/section').subscribe({
      next: (sections) => {
        this.sections = sections.filter(s => !s.hidden).sort((a, b) => a.ranking - b.ranking);
        if (this.sections.length > 0) {
          this.selectSection(this.sections[0]);
        }
      },
      error: (err) => console.error('Fout bij laden van secties:', err)
    });
  }

  selectSection(section: Section): void {
    this.activeSection = section;
    this.http.get<Book[]>(`http://localhost:8080/api/section/${section.id}/books`).subscribe({
      next: (books) => this.books = books,
      error: (err) => console.error('Fout bij laden van boeken:', err)
    });
  }
}