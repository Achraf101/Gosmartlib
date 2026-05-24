// package be.ap.backend.service;

// import be.ap.backend.dto.LoanBookDTO;
// import be.ap.backend.dto.LoanDTO;
// import be.ap.backend.dto.TopBookDTO;
// import be.ap.backend.entity.*;
// import be.ap.backend.repository.LocationBookRepository;
// import be.ap.backend.repository.LoanBookRepository;
// import be.ap.backend.repository.LoanRepository;
// import jakarta.persistence.EntityManager;
// import jakarta.persistence.EntityNotFoundException;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.time.LocalDate;
// import java.util.ArrayList;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;
// import java.util.Set;
// import java.util.concurrent.Executor;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.assertThatThrownBy;
// import static org.assertj.core.api.Assertions.assertThatNoException;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyList;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// public class LoanServiceTest {

// @Mock
// private LoanRepository loanRepository;

// @Mock
// private EntityManager entityManager;

// @Mock
// private LoanBookRepository loanBookRepository;

// @Mock
// private LocationBookRepository locationBookRepository;

// @Mock
// private LocationBookService locationBookService;

// // Required by LoanService constructor — used in toDTO() via toDTOs()
// @Mock
// private SmartschoolLookupService lookupService;

// // Required by LoanService constructor — drives the CompletableFuture pool in
// // toDTOs()
// @Mock
// private Executor lookupExecutor;

// @InjectMocks
// private LoanService loanService;

// private User user;
// private Location location;
// private Book book;
// private LocationBook locationBook;
// private Loan loan;
// private School school;

// @BeforeEach
// void setUp() {
// school = new School();
// school.setId(1L);
// school.setBorrowLimit(5);
// school.setBorrowPeriod(14);

// user = new User();
// user.setId(1L);
// user.setSchool(school);
// user.setRoles(new HashSet<>(Set.of(UserRole.STUDENT)));

// location = new Location();
// location.setId(1L);
// location.setSchool(school);

// book = new Book();
// book.setId(1L);
// book.setTitle("De brief voor de koning");

// locationBook = new LocationBook();
// locationBook.setId(1L);
// locationBook.setLocation(location);
// locationBook.setBook(book);
// locationBook.setAmount(3);
// locationBook.setCurrentAmount(3);

// loan = new Loan();
// loan.setId(1L);
// loan.setUser(user);
// loan.setLocation(location);
// loan.setStart(LocalDate.now().plusDays(1));
// loan.setEnd(LocalDate.now().plusDays(15));
// loan.setStatus(LoanStatus.REQUESTED);
// loan.setLoanBooks(new HashSet<>());

// // Default stub: lookupService returns a display name so toDTO() never NPEs.
// // Individual tests that don't reach toDTO() can leave this unused (lenient).
// lenient().when(lookupService.getUser(any(), any(), anyString()))
// .thenReturn(Map.of("givenName", "Jan", "familyName", "Peeters"));

// // Default stub: execute the Runnable inline so CompletableFuture in toDTOs()
// // completes synchronously in the test thread.
// lenient().doAnswer(inv -> {
// ((Runnable) inv.getArgument(0)).run();
// return null;
// }).when(lookupExecutor).execute(any(Runnable.class));
// }

// // ── createLoan ────────────────────────────────────────────────

// @Test
// void createLoan_success() {
// LoanDTO dto = validLoanDTO();

// when(entityManager.find(User.class, 1L)).thenReturn(user);
// when(entityManager.find(Location.class, 1L)).thenReturn(location);
// when(entityManager.find(Book.class, 1L)).thenReturn(book);
// when(locationBookRepository.findByLocationIdAndBookId(1L, 1L))
// .thenReturn(Optional.of(locationBook));
// when(loanRepository.save(any())).thenAnswer(i -> i.getArgument(0));

// List<LoanDTO> result = loanService.createLoan(dto);

