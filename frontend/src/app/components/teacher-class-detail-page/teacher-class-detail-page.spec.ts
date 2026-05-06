import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { TeacherClassDetailPageComponent } from './teacher-class-detail-page';
import { ClassroomService } from '../../services/classroom';
import { StudentPreviewDTO } from '../../models/classroom';

const mockStudents: StudentPreviewDTO[] = [
  { id: 2, username: 'anna', name: 'Anna', lastActivity: null },
  { id: 3, username: 'ben', name: 'Ben', lastActivity: null },
];

describe('TeacherClassDetailPageComponent', () => {
  describe('with valid route id', () => {
    let component: TeacherClassDetailPageComponent;
    let fixture: ComponentFixture<TeacherClassDetailPageComponent>;
    let classroomServiceSpy: jasmine.SpyObj<ClassroomService>;
    let routerSpy: jasmine.SpyObj<Router>;

    beforeEach(async () => {
      classroomServiceSpy = jasmine.createSpyObj('ClassroomService', ['getStudents']);
      routerSpy = jasmine.createSpyObj('Router', ['navigate']);
      classroomServiceSpy.getStudents.and.returnValue(of(mockStudents));

      await TestBed.configureTestingModule({
        imports: [TeacherClassDetailPageComponent],
        providers: [
          { provide: ClassroomService, useValue: classroomServiceSpy },
          { provide: Router, useValue: routerSpy },
          { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '10' } } } },
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(TeacherClassDetailPageComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should load students for classroom id from route', () => {
      expect(classroomServiceSpy.getStudents).toHaveBeenCalledWith(10);
      expect(component.students).toEqual(mockStudents);
      expect(component.loading).toBeFalse();
      expect(component.error).toBeFalse();
    });

    it('should navigate to report on openReport', () => {
      component.openReport(2);
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/leerkracht/leerlingen', 2]);
    });

    it('should navigate back to classes on back()', () => {
      component.back();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/leerkracht/klassen']);
    });
  });

  describe('with no route id', () => {
    let component: TeacherClassDetailPageComponent;
    let fixture: ComponentFixture<TeacherClassDetailPageComponent>;
    let classroomServiceSpy: jasmine.SpyObj<ClassroomService>;

    beforeEach(async () => {
      classroomServiceSpy = jasmine.createSpyObj('ClassroomService', ['getStudents']);

      await TestBed.configureTestingModule({
        imports: [TeacherClassDetailPageComponent],
        providers: [
          { provide: ClassroomService, useValue: classroomServiceSpy },
          { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
          { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => null } } } },
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(TeacherClassDetailPageComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should set error and not call service', () => {
      expect(component.error).toBeTrue();
      expect(component.loading).toBeFalse();
      expect(classroomServiceSpy.getStudents).not.toHaveBeenCalled();
    });
  });

  describe('on service failure', () => {
    let component: TeacherClassDetailPageComponent;
    let fixture: ComponentFixture<TeacherClassDetailPageComponent>;

    beforeEach(async () => {
      const classroomServiceSpy = jasmine.createSpyObj('ClassroomService', ['getStudents']);
      classroomServiceSpy.getStudents.and.returnValue(throwError(() => new Error('server error')));

      await TestBed.configureTestingModule({
        imports: [TeacherClassDetailPageComponent],
        providers: [
          { provide: ClassroomService, useValue: classroomServiceSpy },
          { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
          { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '10' } } } },
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(TeacherClassDetailPageComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should set error flag', () => {
      expect(component.error).toBeTrue();
      expect(component.loading).toBeFalse();
    });
  });
});
