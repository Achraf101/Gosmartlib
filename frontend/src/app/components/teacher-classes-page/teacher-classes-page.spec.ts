import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { TeacherClassesPageComponent } from './teacher-classes-page';
import { ClassroomService } from '../../services/classroom';
import { ClassroomDTO } from '../../models/classroom';

const mockClassrooms: ClassroomDTO[] = [
  { id: 1, name: '3A', studentCount: 5 },
  { id: 2, name: '4B', studentCount: 3 },
];

describe('TeacherClassesPageComponent', () => {
  describe('on success', () => {
    let component: TeacherClassesPageComponent;
    let fixture: ComponentFixture<TeacherClassesPageComponent>;
    let classroomServiceSpy: jasmine.SpyObj<ClassroomService>;
    let routerSpy: jasmine.SpyObj<Router>;

    beforeEach(async () => {
      classroomServiceSpy = jasmine.createSpyObj('ClassroomService', ['getMyClassrooms']);
      routerSpy = jasmine.createSpyObj('Router', ['navigate']);
      classroomServiceSpy.getMyClassrooms.and.returnValue(of(mockClassrooms));

      await TestBed.configureTestingModule({
        imports: [TeacherClassesPageComponent],
        providers: [
          { provide: ClassroomService, useValue: classroomServiceSpy },
          { provide: Router, useValue: routerSpy },
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(TeacherClassesPageComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should call getMyClassrooms on init', () => {
      expect(classroomServiceSpy.getMyClassrooms).toHaveBeenCalledTimes(1);
    });

    it('should populate classrooms and clear loading flag', () => {
      expect(component.classrooms).toEqual(mockClassrooms);
      expect(component.loading).toBeFalse();
      expect(component.error).toBeFalse();
    });

    it('should navigate to classroom on openClassroom', () => {
      component.openClassroom(1);
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/leerkracht/klassen', 1]);
    });
  });

  describe('on service failure', () => {
    let component: TeacherClassesPageComponent;
    let fixture: ComponentFixture<TeacherClassesPageComponent>;

    beforeEach(async () => {
      const classroomServiceSpy = jasmine.createSpyObj('ClassroomService', ['getMyClassrooms']);
      classroomServiceSpy.getMyClassrooms.and.returnValue(throwError(() => new Error('server error')));

      await TestBed.configureTestingModule({
        imports: [TeacherClassesPageComponent],
        providers: [
          { provide: ClassroomService, useValue: classroomServiceSpy },
          { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(TeacherClassesPageComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should set error flag and clear loading', () => {
      expect(component.error).toBeTrue();
      expect(component.loading).toBeFalse();
    });
  });
});
