import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { BookService } from './book';
import { Page } from '../models/page';
import { BookResult } from '../models/book';

describe('BookService', () => {
  let service: BookService;
  let httpMock: HttpTestingController;

  const mockPage: Page<BookResult> = {
    content: [
      { id: 1, title: 'Harry Potter', book_type: { id: 1, name: 'Roman' } },
      { id: 2, title: 'Hamlet', book_type: { id: 1, name: 'Roman' } },
    ],
    total_pages: 1,
    total_elements: 2,
    size: 5,
    number: 0,
    first: true,
    last: true,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(BookService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call search endpoint with query and pagination params', () => {
    service.search('Harry', 0, 5).subscribe((page) => {
      expect(page.content.length).toBe(2);
      expect(page.content[0].title).toBe('Harry Potter');
    });

    const req = httpMock.expectOne('/api/book/search/Harry?page=0&size=5');
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });

  it('should encode special characters in search query', () => {
    service.search('Harry & Sally', 0, 5).subscribe();

    const req = httpMock.expectOne('/api/book/search/Harry%20%26%20Sally?page=0&size=5');
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });

  it('should pass page and size params to search', () => {
    service.search('fantasy', 2, 10).subscribe();

    const req = httpMock.expectOne('/api/book/search/fantasy?page=2&size=10');
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });

  it('should call getAll endpoint with pagination params', () => {
    service.getAll(1, 10).subscribe((page) => {
      expect(page.content.length).toBe(2);
    });

    const req = httpMock.expectOne('/api/book?page=1&size=10');
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });
});
