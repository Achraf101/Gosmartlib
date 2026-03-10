import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SectionService } from '../../services/section';
import { BookDetail } from '../../models/book';
import { Section } from '../../models/section';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

@Component({
  selector: 'app-book-section',
  standalone: true,
  imports: [CommonModule, RouterModule, ToastModule],
  templateUrl: './book-section.html',
  styleUrl: './book-section.css',
  providers: [MessageService]
})
export class BookSectionComponent implements OnInit {
  sections: Section[] = [];
  activeSection: Section | null = null;
  books: BookDetail[] = [];

  constructor(
    private sectionService: SectionService,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    this.sectionService.getAll().subscribe({
      next: (sections) => {
        this.sections = sections;
        if (this.sections.length > 0) {
          this.selectSection(this.sections[0]);
        }
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Fout bij laden van secties.',
          life: 3750,
        });
      }
    });
  }

  selectSection(section: Section): void {
    this.activeSection = section;
    this.sectionService.getBooksBySection(section.id).subscribe({
      next: (books) => this.books = books,
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Fout bij laden van boeken.',
          life: 3750,
        });
      }
    });
  }
}