// assertThat(result).isNotNull().hasSize(1);
// assertThat(result.get(0).getUserId()).isEqualTo(1L);
// verify(loanRepository).save(any());
// verify(loanBookRepository).save(any());
// verify(locationBookService).updateCurrentAmount(locationBook, 1);
// }

// @Test
// void createLoan_missingUserId_throwsIllegalArgument() {
// LoanDTO dto = validLoanDTO();
// dto.setUserId(null);

// assertThatThrownBy(() -> loanService.createLoan(dto))
// .isInstanceOf(IllegalArgumentException.class)
// .hasMessageContaining("userId is verplicht");
// }

// @Test
// void createLoan_missingStart_throwsIllegalArgument() {
// LoanDTO dto = validLoanDTO();
// dto.setStart(null);

// assertThatThrownBy(() -> loanService.createLoan(dto))
// .isInstanceOf(IllegalArgumentException.class)
// .hasMessageContaining("Begin- en einddatum zijn verplicht");
// }

// @Test
// void createLoan_missingEnd_throwsIllegalArgument() {
// LoanDTO dto = validLoanDTO();
// dto.setEnd(null);

// assertThatThrownBy(() -> loanService.createLoan(dto))
// .isInstanceOf(IllegalArgumentException.class)
// .hasMessageContaining("Begin- en einddatum zijn verplicht");
// }

// @Test
// void createLoan_startInPast_throwsIllegalArgument() {
// LoanDTO dto = validLoanDTO();
// dto.setStart(LocalDate.now().minusDays(1));

// assertThatThrownBy(() -> loanService.createLoan(dto))
// .isInstanceOf(IllegalArgumentException.class)
// .hasMessageContaining("Begindatum kan niet in het verleden liggen");
// }

// @Test
// void createLoan_startAfterEnd_throwsIllegalArgument() {
// LoanDTO dto = validLoanDTO();
// dto.setStart(LocalDate.now().plusDays(5));
// dto.setEnd(LocalDate.now().plusDays(3));

// assertThatThrownBy(() -> loanService.createLoan(dto))
// .isInstanceOf(IllegalArgumentException.class)
// .hasMessageContaining("Begindatum moet voor einddatum zijn");
// }

// @Test
// void createLoan_noBooks_throwsIllegalArgument() {
// LoanDTO dto = validLoanDTO();
// dto.setBooks(new LoanBookDTO[0]);

// assertThatThrownBy(() -> loanService.createLoan(dto))
// .isInstanceOf(IllegalArgumentException.class)
// .hasMessageContaining("minstens 1 boek is verplicht");
// }

// @Test
// void createLoan_bookMissingBookId_throwsIllegalArgument() {
// LoanDTO dto = validLoanDTO();
// dto.getBooks()[0].setBookId(null);

// when(entityManager.find(User.class, 1L)).thenReturn(user);
// when(entityManager.find(Location.class, 1L)).thenReturn(location);

// assertThatThrownBy(() -> loanService.createLoan(dto))
// .isInstanceOf(IllegalArgumentException.class)
// .hasMessageContaining("bookId is verplicht");
// }

// @Test
// void createLoan_bookZeroAmount_throwsIllegalArgument() {
// LoanDTO dto = validLoanDTO();
// dto.getBooks()[0].setRequestedAmount(0);

// when(entityManager.find(User.class, 1L)).thenReturn(user);
// when(entityManager.find(Location.class, 1L)).thenReturn(location);

// assertThatThrownBy(() -> loanService.createLoan(dto))
// .isInstanceOf(IllegalArgumentException.class)
// .hasMessageContaining("aangevraagde hoeveelheid moet groter zijn dan 0");
// }

// @Test
// void createLoan_userNotFound_throwsEntityNotFoundException() {
// LoanDTO dto = validLoanDTO();

// when(entityManager.find(User.class, 1L)).thenReturn(null);

// assertThatThrownBy(() -> loanService.createLoan(dto))
// .isInstanceOf(EntityNotFoundException.class)
// .hasMessageContaining("Gebruiker niet gevonden");
// }

