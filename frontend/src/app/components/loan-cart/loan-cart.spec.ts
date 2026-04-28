import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoanCartComponent } from './loan-cart';

describe('LoanCart', () => {
  let component: LoanCartComponent;
  let fixture: ComponentFixture<LoanCartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoanCartComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(LoanCartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
