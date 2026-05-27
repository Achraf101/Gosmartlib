import { Component, OnInit } from '@angular/core';
import { SmartschoolLookupService } from '../../services/smartschool-lookup';
import { ActivatedRoute } from '@angular/router';
import { MessageService } from 'primeng/api';
import { TeacherDTO } from '../../models/teacher';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { Skeleton } from 'primeng/skeleton';
import { TableModule } from 'primeng/table';
import { Button } from 'primeng/button';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-promote-page',
  imports: [NavBarComponent, Skeleton, TableModule, Button],
  templateUrl: './promote-page.html',
  styleUrl: './promote-page.css',
})
export class PromotePageComponent implements OnInit {
  teachers: TeacherDTO[] = [];
  schoolId: number;
  loading = true;
  promotingIds = new Set<number>();
  promotedIds = new Set<number>();

  constructor(
    private authService: AuthService,
    private smartschoolLookupService: SmartschoolLookupService,
    private messageService: MessageService,
    private route: ActivatedRoute,
  ) {
    this.schoolId = Number(this.route.snapshot.paramMap.get('schoolId'));
  }

  ngOnInit(): void {
    this.smartschoolLookupService.getAllTeachersForSchool(1).subscribe({
      next: (data) => {
        this.teachers = data;
        this.loading = false;
      },
    });
  }

  addRoleForTeacher(teacherId: number): void {
    this.promotingIds.add(teacherId);

    this.authService.addRoleForTeacher(teacherId).subscribe({
      next: () => {
        this.promotingIds.delete(teacherId);
        this.messageService.add({
          severity: 'success',
          summary: 'Rol toegevoegd',
          detail: 'De leerkracht heeft nu de rol van bibliotheekbeheerder.',
          life: 3000,
        });
        this.promotedIds.add(teacherId);
      },
      error: () => {
        this.promotingIds.delete(teacherId);
      },
    });
  }

  isPromoting(teacherId: number): boolean {
    return this.promotingIds.has(teacherId);
  }
  ddRoleForTeacher(teacherId: number) {}
}