// @Test
// void createLoan_locationNotFound_throwsEntityNotFoundException() {
// LoanDTO dto = validLoanDTO();

// when(entityManager.find(User.class, 1L)).thenReturn(user);
// when(entityManager.find(Location.class, 1L)).thenReturn(null);

// assertThatThrownBy(() -> loanService.createLoan(dto))
// .isInstanceOf(EntityNotFoundException.class)
// .hasMessageContaining("Locatie niet gevonden");
// }

// @Test
// void createLoan_exceedsBorrowLimit_throwsIllegalArgument() {
// school.setBorrowLimit(1);

// LoanBookDTO extra = new LoanBookDTO();
// extra.setBookId(2L);
// extra.setRequestedAmount(1);

// LoanDTO dto = validLoanDTO();
// dto.setBooks(new LoanBookDTO[] { dto.getBooks()[0], extra });

// when(entityManager.find(User.class, 1L)).thenReturn(user);
// when(entityManager.find(Location.class, 1L)).thenReturn(location);

// assertThatThrownBy(() -> loanService.createLoan(dto))
// .isInstanceOf(IllegalArgumentException.class)
// .hasMessageContaining("uitleen limiet");
// }

// @Test
// void createLoan_wrongEndDate_throwsIllegalArgument() {
// LoanDTO dto = validLoanDTO();
// dto.setEnd(dto.getStart().plusDays(99));

// when(entityManager.find(User.class, 1L)).thenReturn(user);
// when(entityManager.find(Location.class, 1L)).thenReturn(location);

// assertThatThrownBy(() -> loanService.createLoan(dto))
// .isInstanceOf(IllegalArgumentException.class)
// .hasMessageContaining("14 dagen na begindatum");
// }

// @Test
// void createLoan_notEnoughCurrentAmount_throwsIllegalArgument() {
// locationBook.setCurrentAmount(0);
// LoanDTO dto = validLoanDTO();

// when(entityManager.find(User.class, 1L)).thenReturn(user);
// when(entityManager.find(Location.class, 1L)).thenReturn(location);
// when(entityManager.find(Book.class, 1L)).thenReturn(book);
// when(locationBookRepository.findByLocationIdAndBookId(1L, 1L))
// .thenReturn(Optional.of(locationBook));

// assertThatThrownBy(() -> loanService.createLoan(dto))
// .isInstanceOf(IllegalArgumentException.class)
// .hasMessageContaining("niet meer beschikbaar");
// }

// @Test
// void createLoan_bookNotFoundInEntityManager_throwsEntityNotFoundException() {
// LoanDTO dto = validLoanDTO();

// when(entityManager.find(User.class, 1L)).thenReturn(user);
// when(entityManager.find(Location.class, 1L)).thenReturn(location);
// when(entityManager.find(Book.class, 1L)).thenReturn(null);

// assertThatThrownBy(() -> loanService.createLoan(dto))
// .isInstanceOf(EntityNotFoundException.class)
// .hasMessageContaining("Boek niet gevonden met id");
// }

// @Test
// void createLoan_bookNotFoundOnLocation_throwsEntityNotFoundException() {
// LoanDTO dto = validLoanDTO();

// when(entityManager.find(User.class, 1L)).thenReturn(user);
// when(entityManager.find(Location.class, 1L)).thenReturn(location);
// when(entityManager.find(Book.class, 1L)).thenReturn(book);
// when(locationBookRepository.findByLocationIdAndBookId(1L, 1L))
// .thenReturn(Optional.empty());

// assertThatThrownBy(() -> loanService.createLoan(dto))
// .isInstanceOf(EntityNotFoundException.class)
// .hasMessageContaining("Boek niet gevonden in locatie");
// }

// @Test
// void createLoan_singleBook_groupIdIsNull() {
// LoanDTO dto = validLoanDTO();

// when(entityManager.find(User.class, 1L)).thenReturn(user);
// when(entityManager.find(Location.class, 1L)).thenReturn(location);
// when(entityManager.find(Book.class, 1L)).thenReturn(book);
// when(locationBookRepository.findByLocationIdAndBookId(1L, 1L))
// .thenReturn(Optional.of(locationBook));
// when(loanRepository.save(any())).thenAnswer(i -> i.getArgument(0));

