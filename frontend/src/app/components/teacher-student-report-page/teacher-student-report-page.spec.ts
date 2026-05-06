import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { TeacherStudentReportPageComponent } from './teacher-student-report-page';
import { StudentReportService } from '../../services/student-report';
import { StudentReportDTO } from '../../models/student-report';

const mockReport: StudentReportDTO = {
  studentId: 5,
  studentName: 'Anna',
  studentUsername: 'anna',
  borrowCounts: { week: 1, month: 2, semester: 4, schoolYear: 6 },
  borrowedBooksPreview: [],
  reviews: [],
  stats: {
    favoriteGenre: 'Fantasy',
    averageRating: 4.2,
    punctualityRate: 85.0,
    onTimeReturns: 17,
    totalReturns: 20,
  },
};

describe('TeacherStudentReportPageComponent', () => {
  describe('with valid route id', () => {
    let component: TeacherStudentReportPageComponent;
    let fixture: ComponentFixture<TeacherStudentReportPageComponent>;
    let reportServiceSpy: jasmine.SpyObj<StudentReportService>;

    beforeEach(async () => {
      reportServiceSpy = jasmine.createSpyObj('StudentReportService', ['getReport']);
      reportServiceSpy.getReport.and.returnValue(of(mockReport));

      await TestBed.configureTestingModule({
        imports: [TeacherStudentReportPageComponent],
        providers: [
          { provide: StudentReportService, useValue: reportServiceSpy },
          { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
          { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '5' } } } },
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(TeacherStudentReportPageComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should load report for student id from route', () => {
      expect(reportServiceSpy.getReport).toHaveBeenCalledWith(5);
      expect(component.report).toEqual(mockReport);
      expect(component.loading).toBeFalse();
      expect(component.error).toBeFalse();
    });

    it('punctualityLabel shows rate and counts', () => {
      expect(component.punctualityLabel()).toBe('85% (17/20)');
    });

    it('punctualityLabel shows fallback when totalReturns is 0', () => {
      component.report = { ...mockReport, stats: { ...mockReport.stats!, totalReturns: 0, punctualityRate: null } };
      expect(component.punctualityLabel()).toBe('Geen teruggebrachte boeken');
    });

    it('punctualityLabel shows fallback when report is null', () => {
      component.report = null;
      expect(component.punctualityLabel()).toBe('Geen teruggebrachte boeken');
    });
  });

  describe('with no route id', () => {
    let component: TeacherStudentReportPageComponent;
    let fixture: ComponentFixture<TeacherStudentReportPageComponent>;
    let reportServiceSpy: jasmine.SpyObj<StudentReportService>;

    beforeEach(async () => {
      reportServiceSpy = jasmine.createSpyObj('StudentReportService', ['getReport']);

      await TestBed.configureTestingModule({
        imports: [TeacherStudentReportPageComponent],
        providers: [
          { provide: StudentReportService, useValue: reportServiceSpy },
          { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
          { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => null } } } },
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(TeacherStudentReportPageComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should set error and not call service', () => {
      expect(component.error).toBeTrue();
      expect(component.loading).toBeFalse();
      expect(reportServiceSpy.getReport).not.toHaveBeenCalled();
    });
  });

  describe('on service failure', () => {
    let component: TeacherStudentReportPageComponent;
    let fixture: ComponentFixture<TeacherStudentReportPageComponent>;

    beforeEach(async () => {
      const reportServiceSpy = jasmine.createSpyObj('StudentReportService', ['getReport']);
      reportServiceSpy.getReport.and.returnValue(throwError(() => new Error('server error')));

      await TestBed.configureTestingModule({
        imports: [TeacherStudentReportPageComponent],
        providers: [
          { provide: StudentReportService, useValue: reportServiceSpy },
          { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
          { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '5' } } } },
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(TeacherStudentReportPageComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should set error flag', () => {
      expect(component.error).toBeTrue();
      expect(component.loading).toBeFalse();
    });
  });
});
