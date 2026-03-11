import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { provideRouter } from '@angular/router';

import { HomeComponent } from './home';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let router: Router;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should navigate to catalogus with query param on search', () => {
    const navigateSpy = spyOn(router, 'navigate');
    component.searchQuery = 'Harry Potter';

    const event = new Event('submit');
    event.preventDefault = jasmine.createSpy('preventDefault');
    component.onSearch(event);

    expect(navigateSpy).toHaveBeenCalledWith(['/catalogus'], {
      queryParams: { q: 'Harry Potter' },
    });
  });

  it('should not navigate when search query is empty', () => {
    const navigateSpy = spyOn(router, 'navigate');
    component.searchQuery = '   ';

    const event = new Event('submit');
    event.preventDefault = jasmine.createSpy('preventDefault');
    component.onSearch(event);

    expect(navigateSpy).not.toHaveBeenCalled();
  });

  it('should trim search query before navigating', () => {
    const navigateSpy = spyOn(router, 'navigate');
    component.searchQuery = '  fantasy  ';

    const event = new Event('submit');
    event.preventDefault = jasmine.createSpy('preventDefault');
    component.onSearch(event);

    expect(navigateSpy).toHaveBeenCalledWith(['/catalogus'], {
      queryParams: { q: 'fantasy' },
    });
  });

  it('should update searchQuery on input event', () => {
    const inputEvent = new Event('input');
    Object.defineProperty(inputEvent, 'target', { value: { value: 'test query' } });
    component.onSearchInput(inputEvent);
    expect(component.searchQuery).toBe('test query');
  });

  it('should render search input on the page', () => {
    fixture.detectChanges();
    httpMock.expectOne('/api/book/featured');
    const input = fixture.nativeElement.querySelector('.search-input');
    expect(input).toBeTruthy();
  });

  it('should render search button', () => {
    fixture.detectChanges();
    httpMock.expectOne('/api/book/featured');
    const btn = fixture.nativeElement.querySelector('.search-btn');
    expect(btn).toBeTruthy();
  });
});
