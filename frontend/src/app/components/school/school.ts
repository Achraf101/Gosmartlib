import { Component } from '@angular/core';
import { ConfirmationService, MessageService } from 'primeng/api';
import { SchoolService } from '../../services/school';
import { School } from '../../models/school';
import { Button } from 'primeng/button';
import { RouterLink } from '@angular/router';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { SmartschoolSyncService } from '../../services/smartschool-sync';
import { ConfirmDialogModule } from 'primeng/confirmdialog';

@Component({
  selector: 'app-school',
  imports: [Button, RouterLink, NavBarComponent, ConfirmDialogModule],
  providers: [ConfirmationService],
  templateUrl: './school.html',
  styleUrl: './school.css',
})
export class SchoolComponent {
  syncLoading = false;
  constructor(
    private schoolService: SchoolService,
    private messageService: MessageService,
    private smartschoolSyncService: SmartschoolSyncService,
    private confirmationService: ConfirmationService,
  ) {}

  schools: School[] = [];

  ngOnInit(): void {
    this.schoolService.getAll().subscribe({
      next: (schools) => (this.schools = schools),
      error: () =>
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Fout bij laden van de scholen.',
          life: 3000,
        }),
    });
  }

  syncSmartschool(schoolId: number): void {
    this.syncLoading = true;
    this.smartschoolSyncService.syncSchool(schoolId).subscribe({
      next: () => {
        this.syncLoading = false;
        this.messageService.add({
          severity: 'success',
          summary: 'Sync voltooid',
          detail: 'Smartschool data is gesynchroniseerd.',
        });
      },
      error: () => {
        this.syncLoading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Fout',
          detail: 'Synchronisatie mislukt.',
        });
      },
    });
  }
  confirmSync(schoolId: number) {
    this.confirmationService.confirm({
      header: 'Bevestiging',
      message: 'Ben je zeker dat je de Smartschool synchronisatie wilt starten?',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Ja',
      rejectLabel: 'Annuleren',
      acceptButtonStyleClass: 'p-button-primary',
      rejectButtonStyleClass: 'p-button-secondary',
      accept: () => {
        this.syncSmartschool(schoolId);
      },
    });
  }
}
