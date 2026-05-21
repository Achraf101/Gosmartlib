export interface BorrowCountsDTO {
  week: number;
  month: number;
  semester: number;
  schoolYear: number;
}

export interface BorrowedBookPreviewDTO {
  bookId: number;
  title: string;
  author: string | null;
  cover: string | null;
  start: string;
  end: string;
  returned: boolean;
  overdue: boolean;
}

export interface StudentReviewDTO {
  id: number;
  bookId: number;
  bookTitle: string;
  bookCover: string | null;
  rating: number;
  content: string | null;
  added: string;
}

export interface StudentReportStatsDTO {
  favoriteGenre: string | null;
  averageRating: number | null;
  punctualityRate: number | null;
  onTimeReturns: number;
  totalReturns: number;
}

export interface StudentReportDTO {
  studentId: number;
  firstName: string;
  lastName: string;
  borrowCounts: BorrowCountsDTO;
  borrowedBooksPreview: BorrowedBookPreviewDTO[];
  reviews: StudentReviewDTO[];
  stats: StudentReportStatsDTO;
}
