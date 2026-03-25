import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { SectionService } from '../../services/section';
import { BookDetail } from '../../models/book';
import { Section } from '../../models/section';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-book-section',
  standalone: true,
  imports: [CommonModule, RouterModule, ToastModule, SelectModule, FormsModule, ButtonModule],
  templateUrl: './book-section.html',
  styleUrl: './book-section.css',
  providers: [MessageService]
})
export class BookSectionComponent implements OnInit {
  sections: Section[] = [];
  activeSection: Section | null = null;
  books: BookDetail[] = [];
  selectedGrade: number = 1;
  monthlyBook: BookDetail | null = null;

  grades = [
    { label: 'Graad 1', value: 1 },
    { label: 'Graad 2', value: 2 },
    { label: 'Graad 3', value: 3 },
  ];

  constructor(
    private sectionService: SectionService,
    private messageService: MessageService,
    private router: Router
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
          summary: 'Fout',
          detail: 'Fout bij laden van secties.',
          life: 3750,
        });
      }
    });
  }

  get isMonthlySection(): boolean {
    return this.activeSection?.title === 'Boek van de maand';
  }

  selectSection(section: Section): void {
    this.activeSection = section;
    if (this.isMonthlySection) {
      this.loadMonthlyBook();
    } else {
      this.sectionService.getBooksBySection(section.id).subscribe({
        next: (books) => this.books = books,
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Fout',
            detail: 'Fout bij laden van boeken.',
            life: 3750,
          });
        }
      });
    }
  }

  loadMonthlyBook(): void {
    if (!this.activeSection) return;
    this.sectionService.getBookBySectionAndGrade(this.activeSection.id, this.selectedGrade).subscribe({
      next: (book) => this.monthlyBook = book,
      error: () => this.monthlyBook = null
    });
  }

  onGradeChange(): void {
    this.loadMonthlyBook();
  }

  changeBookOfMonth(): void {
    this.router.navigate(['/catalogus'], {
      queryParams: {
        selectMode: true,
        sectionId: this.activeSection!.id,
        grade: this.selectedGrade
      }
    });
  }
}