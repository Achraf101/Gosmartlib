import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { MessageModule } from 'primeng/message';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { AuthorService } from '../../services/author';
import { PublisherService } from '../../services/publisher';
import { GenreService } from '../../services/genre';
import { ThemeService } from '../../services/theme';
import { Author } from '../../models/author';
import { Publisher } from '../../models/publisher';

@Component({
  selector: 'app-admin-dashboard',
  imports: [
    CardModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    TextareaModule,
    ReactiveFormsModule,
    MessageModule,
    ToastModule,
    NavBarComponent,
  ],
  providers: [MessageService],
  templateUrl: './dashboard-admin.html',
  styleUrl: './dashboard-admin.css',
})
export class DashboardAdmin {
  authorFormVisible = false;
  publisherFormVisible = false;
  genreFormVisible = false;
  themeFormVisible = false;

  authorForm = new FormGroup({
    name: new FormControl<string>('', Validators.required),
    description: new FormControl<string | null>(null),
  });

  publisherForm = new FormGroup({
    name: new FormControl<string>('', Validators.required),
    description: new FormControl<string | null>(null),
  });

  genreForm = new FormGroup({
    name: new FormControl<string>('', Validators.required),
  });

  themeForm = new FormGroup({
    name: new FormControl<string>('', Validators.required),
  });

  constructor(
    private router: Router,
    private authorService: AuthorService,
    private publisherService: PublisherService,
    private genreService: GenreService,
    private themeService: ThemeService,
    private messageService: MessageService,
  ) {}

  goToBooks(): void {
    this.router.navigate(['/catalogus']);
  }

  goToSchools(): void {
    this.router.navigate(['/school']);
  }

  goToAddSchool(): void {
    this.router.navigate(['/school/toevoegen']);
  }

  goToLocations(): void {
    this.router.navigate(['/locatie']);
  }

  addAuthor(): void {
    if (this.authorForm.valid) {
      this.authorService.addAuthor(this.authorForm.value as Author).subscribe({
        next: () => {
          this.authorFormVisible = false;
          this.authorForm.reset();
          this.messageService.add({ severity: 'success', summary: 'Auteur toegevoegd' });
        },
      });
    }
  }

  addPublisher(): void {
    if (this.publisherForm.valid) {
      this.publisherService.addPublisher(this.publisherForm.value as Publisher).subscribe({
        next: () => {
          this.publisherFormVisible = false;
          this.publisherForm.reset();
          this.messageService.add({ severity: 'success', summary: 'Uitgever toegevoegd' });
        },
      });
    }
  }

  addGenre(): void {
    if (this.genreForm.valid) {
      this.genreService.addGenre(this.genreForm.value as { name: string }).subscribe({
        next: () => {
          this.genreFormVisible = false;
          this.genreForm.reset();
          this.messageService.add({ severity: 'success', summary: 'Genre toegevoegd' });
        },
      });
    }
  }

  addTheme(): void {
    if (this.themeForm.valid) {
      this.themeService.addTheme(this.themeForm.value as { name: string }).subscribe({
        next: () => {
          this.themeFormVisible = false;
          this.themeForm.reset();
          this.messageService.add({ severity: 'success', summary: 'Thema toegevoegd' });
        },
      });
    }
  }
}