// List<LoanDTO> result = loanService.createLoan(dto);

// assertThat(result).hasSize(1);
// assertThat(result.get(0).getGroupId()).isNull();
// }

// @Test
// void createLoan_multipleBooks_sameGroupId() {
// Book book2entity = new Book();
// book2entity.setId(2L);
// book2entity.setTitle("Tweede boek");

// LocationBook locationBook2 = new LocationBook();
// locationBook2.setId(2L);
// locationBook2.setLocation(location);
// locationBook2.setBook(book2entity);
// locationBook2.setAmount(3);
// locationBook2.setCurrentAmount(3);

// LoanBookDTO book1 = new LoanBookDTO();
// book1.setBookId(1L);
// book1.setRequestedAmount(1);

// LoanBookDTO book2 = new LoanBookDTO();
// book2.setBookId(2L);
// book2.setRequestedAmount(1);

// LoanDTO dto = validLoanDTO();
// dto.setBooks(new LoanBookDTO[] { book1, book2 });

// when(entityManager.find(User.class, 1L)).thenReturn(user);
// when(entityManager.find(Location.class, 1L)).thenReturn(location);
// when(entityManager.find(Book.class, 1L)).thenReturn(book);
// when(entityManager.find(Book.class, 2L)).thenReturn(book2entity);
// when(locationBookRepository.findByLocationIdAndBookId(1L, 1L))
// .thenReturn(Optional.of(locationBook));
// when(locationBookRepository.findByLocationIdAndBookId(1L, 2L))
// .thenReturn(Optional.of(locationBook2));
// when(loanRepository.save(any())).thenAnswer(i -> i.getArgument(0));

// List<LoanDTO> result = loanService.createLoan(dto);

// assertThat(result).hasSize(2);
// assertThat(result.get(0).getGroupId()).isNotNull();
// assertThat(result.get(1).getGroupId()).isEqualTo(result.get(0).getGroupId());
// }

// // ── getRequested ──────────────────────────────────────────────

// @Test
// void getRequested_returnsListOfDTOs() {
// loan.setLoanBooks(new HashSet<>());
// when(loanRepository.findByStatusWithBooks(LoanStatus.REQUESTED)).thenReturn(List.of(loan));

// List<LoanDTO> result = loanService.getRequested();

// assertThat(result).hasSize(1);
// assertThat(result.get(0).getUserId()).isEqualTo(1L);
// }

// @Test
// void getRequested_empty_returnsEmptyList() {
// when(loanRepository.findByStatusWithBooks(LoanStatus.REQUESTED)).thenReturn(List.of());

// List<LoanDTO> result = loanService.getRequested();

// assertThat(result).isEmpty();
// }

// // ── updateNote ────────────────────────────────────────────────

// @Test
// void updateNote_success() {
// loan.setLoanBooks(new HashSet<>());
// when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
// when(loanRepository.save(loan)).thenReturn(loan);

// loanService.updateNote(1L, "Nieuwe opmerking");

// assertThat(loan.getNote()).isEqualTo("Nieuwe opmerking");
// verify(loanRepository).save(loan);
// }

// @Test
// void updateNote_notFound_throwsEntityNotFoundException() {
// when(loanRepository.findById(99L)).thenReturn(Optional.empty());

// assertThatThrownBy(() -> loanService.updateNote(99L, "test"))
// .isInstanceOf(EntityNotFoundException.class)
// .hasMessageContaining("Loan niet gevonden");
// }

// @Test
// void updateNote_tooLong_throwsIllegalArgument() {
// when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

// String longNote = "a".repeat(256);

// assertThatThrownBy(() -> loanService.updateNote(1L, longNote))
// .isInstanceOf(IllegalArgumentException.class)
// .hasMessageContaining("te lang");
// }

