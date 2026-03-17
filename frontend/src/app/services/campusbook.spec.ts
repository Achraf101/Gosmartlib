import { TestBed } from '@angular/core/testing';

import { CampusBookService } from './campusbook';

describe('Campusbook', () => {
  let service: CampusBookService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CampusBookService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
