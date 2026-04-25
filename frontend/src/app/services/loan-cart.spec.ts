import { TestBed } from '@angular/core/testing';

import { LoanCartService } from './loan-cart';

describe('LoanCart', () => {
  let service: LoanCartService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LoanCartService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