// @Test
// void updateNote_exactly255Chars_succeeds() {
// loan.setLoanBooks(new HashSet<>());
// String boundaryNote = "a".repeat(255);
// when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
// when(loanRepository.save(loan)).thenReturn(loan);

// assertThatNoException().isThrownBy(() -> loanService.updateNote(1L,
// boundaryNote));
// assertThat(loan.getNote()).isEqualTo(boundaryNote);
// }

// // ── updateStatus ──────────────────────────────────────────────

// @Test
// void updateStatus_success() {
// loan.setLoanBooks(new HashSet<>());
// when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
// when(loanRepository.save(loan)).thenReturn(loan);

// loanService.updateStatus(1L, LoanStatus.ACCEPTED);

// assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACCEPTED);
// verify(loanRepository).save(loan);
// }

// @Test
// void updateStatus_notFound_throwsEntityNotFoundException() {
// when(loanRepository.findById(99L)).thenReturn(Optional.empty());

// assertThatThrownBy(() -> loanService.updateStatus(99L, LoanStatus.ACCEPTED))
// .isInstanceOf(EntityNotFoundException.class)
// .hasMessageContaining("Loan niet gevonden");
// }

// @Test
// void updateStatus_declined_restoresCurrentAmount() {
// LoanBook loanBook = new LoanBook();
// loanBook.setBook(book);
// loanBook.setRequestedAmount(2);
// loan.setLoanBooks(Set.of(loanBook));

// when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
// when(locationBookRepository.findByLocationIdAndBookId(1L, 1L))
// .thenReturn(Optional.of(locationBook));
// when(loanRepository.save(loan)).thenReturn(loan);

// loanService.updateStatus(1L, LoanStatus.DECLINED);

// verify(locationBookService).updateCurrentAmount(locationBook, -2);
// }

// @Test
// void
// updateStatus_declined_locationBookNotFound_throwsEntityNotFoundException() {
// LoanBook loanBook = new LoanBook();
// loanBook.setBook(book);
// loanBook.setRequestedAmount(2);
// loan.setLoanBooks(Set.of(loanBook));

// when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
// when(locationBookRepository.findByLocationIdAndBookId(1L, 1L))
// .thenReturn(Optional.empty());

// assertThatThrownBy(() -> loanService.updateStatus(1L, LoanStatus.DECLINED))
// .isInstanceOf(EntityNotFoundException.class)
// .hasMessageContaining("Book not found on location");
// }

// // ── getByUserId ───────────────────────────────────────────────

// @Test
// void getByUserId_returnsLoansForUser() {
// loan.setLoanBooks(new HashSet<>());
// when(loanRepository.findByUserId(1L)).thenReturn(List.of(loan));

// List<LoanDTO> result = loanService.getByUserId(1L);

// assertThat(result).hasSize(1);
// assertThat(result.get(0).getUserId()).isEqualTo(1L);
// }

// @Test
// void getByUserId_noLoans_returnsEmptyList() {
// when(loanRepository.findByUserId(99L)).thenReturn(List.of());

// List<LoanDTO> result = loanService.getByUserId(99L);

// assertThat(result).isEmpty();
// }

// // ── deleteLoan ────────────────────────────────────────────────

// @Test
// void deleteLoan_success() {
// when(loanRepository.existsById(1L)).thenReturn(true);

// assertThatNoException().isThrownBy(() -> loanService.deleteLoan(1L));

// verify(loanRepository).deleteById(1L);
// }

// @Test
// void deleteLoan_notFound_throwsRuntimeException() {
// when(loanRepository.existsById(99L)).thenReturn(false);

// assertThatThrownBy(() -> loanService.deleteLoan(99L))
// .isInstanceOf(RuntimeException.class)
// .hasMessageContaining("Uitlening niet gevonden");
// }

// // ── getByStateAndSchool ───────────────────────────────────────
// // NOTE: The service method is getByStateAndSchool(state, schoolId),
// // not getByStateAndLocation. Tests updated accordingly.

