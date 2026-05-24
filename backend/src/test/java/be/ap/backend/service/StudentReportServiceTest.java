// package be.ap.backend.service;

// import be.ap.backend.dto.StudentReportDTO;
// import be.ap.backend.entity.Loan;
// import be.ap.backend.entity.LoanStatus;
// import be.ap.backend.entity.School;
// import be.ap.backend.entity.User;
// import be.ap.backend.entity.UserRole;
// import be.ap.backend.repository.LoanRepository;
// import be.ap.backend.repository.ReviewRepository;
// import be.ap.backend.repository.UserRepository;
// import jakarta.persistence.EntityNotFoundException;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.ArgumentCaptor;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.time.LocalDate;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;
// import java.util.Set;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.assertThatThrownBy;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.when;

// @ExtendWith(MockitoExtension.class)
// public class StudentReportServiceTest {

// @Mock
// private LoanRepository loanRepository;

// @Mock
// private ReviewRepository reviewRepository;

// @Mock
// private UserRepository userRepository;

// @Mock
// private SmartschoolLookupService lookupService;

// @InjectMocks
// private StudentReportService studentReportService;

// private User student;
// private School school;

// @BeforeEach
// void setUp() {
// school = new School();
// school.setId(1L);
// school.setSsSubdomain("testSchool");

// student = new User();
// student.setId(5L);
// student.setUsername("student1");
// student.setRoles(new HashSet<>(Set.of(UserRole.STUDENT)));
// student.setSchool(school);
// student.setOneRosterId("roster-001");
// }

// /**
// * Stubs all repository/service calls needed for a minimal valid report,
// * except countByUserIdAndStartBetween — stub that separately when you need
// * to capture its arguments.
// */
// private void stubEmptyReportWithoutCount() {
// when(loanRepository.findByUserIdWithBooks(eq(5L),
// anyList())).thenReturn(List.of());
// when(reviewRepository.findByUserIdAndHiddenFalse(5L)).thenReturn(List.of());
// when(loanRepository.findTopGenresByUserId(eq(5L), anyList(), any(),
// any())).thenReturn(List.of());
// when(reviewRepository.findAverageRatingByUserId(5L)).thenReturn(null);
// when(loanRepository.findReturnedByUserId(5L,
// LoanStatus.RETURNED)).thenReturn(List.of());
// when(lookupService.getUser(eq(school), eq("roster-001"), eq("student")))
// .thenReturn(Map.of("givenName", "Anna", "familyName", "De Wolf"));
// }

// private void stubEmptyReport() {
// when(loanRepository.countByUserIdAndStartBetween(eq(5L), anyList(), any(),
// any())).thenReturn(0);
// stubEmptyReportWithoutCount();
// }

// // ── buildReport ───────────────────────────────────────────────

// @Test
// void buildReport_studentNotFound_throwsEntityNotFoundException() {
// when(userRepository.findById(99L)).thenReturn(Optional.empty());

// assertThatThrownBy(() -> studentReportService.buildReport(99L))
// .isInstanceOf(EntityNotFoundException.class)
// .hasMessageContaining("Leerling niet gevonden");
// }

// @Test
// void buildReport_returnsReportWithStudentInfo() {
// when(userRepository.findById(5L)).thenReturn(Optional.of(student));
// stubEmptyReport();

// StudentReportDTO result = studentReportService.buildReport(5L);

// assertThat(result).isNotNull();
// assertThat(result.getStudentId()).isEqualTo(5L);
// }

// @Test
// void buildReport_firstAndLastNameFromSmartschool() {
// when(userRepository.findById(5L)).thenReturn(Optional.of(student));
// stubEmptyReport();

// StudentReportDTO result = studentReportService.buildReport(5L);

// assertThat(result.getFirstName()).isEqualTo("Anna");
// assertThat(result.getLastName()).isEqualTo("De Wolf");
// }

// @Test
// void buildReport_requestedLoansNotCounted() {
// when(userRepository.findById(5L)).thenReturn(Optional.of(student));
// stubEmptyReportWithoutCount();

// @SuppressWarnings("unchecked")
// ArgumentCaptor<List<LoanStatus>> statusCaptor =
// (ArgumentCaptor<List<LoanStatus>>) (ArgumentCaptor<?>) ArgumentCaptor
// .forClass(List.class);
// when(loanRepository.countByUserIdAndStartBetween(eq(5L),
// statusCaptor.capture(), any(), any())).thenReturn(0);

// studentReportService.buildReport(5L);

// List<LoanStatus> capturedStatuses = statusCaptor.getValue();
// assertThat(capturedStatuses).doesNotContain(LoanStatus.REQUESTED);
// assertThat(capturedStatuses).contains(LoanStatus.ACCEPTED,
// LoanStatus.RECEIVED, LoanStatus.RETURNED);
// }

// @Test
// void buildReport_noReturnedLoans_punctualityIsNull() {
// when(userRepository.findById(5L)).thenReturn(Optional.of(student));
// stubEmptyReport();

// StudentReportDTO result = studentReportService.buildReport(5L);

// assertThat(result.getStats().getPunctualityRate()).isNull();
// assertThat(result.getStats().getTotalReturns()).isEqualTo(0);
// }

// @Test
// void buildReport_allReturnedOnTime_punctualityIs100() {
// when(userRepository.findById(5L)).thenReturn(Optional.of(student));
// stubEmptyReport();

// Loan onTimeLoan = new Loan();
// onTimeLoan.setEnd(LocalDate.now().minusDays(1));
// onTimeLoan.setReturnedAt(LocalDate.now().minusDays(2));
// when(loanRepository.findReturnedByUserId(5L,
// LoanStatus.RETURNED)).thenReturn(List.of(onTimeLoan));

// StudentReportDTO result = studentReportService.buildReport(5L);

// assertThat(result.getStats().getPunctualityRate()).isEqualTo(100.0);
// assertThat(result.getStats().getOnTimeReturns()).isEqualTo(1);
// assertThat(result.getStats().getTotalReturns()).isEqualTo(1);
// }

// @Test
// void buildReport_overdueReturn_punctualityIs0() {
// when(userRepository.findById(5L)).thenReturn(Optional.of(student));
// stubEmptyReport();

// Loan overdueLoan = new Loan();
// overdueLoan.setEnd(LocalDate.now().minusDays(5));
// overdueLoan.setReturnedAt(LocalDate.now().minusDays(1));
// when(loanRepository.findReturnedByUserId(5L,
// LoanStatus.RETURNED)).thenReturn(List.of(overdueLoan));

// StudentReportDTO result = studentReportService.buildReport(5L);

// assertThat(result.getStats().getPunctualityRate()).isEqualTo(0.0);
// assertThat(result.getStats().getOnTimeReturns()).isEqualTo(0);
// }

// @Test
// void buildReport_averageRatingRoundedToOneDecimal() {
// when(userRepository.findById(5L)).thenReturn(Optional.of(student));
// stubEmptyReport();
// when(reviewRepository.findAverageRatingByUserId(5L)).thenReturn(3.666);

// StudentReportDTO result = studentReportService.buildReport(5L);

// assertThat(result.getStats().getAverageRating()).isEqualTo(3.7);
// }

// @Test
// void buildReport_previewLimitedToTen() {
// when(userRepository.findById(5L)).thenReturn(Optional.of(student));
// stubEmptyReport();

// StudentReportDTO result = studentReportService.buildReport(5L);

// assertThat(result.getBorrowedBooksPreview()).hasSizeLessThanOrEqualTo(10);
// }
// }