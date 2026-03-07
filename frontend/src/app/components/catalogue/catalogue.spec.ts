import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { CatalogueComponent } from './catalogue';
import { TestBookService } from '../../services/test-book';
import { TestBook } from '../../models/test-book';
import { Page } from '../../models/page';

function mockPage(books: TestBook[], total: number): Page<TestBook> {
  return {
    content: books,
    total_pages: Math.ceil(total / 5),
    total_elements: total,
    size: 5,
    number: 0,
    first: true,
    last: total <= 5,
  };
}

const mockBooks: TestBook[] = [
  {
    id: 1,
    isbn: '9781234567890',
    title: 'Test Boek',
    description: 'Een beschrijving',
    fiction: true,
    published: 2023,
    cover: 'https://example.com/cover.jpg',
    pages: 300,
    rating: 4,
    rating_count: 120,
  },
  {
    id: 2,
    isbn: '9780987654321',
    title: 'Non-Fictie Boek',
    description: 'Nog een beschrijving',
    fiction: false,
    published: 2021,
    cover: '',
    pages: 0,
    rating: 0,
    rating_count: 0,
  },
];

describe('CatalogueComponent', () => {
  let component: CatalogueComponent;
  let fixture: ComponentFixture<CatalogueComponent>;
  let testBookServiceSpy: jasmine.SpyObj<TestBookService>;

  beforeEach(async () => {
    testBookServiceSpy = jasmine.createSpyObj('TestBookService', ['getBooks']);
    testBookServiceSpy.getBooks.and.returnValue(of(mockPage(mockBooks, 2)));

    await TestBed.configureTestingModule({
      imports: [CatalogueComponent],
      providers: [{ provide: TestBookService, useValue: testBookServiceSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(CatalogueComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call loadBooks(0, 5) on init', () => {
    fixture.detectChanges();
    expect(testBookServiceSpy.getBooks).toHaveBeenCalledWith(0, 5);
  });

  it('should show spinner while loading', () => {
    spyOn(component, 'loadBooks');
    component.loading = true;
    fixture.detectChanges();
    const spinnerContainer = fixture.nativeElement.querySelector('.loading-container');
    expect(spinnerContainer).toBeTruthy();
  });

  it('should hide spinner after data loads', () => {
    fixture.detectChanges(); // triggers ngOnInit -> loadBooks -> sets loading=false
    const spinner = fixture.nativeElement.querySelector('p-progressSpinner, p-progressspinner');
    expect(spinner).toBeFalsy();
  });

  it('should show empty message when no books', () => {
    testBookServiceSpy.getBooks.and.returnValue(of(mockPage([], 0)));
    fixture.detectChanges();
    const msg = fixture.nativeElement.querySelector('p');
    expect(msg?.textContent).toContain('Geen boeken gevonden.');
  });

  it('should render book cards when books are present', () => {
    fixture.detectChanges();
    const cards = fixture.nativeElement.querySelectorAll('.book-card');
    expect(cards.length).toBe(2);
  });

  it('should display book title', () => {
    fixture.detectChanges();
    const titles = fixture.nativeElement.querySelectorAll('.book-title');
    expect(titles[0].textContent).toContain('Test Boek');
    expect(titles[1].textContent).toContain('Non-Fictie Boek');
  });

  it('should display description', () => {
    fixture.detectChanges();
    const descs = fixture.nativeElement.querySelectorAll('.book-description');
    expect(descs[0].textContent).toContain('Een beschrijving');
  });

  it('should display published year and pages when present', () => {
    fixture.detectChanges();
    const subtitles = fixture.nativeElement.querySelectorAll('.book-subtitle');
    expect(subtitles[0].textContent).toContain('2023');
    expect(subtitles[0].textContent).toContain("300 pagina's");
  });

  it('should use book cover image when provided', () => {
    fixture.detectChanges();
    const images = fixture.nativeElement.querySelectorAll('.book-cover');
    expect(images[0].src).toBe('https://example.com/cover.jpg');
  });

  it('should use placeholder image when cover is empty', () => {
    fixture.detectChanges();
    const images = fixture.nativeElement.querySelectorAll('.book-cover');
    expect(images[1].src).toContain('placehold.co');
  });

  it('should show Fictie tag for fiction books', () => {
    fixture.detectChanges();
    const cards = fixture.nativeElement.querySelectorAll('.book-card');
    const firstMeta = cards[0].querySelector('.book-meta');
    expect(firstMeta.textContent).toContain('Fictie');
  });

  it('should show Non-fictie tag for non-fiction books', () => {
    fixture.detectChanges();
    const cards = fixture.nativeElement.querySelectorAll('.book-card');
    const secondMeta = cards[1].querySelector('.book-meta');
    expect(secondMeta.textContent).toContain('Non-fictie');
  });

  it('should display rating when present', () => {
    fixture.detectChanges();
    const ratings = fixture.nativeElement.querySelectorAll('.book-rating');
    expect(ratings.length).toBeGreaterThanOrEqual(1);
    expect(ratings[0].textContent).toContain('4/5');
    expect(ratings[0].textContent).toContain('120');
  });

  it('should hide rating when rating is 0', () => {
    fixture.detectChanges();
    const cards = fixture.nativeElement.querySelectorAll('.book-card');
    const secondCardRating = cards[1].querySelector('.book-rating');
    expect(secondCardRating).toBeFalsy();
  });

  it('should update first and rows on page change', () => {
    fixture.detectChanges();
    component.onPageChange({ first: 10, rows: 5 });
    expect(component.first).toBe(10);
    expect(component.rows).toBe(5);
  });

  it('should call loadBooks with correct page on page change', () => {
    fixture.detectChanges();
    testBookServiceSpy.getBooks.calls.reset();
    component.onPageChange({ first: 10, rows: 5 });
    expect(testBookServiceSpy.getBooks).toHaveBeenCalledWith(2, 5);
  });

  it('should set loading to false on API error', () => {
    testBookServiceSpy.getBooks.and.returnValue(throwError(() => new Error('API error')));
    fixture.detectChanges();
    expect(component.loading).toBeFalse();
  });

  it('should update totalRecords from API response', () => {
    testBookServiceSpy.getBooks.and.returnValue(of(mockPage(mockBooks, 42)));
    fixture.detectChanges();
    expect(component.totalRecords).toBe(42);
  });
});
