import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { provideRouter } from '@angular/router';
import { ReviewModerationPageComponent } from './review-moderation-page';
import { ReviewReportService } from '../../services/review-report';
import { MessageService } from 'primeng/api';
import { ReviewReport } from '../../models/review-report';
import { provideAnimations } from '@angular/platform-browser/animations';

const mockReport: ReviewReport = {
  id: 1,
  review_id: 2,
  reporter_user_id: 3,
  note: 'Ongepaste inhoud',
  created_at: '2025-01-01T10:00:00',
  status: 'PENDING',
  review_rating: 2,
  review_content: 'Slecht boek!',
  review_added: '2024-12-01T08:00:00',
  review_user_id: 5,
  book_id: 10,
  book_title: 'Test boek',
  review_username: 'jan_leerling',
  reporter_username: 'piet_rapporteur',
};

describe('ReviewModerationPageComponent', () => {
  let component: ReviewModerationPageComponent;
  let fixture: ComponentFixture<ReviewModerationPageComponent>;
  let reviewReportServiceSpy: jasmine.SpyObj<ReviewReportService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;

  beforeEach(async () => {
    reviewReportServiceSpy = jasmine.createSpyObj('ReviewReportService', [
      'getPendingReports',
      'acceptReport',
      'rejectReport',
    ]);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['add']);

    reviewReportServiceSpy.getPendingReports.and.returnValue(of([mockReport]));

    await TestBed.configureTestingModule({
      imports: [ReviewModerationPageComponent],
      providers: [
        provideRouter([]),
        provideAnimations(),
        { provide: ReviewReportService, useValue: reviewReportServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ReviewModerationPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load pending reports on init', () => {
    expect(reviewReportServiceSpy.getPendingReports).toHaveBeenCalledTimes(1);
    expect(component.reports.length).toBe(1);
    expect(component.reports[0].review_username).toBe('jan_leerling');
    expect(component.reports[0].reporter_username).toBe('piet_rapporteur');
  });

  it('openDetail should set selectedReport and show dialog', () => {
    component.openDetail(mockReport);

    expect(component.selectedReport).toBe(mockReport);
    expect(component.detailDialogVisible).toBeTrue();
  });

  it('deleteReview should call acceptReport and reload reports on success', () => {
    reviewReportServiceSpy.acceptReport.and.returnValue(of(undefined));
    component.detailDialogVisible = true;

    component.deleteReview(mockReport);

    expect(reviewReportServiceSpy.acceptReport).toHaveBeenCalledWith(1);
    expect(component.detailDialogVisible).toBeFalse();
    expect(reviewReportServiceSpy.getPendingReports).toHaveBeenCalledTimes(2);
  });

  it('rejectReport should call rejectReport service and reload reports on success', () => {
    reviewReportServiceSpy.rejectReport.and.returnValue(of(undefined));
    component.detailDialogVisible = true;

    component.rejectReport(mockReport);

    expect(reviewReportServiceSpy.rejectReport).toHaveBeenCalledWith(1);
    expect(component.detailDialogVisible).toBeFalse();
    expect(reviewReportServiceSpy.getPendingReports).toHaveBeenCalledTimes(2);
  });

  it('deleteReview should show error toast on failure', () => {
    reviewReportServiceSpy.acceptReport.and.returnValue(
      new (require('rxjs').Observable)((subscriber: any) => subscriber.error('fout'))
    );

    component.deleteReview(mockReport);

    expect(messageServiceSpy.add).toHaveBeenCalledWith(
      jasmine.objectContaining({ severity: 'error' })
    );
  });

  it('rejectReport should show error toast on failure', () => {
    reviewReportServiceSpy.rejectReport.and.returnValue(
      new (require('rxjs').Observable)((subscriber: any) => subscriber.error('fout'))
    );

    component.rejectReport(mockReport);

    expect(messageServiceSpy.add).toHaveBeenCalledWith(
      jasmine.objectContaining({ severity: 'error' })
    );
  });

  it('getStars should return array of correct length', () => {
    expect(component.getStars(3).length).toBe(3);
    expect(component.getStars(0).length).toBe(0);
    expect(component.getStars(5).length).toBe(5);
  });

  it('getEmptyStars should return 5 minus rating length', () => {
    expect(component.getEmptyStars(3).length).toBe(2);
    expect(component.getEmptyStars(5).length).toBe(0);
    expect(component.getEmptyStars(0).length).toBe(5);
  });
});
