package be.ap.backend.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import be.ap.backend.dto.BorrowCountsDTO;
import be.ap.backend.dto.BorrowedBookPreviewDTO;
import be.ap.backend.dto.StudentReportDTO;
import be.ap.backend.dto.StudentReportStatsDTO;
import be.ap.backend.dto.StudentReviewDTO;
import be.ap.backend.entity.Loan;
import be.ap.backend.entity.LoanBook;
import be.ap.backend.entity.Review;
import be.ap.backend.entity.User;
import be.ap.backend.enums.LoanStatus;
import be.ap.backend.repository.LoanRepository;
import be.ap.backend.repository.ReviewRepository;
import be.ap.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

/**
 * Service for building a student's reading report, including borrow counts,
 * book previews, reviews, and statistics.
 */
@Service
public class StudentReportService {

    private static final List<LoanStatus> COUNTED_STATUSES = List.of(
            LoanStatus.ACCEPTED, LoanStatus.RECEIVED, LoanStatus.RETURNED);

    private final LoanRepository loanRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final SmartschoolLookupService lookupService;

    public StudentReportService(LoanRepository loanRepository, ReviewRepository reviewRepository,
            UserRepository userRepository, SmartschoolLookupService lookupService) {
        this.loanRepository = loanRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.lookupService = lookupService;
    }

    /**
     * Builds a full reading report for the given student.
     *
     * @throws EntityNotFoundException if no user exists with the given ID
     */
    public StudentReportDTO buildReport(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Leerling niet gevonden met id: " + studentId));

        LocalDate today = LocalDate.now();

        BorrowCountsDTO counts = new BorrowCountsDTO(
                loanRepository.countByUserIdAndStartBetween(studentId, COUNTED_STATUSES, startOfWeek(today), today),
                loanRepository.countByUserIdAndStartBetween(studentId, COUNTED_STATUSES, today.withDayOfMonth(1),
                        today),
                loanRepository.countByUserIdAndStartBetween(studentId, COUNTED_STATUSES, semesterStart(today), today),
                loanRepository.countByUserIdAndStartBetween(studentId, COUNTED_STATUSES, schoolYearStart(today),
                        today));

        List<BorrowedBookPreviewDTO> preview = loanRepository
                .findByUserIdWithBooks(studentId, COUNTED_STATUSES).stream()
                .flatMap(loan -> loan.getLoanBooks().stream().map(lb -> toPreview(loan, lb, today)))
                .limit(10)
                .toList();

        List<StudentReviewDTO> reviews = reviewRepository.findByUserIdAndHiddenFalse(studentId).stream()
                .map(this::toReviewDTO)
                .toList();

        StudentReportStatsDTO stats = buildStats(studentId, today);

        Map<String, Object> user = lookupService.getUser(student.getSchool().getId(), student.getOneRosterId(),
                student.getRoles());

        String firstName = (String) user.get("givenName");
        String lastName = (String) user.get("familyName");

        return new StudentReportDTO(
                student.getId(),
                firstName,
                lastName,
                counts,
                preview,
                reviews,
                stats);
    }

    /**
     * Computes aggregated statistics for the given student: favourite genre,
     * average rating,
     * and on-time return rate for the current school year.
     */
    private StudentReportStatsDTO buildStats(Long studentId, LocalDate today) {
        List<Object[]> topGenres = loanRepository.findTopGenresByUserId(
                studentId, COUNTED_STATUSES, schoolYearStart(today), today);
        String favoriteGenre = topGenres.isEmpty() ? null : (String) topGenres.get(0)[0];

        Double averageRating = reviewRepository.findAverageRatingByUserId(studentId);
        if (averageRating != null) {
            averageRating = Math.round(averageRating * 10.0) / 10.0;
        }

        List<Loan> returnedLoans = loanRepository.findReturnedByUserId(studentId, LoanStatus.RETURNED);
        int total = returnedLoans.size();
        int onTime = (int) returnedLoans.stream().filter(this::isOnTime).count();
        Double punctuality = total == 0 ? null : Math.round(((double) onTime / total) * 1000.0) / 10.0;

        return new StudentReportStatsDTO(favoriteGenre, averageRating, punctuality, onTime, total);
    }

    /**
     * Returns {@code true} if the loan was returned on or before its due date.
     * Loans without a return date are considered on time.
     */
    private boolean isOnTime(Loan loan) {
        if (loan.getEnd() == null)
            return true;
        LocalDate returned = loan.getReturnedAt();
        if (returned == null)
            return true;
        return !returned.isAfter(loan.getEnd());
    }

    private BorrowedBookPreviewDTO toPreview(Loan loan, LoanBook lb, LocalDate today) {
        boolean returned = loan.getStatus() == LoanStatus.RETURNED || Boolean.TRUE.equals(loan.getClosed());
        boolean overdue = !returned && loan.getEnd() != null && today.isAfter(loan.getEnd());
        boolean requested = loan.getStatus() == LoanStatus.REQUESTED;
        boolean accepted = loan.getStatus() == LoanStatus.ACCEPTED;
        boolean received = loan.getStatus() == LoanStatus.RECEIVED;
        boolean declined = loan.getStatus() == LoanStatus.DECLINED;
        return new BorrowedBookPreviewDTO(
                lb.getBook().getId(),
                lb.getBook().getTitle(),
                lb.getBook().getAuthor() != null ? lb.getBook().getAuthor().getName() : null,
                lb.getBook().getCover(),
                loan.getStart(),
                loan.getEnd(),
                returned,
                overdue,
                requested,
                accepted,
                received,
                declined);
    }

    private StudentReviewDTO toReviewDTO(Review review) {
        return new StudentReviewDTO(
                review.getId(),
                review.getBook() != null ? review.getBook().getId() : null,
                review.getBook() != null ? review.getBook().getTitle() : null,
                review.getBook() != null ? review.getBook().getCover() : null,
                review.getRating(),
                review.getContent(),
                review.getAdded());
    }

    private LocalDate startOfWeek(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }

    // Semester 1: Sep 1 - Jan 31, Semester 2: Feb 1 - Jun 30
    private LocalDate semesterStart(LocalDate date) {
        Month month = date.getMonth();
        if (month.getValue() >= Month.SEPTEMBER.getValue()) {
            return LocalDate.of(date.getYear(), Month.SEPTEMBER, 1);
        }
        if (month.getValue() <= Month.JANUARY.getValue()) {
            return LocalDate.of(date.getYear() - 1, Month.SEPTEMBER, 1);
        }
        return LocalDate.of(date.getYear(), Month.FEBRUARY, 1);
    }

    // School year: Sep 1 -> Jun 30
    private LocalDate schoolYearStart(LocalDate date) {
        if (date.getMonth().getValue() >= Month.SEPTEMBER.getValue()) {
            return LocalDate.of(date.getYear(), Month.SEPTEMBER, 1);
        }
        return LocalDate.of(date.getYear() - 1, Month.SEPTEMBER, 1);
    }
}
