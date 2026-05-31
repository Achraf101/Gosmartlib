package be.ap.backend.service;

import be.ap.backend.dto.StudentReportDTO;
import be.ap.backend.entity.Loan;
import be.ap.backend.entity.School;
import be.ap.backend.entity.User;
import be.ap.backend.entity.UserRole;
import be.ap.backend.enums.LoanStatus;
import be.ap.backend.repository.LoanRepository;
import be.ap.backend.repository.ReviewRepository;
import be.ap.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentReportServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SmartschoolLookupService lookupService;

    @InjectMocks
    private StudentReportService studentReportService;

    private User student;
    private School school;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1L);
        school.setSsSubdomain("testSchool");

        student = new User();
        student.setId(5L);
        student.setUsername("student1");
        student.setRoles(new HashSet<>(Set.of(UserRole.STUDENT)));
        student.setSchool(school);
        student.setOneRosterId("roster-001");
    }

    private void stubEmptyReportWithoutCount() {
        when(loanRepository.findByUserIdWithBooks(eq(5L), anyList())).thenReturn(List.of());
        when(reviewRepository.findByUserIdAndHiddenFalse(5L)).thenReturn(List.of());
        when(loanRepository.findTopGenresByUserId(eq(5L), anyList(), any(), any()))
                .thenReturn(List.of());
        when(reviewRepository.findAverageRatingByUserId(5L)).thenReturn(null);
        when(loanRepository.findReturnedByUserId(5L, LoanStatus.RETURNED))
                .thenReturn(List.of());

        when(lookupService.getUser(eq(school.getId()), eq("roster-001"), anySet()))
                .thenReturn(Map.of("givenName", "Anna", "familyName", "De Wolf"));
    }

    private void stubEmptyReport() {
        when(loanRepository.countByUserIdAndStartBetween(eq(5L), anyList(), any(), any()))
                .thenReturn(0);
        stubEmptyReportWithoutCount();
    }

    // ─────────────────────────────────────────────
    // buildReport
    // ─────────────────────────────────────────────

    @Test
    void buildReport_studentNotFound_throwsEntityNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentReportService.buildReport(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Leerling niet gevonden");
    }

    @Test
    void buildReport_returnsReportWithStudentInfo() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(student));
        stubEmptyReport();

        StudentReportDTO result = studentReportService.buildReport(5L);

        assertThat(result).isNotNull();
        assertThat(result.getStudentId()).isEqualTo(5L);
    }

    @Test
    void buildReport_firstAndLastNameFromSmartschool() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(student));
        stubEmptyReport();

        StudentReportDTO result = studentReportService.buildReport(5L);

        assertThat(result.getFirstName()).isEqualTo("Anna");
        assertThat(result.getLastName()).isEqualTo("De Wolf");
    }

    @Test
    void buildReport_noReturnedLoans_punctualityIsNull() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(student));
        stubEmptyReport();

        StudentReportDTO result = studentReportService.buildReport(5L);

        assertThat(result.getStats().getPunctualityRate()).isNull();
        assertThat(result.getStats().getTotalReturns()).isEqualTo(0);
    }

    @Test
    void buildReport_allReturnedOnTime_punctualityIs100() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(student));
        stubEmptyReport();

        Loan loan = new Loan();
        loan.setEnd(LocalDate.now().minusDays(1));
        loan.setReturnedAt(LocalDate.now().minusDays(2));

        when(loanRepository.findReturnedByUserId(5L, LoanStatus.RETURNED))
                .thenReturn(List.of(loan));

        StudentReportDTO result = studentReportService.buildReport(5L);

        assertThat(result.getStats().getPunctualityRate()).isEqualTo(100.0);
        assertThat(result.getStats().getOnTimeReturns()).isEqualTo(1);
        assertThat(result.getStats().getTotalReturns()).isEqualTo(1);
    }

    @Test
    void buildReport_overdueReturn_punctualityIs0() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(student));
        stubEmptyReport();

        Loan loan = new Loan();
        loan.setEnd(LocalDate.now().minusDays(5));
        loan.setReturnedAt(LocalDate.now().minusDays(1));

        when(loanRepository.findReturnedByUserId(5L, LoanStatus.RETURNED))
                .thenReturn(List.of(loan));

        StudentReportDTO result = studentReportService.buildReport(5L);

        assertThat(result.getStats().getPunctualityRate()).isEqualTo(0.0);
        assertThat(result.getStats().getOnTimeReturns()).isEqualTo(0);
    }

    @Test
    void buildReport_averageRatingRoundedToOneDecimal() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(student));
        stubEmptyReport();

        when(reviewRepository.findAverageRatingByUserId(5L)).thenReturn(3.666);

        StudentReportDTO result = studentReportService.buildReport(5L);

        assertThat(result.getStats().getAverageRating()).isEqualTo(3.7);
    }

    @Test
    void buildReport_previewLimitedToTen() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(student));
        stubEmptyReport();

        StudentReportDTO result = studentReportService.buildReport(5L);

        assertThat(result.getBorrowedBooksPreview().size()).isLessThanOrEqualTo(10);
    }

    @Test
    void buildReport_countStatuses_doNotIncludeRequested() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(student));
        stubEmptyReport();

        studentReportService.buildReport(5L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<LoanStatus>> captor = ArgumentCaptor.forClass(List.class);

        verify(loanRepository, times(4))
                .countByUserIdAndStartBetween(eq(5L), captor.capture(), any(), any());

        List<List<LoanStatus>> allCalls = captor.getAllValues();

        for (List<LoanStatus> statuses : allCalls) {
            assertThat(statuses).doesNotContain(LoanStatus.REQUESTED);
            assertThat(statuses).containsExactlyInAnyOrder(
                    LoanStatus.ACCEPTED,
                    LoanStatus.RECEIVED,
                    LoanStatus.RETURNED);
        }
    }
}