import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { SkeletonModule } from 'primeng/skeleton';
import { MessageModule } from 'primeng/message';
import { TableModule } from 'primeng/table';
import { ClassroomService } from '../../services/classroom';
import { StudentPreviewDTO } from '../../models/classroom';
import { NavBarComponent } from '../nav-bar/nav-bar';

@Component({
  selector: 'app-teacher-class-detail-page',
  standalone: true,
  imports: [
    CommonModule,
    CardModule,
    ButtonModule,
    SkeletonModule,
    MessageModule,
    TableModule,
    DatePipe,
    NavBarComponent,
  ],
  templateUrl: './teacher-class-detail-page.html',
  styleUrl: './teacher-class-detail-page.css',
})
export class TeacherClassDetailPageComponent implements OnInit {
  classroomId: number | null = null;
  students: StudentPreviewDTO[] = [];
  loading = true;
  error = false;

  constructor(
    private classroomService: ClassroomService,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit(): void {
    const idRaw = this.route.snapshot.paramMap.get('id');
    if (!idRaw) {
      this.error = true;
      this.loading = false;
      return;
    }
    this.classroomId = Number(idRaw);
    this.classroomService.getStudents(this.classroomId).subscribe({
      next: (data) => {
        this.students = data;
        this.loading = false;
      },
      error: () => {
        this.error = true;
        this.loading = false;
      },
    });
  }

  openReport(studentId: number): void {
    this.router.navigate(['/leerkracht/leerlingen', studentId]);
  }

  back(): void {
    this.router.navigate(['/leerkracht/klassen']);
  }
}
