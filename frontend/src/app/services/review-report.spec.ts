import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ReviewReportService } from './review-report';
import { ReviewReport } from '../models/review-report';

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

describe('ReviewReportService', () => {
  let service: ReviewReportService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ReviewReportService],
    });
    service = TestBed.inject(ReviewReportService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('reportReview should POST to the correct endpoint with note', () => {
    service.reportReview(5, 'ongepaste tekst').subscribe();

    const req = httpMock.expectOne('/api/review/5/rapporteer');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ note: 'ongepaste tekst' });
    req.flush(null);
  });

  it('getPendingReports should GET from the correct endpoint and return reports', () => {
    service.getPendingReports().subscribe((reports) => {
      expect(reports.length).toBe(1);
      expect(reports[0].review_username).toBe('jan_leerling');
      expect(reports[0].reporter_username).toBe('piet_rapporteur');
    });

    const req = httpMock.expectOne('/api/review/rapportages');
    expect(req.request.method).toBe('GET');
    req.flush([mockReport]);
  });

  it('acceptReport should PUT to the correct endpoint', () => {
    service.acceptReport(7).subscribe();

    const req = httpMock.expectOne('/api/review/rapportages/7/accepteren');
    expect(req.request.method).toBe('PUT');
    req.flush(null);
  });

  it('rejectReport should PUT to the correct endpoint', () => {
    service.rejectReport(8).subscribe();

    const req = httpMock.expectOne('/api/review/rapportages/8/weigeren');
    expect(req.request.method).toBe('PUT');
    req.flush(null);
  });
});
