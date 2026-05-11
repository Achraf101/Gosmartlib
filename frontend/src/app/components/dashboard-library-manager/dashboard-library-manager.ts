import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { MessageModule } from 'primeng/message';
import { ToastModule } from 'primeng/toast';
import { BadgeModule } from 'primeng/badge';
import { SkeletonModule } from 'primeng/skeleton';
import { Author } from '../../models/author';
import { Publisher } from '../../models/publisher';
import { Section } from '../../models/section';
import { BookDetail } from '../../models/book';
import { MessageService } from 'primeng/api';
import { BookCardComponent } from '../misc/book-card/book-card';
import { AuthorService } from '../../services/author';
import { PublisherService } from '../../services/publisher';
import { LoanService } from '../../services/loan';
import { SectionService } from '../../services/section';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { LoanDTO } from '../../models/loan';
import { DatePipe } from '@angular/common';
import { Divider } from 'primeng/divider';
import { CampusBookService } from '../../services/campusbook';
import { ProgressBarModule } from 'primeng/progressbar';
import { CampusSettings } from '../../models/campus-settings';
import { CampusSettingsService } from '../../services/campus-settings';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { isVisible } from '../../models/campus-settings';

@Component({
  selector: 'app-library-manager-dashboard',
  imports: [
    CardModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    TextareaModule,
    ReactiveFormsModule,
    MessageModule,
    ToastModule,
    BadgeModule,
    SkeletonModule,
    BookCardComponent,
    NavBarComponent,
    DatePipe,
    Divider,
    ProgressBarModule,
    ToggleSwitchModule,
    FormsModule,
  ],
  providers: [MessageService],
  templateUrl: './dashboard-library-manager.html',
  styleUrl: './dashboard-library-manager.css',
})
export class DashboardLibraryManager implements OnInit {
  authorFormVisible = false;
  publisherFormVisible = false;
  pendingLoanCount = 0;
  isVisible = isVisible;

  activeSection: Section | null = null;
  grades = [
    { label: 'Graad 1', value: 1 },
    { label: 'Graad 2', value: 2 },
    { label: 'Graad 3', value: 3 },
  ];
  monthlyBooks: { [grade: number]: BookDetail | null } = { 1: null, 2: null, 3: null };
  monthlyBooksLoading = true;

  authorForm = new FormGroup({
    name: new FormControl<string>('', Validators.required),
    description: new FormControl<string | null>(null),
  });

  publisherForm = new FormGroup({
    name: new FormControl<string>('', Validators.required),
    description: new FormControl<string | null>(null),
  });

  overdueLoans: LoanDTO[] = [];
  dueSoonLoans: LoanDTO[] = [];

  overdueLoansLength: number = 0;
  dueSoonLoansLength: number = 0;

  topBooks: { title: string; count: number }[] = [];
  topGenres: { title: string; count: number }[] = [];
  campusStats: { total_books: number; available_books: number } | null = null;
  campusSettings: CampusSettings | null = null;

  spotlightBooks: { [ranking: number]: BookDetail | null } = { 1: null, 2: null, 3: null, 4: null };
  spotlightSectionId: number | null = null;

  constructor(
    private router: Router,
    private authorService: AuthorService,
    private publisherService: PublisherService,
    private loanService: LoanService,
    private sectionService: SectionService,
    private messageService: MessageService,
    private campusBookService: CampusBookService,
    private campusSettingsService: CampusSettingsService,
  ) {}

  ngOnInit(): void {
    this.loadPendingCount();
    this.loadSectionAndBooks();
    this.loadSpotlightBooks();
    this.loadOverdueLoans();
    this.loadTopBooks();
    this.loadCampusStats();
    this.loadCampusSettings();
    this.loadDueSoonLoans();
    this.loadTopGenres();
  }

  loadPendingCount(): void {
  this.loanService.getRequested().subscribe({
    next: (loans) => {
      const uniqueGroups = new Set(loans.map(l => l.groupId ?? l.id));
      this.pendingLoanCount = uniqueGroups.size;
    },
    error: () => (this.pendingLoanCount = 0),
  });
}

  loadSectionAndBooks(): void {
    this.monthlyBooksLoading = true;
    this.sectionService.getAll().subscribe({
      next: (sections) => {
        this.activeSection = sections.find((s) => s.title === 'Boek van de maand') ?? null;
        if (this.activeSection) {
          this.loadAllMonthlyBooks();
        } else {
          this.monthlyBooksLoading = false;
        }
      },
      error: () => {
        this.monthlyBooksLoading = false;
      },
    });
  }

  loadAllMonthlyBooks(): void {
    if (!this.activeSection) return;
    this.monthlyBooksLoading = true;
    let loaded = 0;

    this.grades.forEach((grade) => {
      this.sectionService.getBookBySectionAndGrade(this.activeSection!.id, grade.value).subscribe({
        next: (book) => {
          this.monthlyBooks[grade.value] = book;
          loaded++;
          if (loaded === this.grades.length) this.monthlyBooksLoading = false;
        },
        error: () => {
          this.monthlyBooks[grade.value] = null;
          loaded++;
          if (loaded === this.grades.length) this.monthlyBooksLoading = false;
        },
      });
    });
  }