// @Test
// void getByStateAndSchool_returnsMatchingLoans() {
// loan.setStatus(LoanStatus.ACCEPTED);
// loan.setLoanBooks(new HashSet<>());
// when(loanRepository.findByStateAndSchool(LoanStatus.ACCEPTED, 1L))
// .thenReturn(List.of(loan));

// List<LoanDTO> result = loanService.getByStateAndSchool(LoanStatus.ACCEPTED,
// 1L);

// assertThat(result).hasSize(1);
// assertThat(result.get(0).getUserId()).isEqualTo(1L);
// }

// @Test
// void getByStateAndSchool_empty_returnsEmptyList() {
// when(loanRepository.findByStateAndSchool(LoanStatus.DECLINED, 1L))
// .thenReturn(List.of());

// List<LoanDTO> result = loanService.getByStateAndSchool(LoanStatus.DECLINED,
// 1L);

// assertThat(result).isEmpty();
// }

// // ── getOverdueLoans ───────────────────────────────────────────

// @Test
// void getOverdueLoans_returnsListOfDTOs() {
// loan.setStatus(LoanStatus.RECEIVED);
// loan.setEnd(LocalDate.now().minusDays(1));
// loan.setLoanBooks(new HashSet<>());

// when(loanRepository.findOverdueLoans(anyList(), any(LocalDate.class),
// eq(1L)))
// .thenReturn(List.of(loan));

// List<LoanDTO> result = loanService.getOverdueLoans(1L);

// assertThat(result).hasSize(1);
// assertThat(result.get(0).getUserId()).isEqualTo(1L);
// }

// @Test
// void getOverdueLoans_empty_returnsEmptyList() {
// when(loanRepository.findOverdueLoans(anyList(), any(LocalDate.class),
// eq(1L)))
// .thenReturn(List.of());

// List<LoanDTO> result = loanService.getOverdueLoans(1L);

// assertThat(result).isEmpty();
// }

// @Test
// void getOverdueLoansLength_returnsCount() {
// when(loanRepository.countOverdueLoans(anyList(), any(LocalDate.class),
// eq(1L)))
// .thenReturn(3);

// int result = loanService.getOverdueLoansLength(1L);

// assertThat(result).isEqualTo(3);
// }

// @Test
// void getOverdueLoansLength_noOverdue_returnsZero() {
// when(loanRepository.countOverdueLoans(anyList(), any(LocalDate.class),
// eq(1L)))
// .thenReturn(0);

// int result = loanService.getOverdueLoansLength(1L);

// assertThat(result).isEqualTo(0);
// }

// // ── getDueSoonLoans ───────────────────────────────────────────

// @Test
// void getDueSoonLoans_returnsListOfDTOs() {
// loan.setStatus(LoanStatus.RECEIVED);
// loan.setEnd(LocalDate.now().plusDays(3));
// loan.setLoanBooks(new HashSet<>());

// when(loanRepository.findDueSoonLoans(anyList(), any(LocalDate.class),
// any(LocalDate.class), eq(1L)))
// .thenReturn(List.of(loan));

// List<LoanDTO> result = loanService.getDueSoonLoans(1L);

// assertThat(result).hasSize(1);
// assertThat(result.get(0).getUserId()).isEqualTo(1L);
// }

// @Test
// void getDueSoonLoans_empty_returnsEmptyList() {
// when(loanRepository.findDueSoonLoans(anyList(), any(LocalDate.class),
// any(LocalDate.class), eq(1L)))
// .thenReturn(List.of());

// List<LoanDTO> result = loanService.getDueSoonLoans(1L);

// assertThat(result).isEmpty();
// }

// @Test
// void getDueSoonLoansLength_returnsCount() {
// when(loanRepository.countDueSoonLoans(anyList(), any(LocalDate.class),
// any(LocalDate.class), eq(1L)))
// .thenReturn(7);

// int result = loanService.getDueSoonLoansLength(1L);

// assertThat(result).isEqualTo(7);
// }

