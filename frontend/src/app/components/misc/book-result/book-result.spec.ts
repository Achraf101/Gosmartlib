import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BookResult } from './book-result';

describe('BookResult', () => {
  let component: BookResult;
  let fixture: ComponentFixture<BookResult>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookResult]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BookResult);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
