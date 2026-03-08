import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BookSection } from './book-section';

describe('BookSection', () => {
  let component: BookSection;
  let fixture: ComponentFixture<BookSection>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookSection]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BookSection);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
