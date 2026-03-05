import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SchoolComponent } from './school';

describe('School', () => {
  let component: SchoolComponent;
  let fixture: ComponentFixture<SchoolComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SchoolComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(SchoolComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
