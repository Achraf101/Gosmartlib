import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SectionService } from '../../services/section';
import { BookDetail } from '../../models/book';
import { Section } from '../../models/section';

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
  books: BookDetail[] = [];

  constructor(private sectionService: SectionService) {}

ngOnInit(): void {
  this.sectionService.getAll().subscribe({
    next: (sections) => {
      this.sections = sections;
      if (this.sections.length > 0) {
        this.selectSection(this.sections[0]);
      }
    },
    error: (err) => console.error('Fout bij laden van secties:', err)
  });
}

selectSection(section: Section): void {
  this.activeSection = section;
  this.sectionService.getBooksBySection(section.id).subscribe({
    next: (books) => this.books = books,
    error: (err) => console.error('Fout bij laden van boeken:', err)
  });
}
}