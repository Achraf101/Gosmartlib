import { Component } from '@angular/core';
import { MessageService } from 'primeng/api';
import { SchoolService } from '../../services/school';
import { School } from '../../models/school';
import { Button } from 'primeng/button';
import { RouterLink } from '@angular/router';
import { NavBarComponent } from '../nav-bar/nav-bar';

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
}
