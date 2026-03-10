import { TestBed } from '@angular/core/testing';

import { CampusService } from './campus';

describe('Campus', () => {
  let service: CampusService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CampusService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
