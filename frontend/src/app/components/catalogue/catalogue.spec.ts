import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { CatalogueComponent } from './catalogue';
import { BookService } from '../../services/book';
import { BookResult } from '../../models/book';
import { Page } from '../../models/page';

const mockBooks: BookResult[] = [
  {
    id: 1,
    title: 'Test Boek',
    description: 'Een beschrijving',
    fiction: true,
    published: 2023,
    cover: 'https://example.com/cover.jpg',
    pages: 300,
    rating: 4,
    rating_count: 120,
    available: true,
    book_type: { id: 1, name: 'Boek' },
  } as BookResult,
  {
    id: 2,
    title: 'Non-Fictie Boek',
    description: 'Nog een beschrijving',
    fiction: false,
    published: 2021,
    cover: '',
    pages: 0,
    rating: 0,
    rating_count: 0,
    available: true,
    book_type: { id: 1, name: 'Boek' },
  } as BookResult,
];

const mockPage: Page<BookResult> = {
  content: mockBooks,
  total_pages: 1,
  total_elements: 2,
  size: 5,
  number: 0,
  first: true,
  last: true,
};

describe('CatalogueComponent', () => {
  let component: CatalogueComponent;
  let fixture: ComponentFixture<CatalogueComponent>;
  let bookServiceSpy: jasmine.SpyObj<BookService>;

  beforeEach(async () => {
    bookServiceSpy = jasmine.createSpyObj('BookService', ['getAll']);
    bookServiceSpy.getAll.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      imports: [CatalogueComponent],
      providers: [{ provide: BookService, useValue: bookServiceSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(CatalogueComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call loadBooks on init', () => {
    fixture.detectChanges();
    expect(bookServiceSpy.getAll).toHaveBeenCalled();
  });

  it('should show spinner while loading', () => {
    spyOn(component, 'loadBooks');
    component.loading = true;
    fixture.detectChanges();
    const spinnerContainer = fixture.nativeElement.querySelector('.loading-container');
    expect(spinnerContainer).toBeTruthy();
  });

  it('should hide spinner after data loads', () => {
    fixture.detectChanges();
    const spinner = fixture.nativeElement.querySelector('p-progressSpinner, p-progressspinner');
    expect(spinner).toBeFalsy();
  });

  it('should show empty message when no books', () => {
    bookServiceSpy.getAll.and.returnValue(
      of({
        content: [],
        total_pages: 0,
        total_elements: 0,
        size: 5,
        number: 0,
        first: true,
        last: true,
      }),
    );
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

  it('should set loading to false on API error', () => {
    bookServiceSpy.getAll.and.returnValue(throwError(() => new Error('API error')));
    fixture.detectChanges();
    expect(component.loading).toBeFalse();
  });
});