// @Test
// void getDueSoonLoansLength_noSoonDue_returnsZero() {
// when(loanRepository.countDueSoonLoans(anyList(), any(LocalDate.class),
// any(LocalDate.class), eq(1L)))
// .thenReturn(0);

// int result = loanService.getDueSoonLoansLength(1L);

// assertThat(result).isEqualTo(0);
// }

// // ── getTopBooksThisMonth ──────────────────────────────────────

// @Test
// void getTopBooksThisMonth_returnsTopBooks() {
// LoanBook loanBook = new LoanBook();
// loanBook.setBook(book);
// loan.setLoanBooks(Set.of(loanBook));

// when(loanRepository.findByDateRangeWithBooks(any(), any(), anyList(),
// eq(1L)))
// .thenReturn(List.of(loan));

// List<TopBookDTO> result = loanService.getTopBooksThisMonth(1L);

// assertThat(result).isNotEmpty();
// assertThat(result.get(0).title()).isEqualTo("De brief voor de koning");
// assertThat(result.get(0).count()).isEqualTo(1);
// }

// @Test
// void getTopBooksThisMonth_limitsToFive() {
// List<Loan> loans = new ArrayList<>();
// for (int i = 1; i <= 6; i++) {
// Book b = new Book();
// b.setId((long) i);
// b.setTitle("Boek " + i);
// LoanBook lb = new LoanBook();
// lb.setBook(b);
// Loan l = new Loan();
// l.setLoanBooks(Set.of(lb));
// loans.add(l);
// }

// when(loanRepository.findByDateRangeWithBooks(any(), any(), anyList(),
// eq(1L)))
// .thenReturn(loans);

// List<TopBookDTO> result = loanService.getTopBooksThisMonth(1L);

// assertThat(result.size()).isLessThanOrEqualTo(5);
// }

// @Test
// void getTopBooksThisMonth_empty_returnsEmptyList() {
// when(loanRepository.findByDateRangeWithBooks(any(), any(), anyList(),
// eq(1L)))
// .thenReturn(List.of());

// List<TopBookDTO> result = loanService.getTopBooksThisMonth(1L);

// assertThat(result).isEmpty();
// }

// // ── getTopGenresThisMonth ─────────────────────────────────────

// @Test
// void getTopGenresThisMonth_returnsTopGenres() {
// Object[] row = { "Fantasy", 5L };
// List<Object[]> rows = new ArrayList<>();
// rows.add(row);
// when(loanRepository.findTopGenres(anyList(), any(), any(), eq(1L)))
// .thenReturn(rows);

// List<TopBookDTO> result = loanService.getTopGenresThisMonth(1L);

// assertThat(result).hasSize(1);
// assertThat(result.get(0).title()).isEqualTo("Fantasy");
// assertThat(result.get(0).count()).isEqualTo(5);
// }

// @Test
// void getTopGenresThisMonth_empty_returnsEmptyList() {
// when(loanRepository.findTopGenres(anyList(), any(), any(), eq(1L)))
// .thenReturn(List.of());

// List<TopBookDTO> result = loanService.getTopGenresThisMonth(1L);

// assertThat(result).isEmpty();
// }

// @Test
// void getTopGenresThisMonth_limitsToFive() {
// List<Object[]> rows = List.of(
// new Object[] { "Fantasy", 10L },
// new Object[] { "Horror", 9L },
// new Object[] { "Humor", 8L },
// new Object[] { "Avontuur", 7L },
// new Object[] { "Poëzie", 6L },
// new Object[] { "Romantiek", 5L });

// when(loanRepository.findTopGenres(anyList(), any(), any(), eq(1L)))
// .thenReturn(rows);

// List<TopBookDTO> result = loanService.getTopGenresThisMonth(1L);

// assertThat(result.size()).isLessThanOrEqualTo(5);
// }

// // ── extendLoan ────────────────────────────────────────────────

