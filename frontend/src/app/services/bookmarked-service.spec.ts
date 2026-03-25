import { TestBed } from '@angular/core/testing';

import { BookmarkedService } from './bookmarked-service';

describe('BookmarkedService', () => {
  let service: BookmarkedService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BookmarkedService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
