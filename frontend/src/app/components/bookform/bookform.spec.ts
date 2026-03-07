import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BookformComponent } from './bookform';

describe('Bookform', () => {
  let component: BookformComponent;
  let fixture: ComponentFixture<BookformComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookformComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(BookformComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
