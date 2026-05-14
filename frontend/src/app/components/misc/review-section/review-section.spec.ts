import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ReviewSectionComponent } from './review-section';
import { ApiService } from '../../../services/api';
import { ReviewReportService } from '../../../services/review-report';
import { MessageService, ConfirmationService } from 'primeng/api';
import { Review } from '../../../models/review';
import { provideAnimations } from '@angular/platform-browser/animations';

const mockReview: Review = {
  id: 1,
  user_id: 2,
  book_id: 10,
  rating: 4,
  content: 'Goed boek!',
  added: '2025-01-01',
};

describe('ReviewSectionComponent', () => {
  let component: ReviewSectionComponent;
  let fixture: ComponentFixture<ReviewSectionComponent>;
  let apiServiceSpy: jasmine.SpyObj<ApiService>;
  let reviewReportServiceSpy: jasmine.SpyObj<ReviewReportService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;

  beforeEach(async () => {
    apiServiceSpy = jasmine.createSpyObj('ApiService', ['get', 'post', 'delete']);
    reviewReportServiceSpy = jasmine.createSpyObj('ReviewReportService', ['reportReview']);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['add']);

    apiServiceSpy.get.and.callFake((url: string) => {
      if (url.includes('auth/me/id')) return of({ userId: 1 });
      return of([]);
    });

    await TestBed.configureTestingModule({
      imports: [ReviewSectionComponent],
      providers: [
        provideAnimations(),
        { provide: ApiService, useValue: apiServiceSpy },
        { provide: ReviewReportService, useValue: reviewReportServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        ConfirmationService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ReviewSectionComponent);
    component = fixture.componentInstance;
    component.bookId = 10;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load reviews and current user on init', () => {
    expect(apiServiceSpy.get).toHaveBeenCalledWith('review/book/10');
    expect(apiServiceSpy.get).toHaveBeenCalledWith('auth/me/id');
    expect(component.currentUserId).toBe(1);
  });

  it('getMenuItems should return delete option for own review', () => {
    component.currentUserId = 2;

    const items = component.getMenuItems(mockReview);

    expect(items.length).toBe(1);
    expect(items[0].label).toBe('Recensie verwijderen');
    expect(items[0].icon).toBe('pi pi-trash');
  });

  it('getMenuItems should return report option for another user review', () => {
    component.currentUserId = 99;

    const items = component.getMenuItems(mockReview);

    expect(items.length).toBe(1);
    expect(items[0].label).toBe('Recensie rapporteren');
    expect(items[0].icon).toBe('pi pi-flag');
  });

  it('submitReview should show error when rating is 0', () => {
    component.newRating = 0;

    component.submitReview();

    expect(component.error).toBeTruthy();
    expect(apiServiceSpy.post).not.toHaveBeenCalled();
  });

  it('submitReview should show error when content contains a URL', () => {
    component.newRating = 4;
    component.newContent = 'Kijk op https://spam.com voor meer info.';

    component.submitReview();

    expect(component.error).toBe('Je recensie mag geen URLs bevatten.');
    expect(apiServiceSpy.post).not.toHaveBeenCalled();
  });

  it('submitReview should POST review when rating and content are valid', () => {
    component.newRating = 5;
    component.newContent = 'Geweldig boek!';
    apiServiceSpy.post.and.returnValue(of({ id: 1, rating: 5, content: 'Geweldig boek!' }));
    apiServiceSpy.get.and.returnValue(of([]));

    component.submitReview();

    expect(apiServiceSpy.post).toHaveBeenCalledWith('review/book/10', {
      rating: 5,
      content: 'Geweldig boek!',
    });
    expect(component.submitted).toBeTrue();
    expect(component.newRating).toBe(0);
    expect(component.newContent).toBe('');
  });

  it('openReportDialog should set reportingReviewId and show dialog', () => {
    component.openReportDialog(mockReview);

    expect(component.reportingReviewId).toBe(1);
    expect(component.reportDialogVisible).toBeTrue();
    expect(component.reportNote).toBe('');
    expect(component.reportError).toBe('');
  });

  it('getStars should return array of correct length', () => {
    expect(component.getStars(4).length).toBe(4);
    expect(component.getStars(1).length).toBe(1);
  });

  it('getEmptyStars should return 5 minus rating', () => {
    expect(component.getEmptyStars(4).length).toBe(1);
    expect(component.getEmptyStars(0).length).toBe(5);
  });
});
