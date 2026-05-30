import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { SectionService } from '../../services/section';
import { BookDetail } from '../../models/book';
import { Section } from '../../models/section';
import { ToastModule } from 'primeng/toast';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { FormsModule } from '@angular/forms';
import { BookCardComponent } from '../misc/book-card/book-card';
import { BookResult } from '../misc/book-result/book-result';
import { DelayedLoader } from '../../utils/delayed-loader';
import { SchoolSettings } from '../../models/school-settings';
import { SchoolSettingsService } from '../../services/school-settings';
import { isVisible } from '../../models/school-settings';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-book-section',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ToastModule,
    SelectModule,
    FormsModule,
    ButtonModule,
    ProgressSpinnerModule,
    BookCardComponent,
    BookResult,
  ],
  templateUrl: './book-section.html',
  styleUrl: './book-section.css',
  providers: [],
})
export class BookSectionComponent implements OnInit {
  sections: Section[] = [];
  activeSection: Section | null = null;
  books: BookDetail[] = [];
  selectedGrade: number = 1;
  monthlyBook: BookDetail | null = null;
  loading = new DelayedLoader();
  schoolSettings: SchoolSettings | null = null;
  isVisible = isVisible;

  grades = [
    { label: 'Graad 1', value: 1 },
    { label: 'Graad 2', value: 2 },
    { label: 'Graad 3', value: 3 },
  ];

  constructor(
    private sectionService: SectionService,
    private router: Router,
    private schoolSettingsService: SchoolSettingsService,
    public authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.loading.start();

    this.sectionService.getAll(this.schoolId).subscribe({
      next: (sections) => {
        this.sections = sections;
        if (this.sections.length > 0) {
          this.selectSection(this.visibleSections[0]);
        } else {
          this.loading.stop();
        }
      },
      error: () => {
        this.loading.stop();
      },
    });

    this.schoolSettingsService.getSettings().subscribe({
      next: (settings) => {
        this.schoolSettings = settings;
        if (
          this.visibleSections.length > 0 &&
          !this.visibleSections.includes(this.activeSection!)
        ) {
          this.selectSection(this.visibleSections[0]);
        }
      },
      error: () => (this.schoolSettings = null),
    });
  }

  private get schoolId(): number {
    return this.authService.currentUser?.schoolId ?? 0;
  }

  get isMonthlySection(): boolean {
    return this.activeSection?.title === 'Boek van de maand';
  }

  get visibleSections(): Section[] {
    return this.sections.filter((s) => {
      if (s.title === 'Boek van de maand')
        return this.schoolSettings ? isVisible(this.schoolSettings, 'HOME', 'MONTHLY_BOOK') : true;
      if (s.title === 'In de kijker')
        return this.schoolSettings ? isVisible(this.schoolSettings, 'HOME', 'IN_SPOTLIGHT') : true;
      return true;
    });
  }

  selectSection(section: Section): void {
    this.activeSection = section;
    this.loading.start();
    if (this.isMonthlySection) {
      this.loadMonthlyBook();
    } else {
      this.sectionService.getBooksBySection(section.id).subscribe({
        next: (books) => {
          this.books = books;
          this.loading.stop();
        },
        error: () => {
          this.loading.stop();
        },
      });
    }
  }

  loadMonthlyBook(): void {
    if (!this.activeSection) return;
    this.loading.start();
    this.sectionService
      .getBookBySectionAndGrade(this.activeSection.id, this.selectedGrade)
      .subscribe({
        next: (book) => {
          this.monthlyBook = book;
          this.loading.stop();
        },
        error: () => {
          this.monthlyBook = null;
          this.loading.stop();
        },
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
        grade: this.selectedGrade,
      },
    });
  }
}