// @Test
// void extendLoan_success() {
// school.setExtendLimit(3);
// school.setExtendPeriod(7);
// location.setSchool(school);
// loan.setStatus(LoanStatus.RECEIVED);
// loan.setExtended((byte) 0);
// loan.setEnd(LocalDate.now().plusDays(15));
// loan.setLoanBooks(new HashSet<>());

// when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
// when(loanRepository.save(loan)).thenReturn(loan);

// LoanDTO result = loanService.extendLoan(1L);

// assertThat(loan.getExtended()).isEqualTo((byte) 1);
// assertThat(loan.getEnd()).isEqualTo(LocalDate.now().plusDays(22));
// assertThat(result).isNotNull();
// verify(loanRepository).save(loan);
// }

// @Test
// void extendLoan_withAcceptedStatus_success() {
// school.setExtendLimit(3);
// school.setExtendPeriod(7);
// location.setSchool(school);
// loan.setStatus(LoanStatus.ACCEPTED);
// loan.setExtended((byte) 0);
// loan.setEnd(LocalDate.now().plusDays(15));
// loan.setLoanBooks(new HashSet<>());

// when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
// when(loanRepository.save(loan)).thenReturn(loan);

// LoanDTO result = loanService.extendLoan(1L);

// assertThat(result).isNotNull();
// verify(loanRepository).save(loan);
// }

// @Test
// void extendLoan_notFound_throwsEntityNotFoundException() {
// when(loanRepository.findById(99L)).thenReturn(Optional.empty());

// assertThatThrownBy(() -> loanService.extendLoan(99L))
// .isInstanceOf(EntityNotFoundException.class)
// .hasMessageContaining("Lening niet gevonden met id: 99");
// }

// @Test
// void extendLoan_wrongStatus_throwsIllegalArgument() {
// loan.setStatus(LoanStatus.REQUESTED);

// when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

// assertThatThrownBy(() -> loanService.extendLoan(1L))
// .isInstanceOf(IllegalArgumentException.class)
// .hasMessageContaining("Lening kan niet verlengd worden met status");
// }

// @Test
// void extendLoan_returnedStatus_throwsIllegalArgument() {
// loan.setStatus(LoanStatus.RETURNED);

// when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

// assertThatThrownBy(() -> loanService.extendLoan(1L))
// .isInstanceOf(IllegalArgumentException.class)
// .hasMessageContaining("Lening kan niet verlengd worden met status");
// }

// @Test
// void extendLoan_maxExtensionsReached_throwsIllegalArgument() {
// school.setExtendLimit(2);
// school.setExtendPeriod(7);
// location.setSchool(school);
// loan.setStatus(LoanStatus.RECEIVED);
// loan.setExtended((byte) 2);

// when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

// assertThatThrownBy(() -> loanService.extendLoan(1L))
// .isInstanceOf(IllegalArgumentException.class)
// .hasMessageContaining("Maximum aantal verlengingen bereikt");
// }

// @Test
// void extendLoan_incrementsExtendedCount() {
// school.setExtendLimit(3);
// school.setExtendPeriod(7);
// location.setSchool(school);
// loan.setStatus(LoanStatus.RECEIVED);
// loan.setExtended((byte) 1);
// loan.setEnd(LocalDate.now().plusDays(15));
// loan.setLoanBooks(new HashSet<>());

// when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
// when(loanRepository.save(loan)).thenReturn(loan);

// loanService.extendLoan(1L);

// assertThat(loan.getExtended()).isEqualTo((byte) 2);
// }

// // ── helper ────────────────────────────────────────────────────

// private LoanDTO validLoanDTO() {
// LoanBookDTO loanBookDTO = new LoanBookDTO();
// loanBookDTO.setBookId(1L);
// loanBookDTO.setRequestedAmount(1);

// LoanDTO dto = new LoanDTO();
// dto.setUserId(1L);
// dto.setLocationId(1L);
// dto.setStart(LocalDate.now().plusDays(1));
// dto.setEnd(LocalDate.now().plusDays(15));
// dto.setBooks(new LoanBookDTO[] { loanBookDTO });
// return dto;
// }
// }