  loadSpotlightBooks(): void {
    this.sectionService.getAll().subscribe({
      next: (sections) => {
        const spotlight = sections.find((s) => s.title === 'In de kijker');
        if (spotlight) {
          this.spotlightSectionId = spotlight.id;
          this.sectionService.getSpotlightBooks(spotlight.id).subscribe({
            next: (items) => {
              this.spotlightBooks = { 1: null, 2: null, 3: null, 4: null };
              items.forEach((item) => {
                if (item.ranking >= 1 && item.ranking <= 4) {
                  this.spotlightBooks[item.ranking] = item.book;
                }
              });
            },
          });
        }
      },
    });
  }

  getBookAsCard(book: BookDetail) {
    return {
      id: book.id,
      title: book.title,
      author: book.author,
      author_name: book.author_name,
      cover: book.cover,
    };
  }

  changeBookOfMonth(grade: number): void {
    this.router.navigate(['/catalogus'], {
      queryParams: {
        selectMode: true,
        sectionId: this.activeSection!.id,
        grade: grade,
      },
    });
  }

  changeSpotlightBook(ranking: number): void {
    this.router.navigate(['/catalogus'], {
      queryParams: {
        selectMode: true,
        sectionId: this.spotlightSectionId,
        ranking: ranking,
      },
    });
  }

  goToBookForm(): void {
    this.router.navigate(['/boek/toevoegen']);
  }

  goToCampus(): void {
    this.router.navigate(['/campus']);
  }

  goToLoanRequests(): void {
    this.router.navigate(['/uitleenaanvragen']);
  }

  goToCatalogus(): void {
    this.router.navigate(['/catalogus']);
  }

  addAuthor(): void {
    if (this.authorForm.valid) {
      this.authorService.addAuthor(this.authorForm.value as Author).subscribe({
        next: () => {
          this.authorFormVisible = false;
          this.authorForm.reset();
          this.messageService.add({
            severity: 'success',
            summary: 'Auteur toegevoegd',
            detail: 'De auteur is succesvol opgeslagen.',
          });
        },
        error: () =>
          this.messageService.add({
            severity: 'error',
            summary: 'Fout',
            detail: 'Fout bij opslaan auteur.',
          }),
      });
    }
  }

  addPublisher(): void {
    if (this.publisherForm.valid) {
      this.publisherService.addPublisher(this.publisherForm.value as Publisher).subscribe({
        next: () => {
          this.publisherFormVisible = false;
          this.publisherForm.reset();
          this.messageService.add({
            severity: 'success',
            summary: 'Uitgever toegevoegd',
            detail: 'De uitgever is succesvol opgeslagen.',
          });
        },
        error: () =>
          this.messageService.add({
            severity: 'error',
            summary: 'Fout',
            detail: 'Fout bij opslaan uitgever.',
          }),
      });
    }
  }

  loadOverdueLoans(): void {
    this.loanService.getOverdue().subscribe({
      next: (loans) => (this.overdueLoans = loans.slice(0, 5)),
      error: () => (this.overdueLoans = []),
    });
    this.loanService.getOverdueLength().subscribe({
      next: (loansLength) => (this.overdueLoansLength = loansLength),
      error: () => (this.overdueLoansLength = 0),
    });
  }

  loadTopBooks(): void {
    this.loanService.getTopBooks().subscribe({
      next: (books) => (this.topBooks = books),
      error: () => (this.topBooks = []),
    });
  }

  loadCampusStats(): void {
    this.campusBookService.getCampusStats().subscribe({
      next: (stats) => (this.campusStats = stats),
      error: () => (this.campusStats = null),
    });
  }

  loadCampusSettings(): void {
    this.campusSettingsService.getSettings().subscribe({
      next: (settings) => (this.campusSettings = settings),
      error: () => (this.campusSettings = null),
    });
  }

  toggleComponent(screen: string, type: string, visible: boolean): void {
    if (!this.campusSettings) return;

    if (!this.campusSettings.hiddenComponents) {
      this.campusSettings.hiddenComponents = [];
    }

    if (visible) {
      this.campusSettings.hiddenComponents = this.campusSettings.hiddenComponents.filter(
        (c) => !(c.screen === screen && c.type === type),
      );
    } else {
      this.campusSettings.hiddenComponents = [
        ...this.campusSettings.hiddenComponents,
        { screen, type },
      ];
    }

    this.campusSettingsService.updateSettings(this.campusSettings).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Opgeslagen',
          detail: 'Instellingen bijgewerkt.',
        });
      },
      error: () =>
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Kon instellingen niet opslaan.',
        }),
    });
  }

  loadDueSoonLoans(): void {
    this.loanService.getDueSoon().subscribe({
      next: (loans) => (this.dueSoonLoans = loans.slice(0, 5)),
      error: () => (this.dueSoonLoans = []),
    });
    this.loanService.getDueSoonLength().subscribe({
      next: (loansLength) => (this.dueSoonLoansLength = loansLength),
      error: () => (this.dueSoonLoansLength = 0),
    });
  }

  loadTopGenres(): void {
    this.loanService.getTopGenres().subscribe({
      next: (genres) => (this.topGenres = genres),
      error: () => (this.topGenres = []),
    });
  }
}
