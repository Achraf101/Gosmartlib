import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { SkeletonModule } from 'primeng/skeleton';
import { MessageModule } from 'primeng/message';
import { TagModule } from 'primeng/tag';
import { ProgressBarModule } from 'primeng/progressbar';
import { Divider } from 'primeng/divider';
import { StudentReportService } from '../../services/student-report';
import { StudentReportDTO } from '../../models/student-report';
import { NavBarComponent } from '../nav-bar/nav-bar';

@Component({
  selector: 'app-teacher-student-report-page',
  standalone: true,
  imports: [
    CommonModule,
    CardModule,
    ButtonModule,
    SkeletonModule,
    MessageModule,
    TagModule,
    ProgressBarModule,
    Divider,
    DatePipe,
    NavBarComponent,
  ],
  templateUrl: './teacher-student-report-page.html',
  styleUrl: './teacher-student-report-page.css',
})
export class TeacherStudentReportPageComponent implements OnInit {
  report: StudentReportDTO | null = null;
  loading = true;
  error = false;

  constructor(
    private studentReportService: StudentReportService,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    const idRaw = this.route.snapshot.paramMap.get('id');
    if (!idRaw) {
      this.error = true;
      this.loading = false;
      return;
    }
    this.studentReportService.getReport(Number(idRaw)).subscribe({
      next: (data) => {
        this.report = data;
        this.loading = false;
      },
      error: () => {
        this.error = true;
        this.loading = false;
      },
    });
  }

  back(): void {
    history.back();
  }

  punctualityLabel(): string {
    const stats = this.report?.stats;
    if (!stats || stats.totalReturns === 0 || stats.punctualityRate === null) {
      return 'Geen teruggebrachte boeken';
    }
    return `${stats.punctualityRate}% (${stats.onTimeReturns}/${stats.totalReturns})`;
  }
}
