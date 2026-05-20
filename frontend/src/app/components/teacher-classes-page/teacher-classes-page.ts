import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { SkeletonModule } from 'primeng/skeleton';
import { MessageModule } from 'primeng/message';
import { ClassroomService } from '../../services/classroom';
import { ClassroomDTO } from '../../models/classroom';
import { NavBarComponent } from '../nav-bar/nav-bar';

@Component({
  selector: 'app-teacher-classes-page',
  standalone: true,
  imports: [CommonModule, CardModule, ButtonModule, SkeletonModule, MessageModule, NavBarComponent],
  templateUrl: './teacher-classes-page.html',
  styleUrl: './teacher-classes-page.css',
})
export class TeacherClassesPageComponent implements OnInit {
  classrooms: ClassroomDTO[] = [];
  loading = true;
  error = false;

  constructor(
    private classroomService: ClassroomService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.classroomService.getMyClassrooms().subscribe({
      next: (data) => {
        this.classrooms = data;
        this.loading = false;
      },
      error: () => {
        this.error = true;
        this.loading = false;
      },
    });
  }

  openClassroom(id: number): void {
    this.router.navigate(['/leerkracht/klassen', id]);
  }
}
