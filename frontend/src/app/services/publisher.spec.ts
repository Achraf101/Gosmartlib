import { TestBed } from '@angular/core/testing';

import { PublisherService } from './publisher';

describe('Publisher', () => {
  let service: PublisherService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PublisherService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
