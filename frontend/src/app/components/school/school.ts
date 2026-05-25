import { Component } from '@angular/core';
import { MessageService } from 'primeng/api';
import { SchoolService } from '../../services/school';
import { School } from '../../models/school';
import { Button } from 'primeng/button';
import { RouterLink } from '@angular/router';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-school',
  imports: [Button, RouterLink, NavBarComponent],
  templateUrl: './school.html',
  styleUrl: './school.css',
})
export class SchoolComponent {
  constructor(
    private schoolService: SchoolService,
    private messageService: MessageService,
    public authService: AuthService,
  ) {}

  schools: School[] = [];

  ngOnInit(): void {
    const user = this.authService.currentUser;

    if (user?.role === 'ADMIN') {
      this.schoolService.getAll().subscribe({
        next: (schools) => (this.schools = schools),
        error: () => this.showError(),
      });
    }
    if (user?.role === 'BIBLIOTHEEKBEHEERDER') {
      this.schoolService.getById(user.schoolId).subscribe({
        next: (school) => (this.schools = [school]),
        error: () => this.showError(),
      });
    }
  }

  private showError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Fout',
      detail: 'Fout bij laden van de scholen.',
      life: 3000,
    });
  }
}
