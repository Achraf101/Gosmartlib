package be.ap.backend.service;

import be.ap.backend.dto.LoanBookDTO;
import be.ap.backend.dto.LoanDTO;
import be.ap.backend.dto.TopBookDTO;
import be.ap.backend.entity.*;
import be.ap.backend.repository.LocationBookRepository;
import be.ap.backend.repository.LoanBookRepository;
import be.ap.backend.repository.LoanRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private LoanBookRepository loanBookRepository;

    @Mock
    private LocationBookRepository locationBookRepository;

    @Mock
    private LocationBookService locationBookService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private LoanService loanService;

    private User user;
    private Location location;
    private Book book;
    private LocationBook locationBook;
    private Loan loan;
    private School school;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        location = new Location();
        location.setId(1L);
        school = new School();
        school.setBorrowLimit(5);
        school.setBorrowPeriod(14);

        book = new Book();
        book.setId(1L);
        book.setTitle("De brief voor de koning");

        locationBook = new LocationBook();
        locationBook.setId(1L);
        locationBook.setLocation(location);
        locationBook.setBook(book);
        locationBook.setAmount(3);
        locationBook.setCurrentAmount(3);

        loan = new Loan();
        loan.setId(1L);
        loan.setUser(user);
        loan.setLocation(location);
        loan.setStart(LocalDate.now().plusDays(1));
        loan.setEnd(LocalDate.now().plusDays(15));
        loan.setStatus(LoanStatus.REQUESTED);
        loan.setLoanBooks(new HashSet<>());
    }

    // ── createLoan ────────────────────────────────────────────────

    @Test
    void createLoan_success() {
        LoanDTO dto = validLoanDTO();

        when(entityManager.find(User.class, 1L)).thenReturn(user);
        when(entityManager.find(Location.class, 1L)).thenReturn(location);
        when(session.getAttribute("school")).thenReturn("1");
        when(entityManager.find(School.class, 1L)).thenReturn(school);
        when(entityManager.find(Book.class, 1L)).thenReturn(book);
        when(locationBookRepository.findByLocationIdAndBookId(1L, 1L)).thenReturn(Optional.of(locationBook));
        when(loanRepository.save(any())).thenReturn(loan);
        when(loanBookRepository.save(any())).thenReturn(new LoanBook());

        List<LoanDTO> result = loanService.createLoan(dto);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        verify(loanRepository).save(any());
        verify(loanBookRepository).saveAll(any());
        verify(locationBookService).updateCurrentAmount(locationBook, 1);
    }

    @Test
    void createLoan_missingUserId_throwsIllegalArgument() {
        LoanDTO dto = validLoanDTO();
        dto.setUserId(null);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId is verplicht");
    }

    @Test
    void createLoan_missingStart_throwsIllegalArgument() {
        LoanDTO dto = validLoanDTO();
        dto.setStart(null);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Begin- en einddatum zijn verplicht");
    }

    @Test
    void createLoan_missingEnd_throwsIllegalArgument() {
        LoanDTO dto = validLoanDTO();
        dto.setEnd(null);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Begin- en einddatum zijn verplicht");
    }

    @Test
    void createLoan_startInPast_throwsIllegalArgument() {
        LoanDTO dto = validLoanDTO();
        dto.setStart(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Begindatum kan niet in het verleden liggen");
    }

    @Test
    void createLoan_startAfterEnd_throwsIllegalArgument() {
        LoanDTO dto = validLoanDTO();
        dto.setStart(LocalDate.now().plusDays(5));
        dto.setEnd(LocalDate.now().plusDays(3));

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Begindatum moet voor einddatum zijn");
    }

    @Test
    void createLoan_noBooks_throwsIllegalArgument() {
        LoanDTO dto = validLoanDTO();
        dto.setBooks(new LoanBookDTO[0]);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minstens 1 boek is verplicht");
    }

    @Test
    void createLoan_bookMissingBookId_throwsIllegalArgument() {
        LoanDTO dto = validLoanDTO();
        dto.getBooks()[0].setBookId(null);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bookId is verplicht");
    }

    @Test
    void createLoan_bookZeroAmount_throwsIllegalArgument() {
        LoanDTO dto = validLoanDTO();
        dto.getBooks()[0].setRequestedAmount(0);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("aangevraagde hoeveelheid moet groter zijn dan 0");
    }

    @Test
    void createLoan_userNotFound_throwsEntityNotFoundException() {
        LoanDTO dto = validLoanDTO();

        when(entityManager.find(User.class, 1L)).thenReturn(null);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Gebruiker niet gevonden");
    }

    @Test
    void createLoan_locationNotFound_throwsEntityNotFoundException() {
        LoanDTO dto = validLoanDTO();

        when(entityManager.find(User.class, 1L)).thenReturn(user);
        when(entityManager.find(Location.class, 1L)).thenReturn(null);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Locatie niet gevonden");
    }

    @Test
    void createLoan_exceedsBorrowLimit_throwsIllegalArgument() {
        school.setBorrowLimit(1);
        LoanDTO dto = validLoanDTO();

        LoanBookDTO extra = new LoanBookDTO();
        extra.setBookId(2L);
        extra.setRequestedAmount(1);
        dto.setBooks(new LoanBookDTO[] { dto.getBooks()[0], extra });

        when(entityManager.find(User.class, 1L)).thenReturn(user);
        when(session.getAttribute("school")).thenReturn("1");
        when(entityManager.find(School.class, 1L)).thenReturn(school);

        when(entityManager.find(Location.class, 1L)).thenReturn(location);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("uitleen limiet");
    }

    @Test
    void createLoan_wrongEndDate_throwsIllegalArgument() {
        LoanDTO dto = validLoanDTO();
        dto.setEnd(dto.getStart().plusDays(99));

        when(entityManager.find(User.class, 1L)).thenReturn(user);
        when(entityManager.find(Location.class, 1L)).thenReturn(location);
        when(session.getAttribute("school")).thenReturn("1");
        when(entityManager.find(School.class, 1L)).thenReturn(school);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("14 dagen na begindatum");
    }

    @Test
    void createLoan_notEnoughCurrentAmount_throwsIllegalArgument() {
        locationBook.setCurrentAmount(0);
        LoanDTO dto = validLoanDTO();

        when(entityManager.find(User.class, 1L)).thenReturn(user);
        when(entityManager.find(Location.class, 1L)).thenReturn(location);
        when(session.getAttribute("school")).thenReturn("1");
        when(entityManager.find(School.class, 1L)).thenReturn(school);

        when(entityManager.find(Book.class, 1L)).thenReturn(book);
        when(locationBookRepository.findByLocationIdAndBookId(1L, 1L)).thenReturn(Optional.of(locationBook));

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("niet meer beschikbaar");
    }

    @Test
    void createLoan_bookNotFoundInEntityManager_throwsEntityNotFoundException() {
        LoanDTO dto = validLoanDTO();

        when(entityManager.find(User.class, 1L)).thenReturn(user);
        when(entityManager.find(Location.class, 1L)).thenReturn(location);
        when(session.getAttribute("school")).thenReturn("1");
        when(entityManager.find(School.class, 1L)).thenReturn(school);
        when(entityManager.find(Book.class, 1L)).thenReturn(null);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Boek niet gevonden met id");
    }

    @Test
    void createLoan_bookNotFoundOnLocation_throwsEntityNotFoundException() {
        LoanDTO dto = validLoanDTO();

        when(entityManager.find(User.class, 1L)).thenReturn(user);
        when(entityManager.find(Location.class, 1L)).thenReturn(location);
        when(session.getAttribute("school")).thenReturn("1");
        when(entityManager.find(School.class, 1L)).thenReturn(school);
        when(entityManager.find(Book.class, 1L)).thenReturn(book);
        when(locationBookRepository.findByLocationIdAndBookId(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Boek niet gevonden in locatie");
    }

    // ── getRequested ──────────────────────────────────────────────

    @Test
    void getRequested_returnsListOfDTOs() {
        loan.setLoanBooks(new HashSet<>());
        when(loanRepository.findByStatusWithBooks(LoanStatus.REQUESTED)).thenReturn(List.of(loan));

        List<LoanDTO> result = loanService.getRequested();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void getRequested_empty_returnsEmptyList() {
        when(loanRepository.findByStatusWithBooks(LoanStatus.REQUESTED)).thenReturn(List.of());

        List<LoanDTO> result = loanService.getRequested();

        assertThat(result).isEmpty();
    }

    // ── updateNote ────────────────────────────────────────────────

    @Test
    void updateNote_success() {
        loan.setLoanBooks(new HashSet<>());
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(loan)).thenReturn(loan);

        loanService.updateNote(1L, "Nieuwe opmerking");

        assertThat(loan.getNote()).isEqualTo("Nieuwe opmerking");
        verify(loanRepository).save(loan);
    }

    @Test
    void updateNote_notFound_throwsEntityNotFoundException() {
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.updateNote(99L, "test"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Loan niet gevonden");
    }

    @Test
    void updateNote_tooLong_throwsIllegalArgument() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        String longNote = "a".repeat(256);

        assertThatThrownBy(() -> loanService.updateNote(1L, longNote))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("te lang");
    }

    @Test
    void updateNote_exactly255Chars_succeeds() {
        loan.setLoanBooks(new HashSet<>());
        String boundaryNote = "a".repeat(255);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(loan)).thenReturn(loan);

        assertThatNoException().isThrownBy(() -> loanService.updateNote(1L, boundaryNote));
        assertThat(loan.getNote()).isEqualTo(boundaryNote);
    }

    // ── updateStatus ──────────────────────────────────────────────

    @Test
    void updateStatus_success() {
        loan.setLoanBooks(new HashSet<>());
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(loan)).thenReturn(loan);

        loanService.updateStatus(1L, LoanStatus.ACCEPTED);

        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACCEPTED);
        verify(loanRepository).save(loan);
    }

    @Test
    void updateStatus_notFound_throwsEntityNotFoundException() {
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.updateStatus(99L, LoanStatus.ACCEPTED))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Loan niet gevonden");
    }

    @Test
    void updateStatus_declined_restoresCurrentAmount() {
        LoanBook loanBook = new LoanBook();
        loanBook.setBook(book);
        loanBook.setRequestedAmount(2);
        loan.setLoanBooks(Set.of(loanBook));

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(locationBookRepository.findByLocationIdAndBookId(1L, 1L)).thenReturn(Optional.of(locationBook));
        when(loanRepository.save(loan)).thenReturn(loan);

        loanService.updateStatus(1L, LoanStatus.DECLINED);

        verify(locationBookService).updateCurrentAmount(locationBook, -2);
    }

    @Test
    void updateStatus_declined_locationBookNotFound_throwsEntityNotFoundException() {
        LoanBook loanBook = new LoanBook();
        loanBook.setBook(book);
        loanBook.setRequestedAmount(2);
        loan.setLoanBooks(Set.of(loanBook));

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(locationBookRepository.findByLocationIdAndBookId(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.updateStatus(1L, LoanStatus.DECLINED))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book niet gevonden in locatie");
    }

    // ── getByUserId ───────────────────────────────────────────────

    @Test
    void getByUserId_returnsLoansForUser() {
        loan.setLoanBooks(new HashSet<>());
        when(loanRepository.findByUserId(1L)).thenReturn(List.of(loan));

        List<LoanDTO> result = loanService.getByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void getByUserId_noLoans_returnsEmptyList() {
        when(loanRepository.findByUserId(99L)).thenReturn(List.of());

        List<LoanDTO> result = loanService.getByUserId(99L);

        assertThat(result).isEmpty();
    }

    // ── deleteLoan ────────────────────────────────────────────────

    @Test
    void deleteLoan_success() {
        when(loanRepository.existsById(1L)).thenReturn(true);

        assertThatNoException().isThrownBy(() -> loanService.deleteLoan(1L));

        verify(loanRepository).deleteById(1L);
    }

    @Test
    void deleteLoan_notFound_throwsRuntimeException() {
        when(loanRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> loanService.deleteLoan(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Uitlening niet gevonden");
    }

    // ── helper ────────────────────────────────────────────────────

    private LoanDTO validLoanDTO() {
        LoanBookDTO loanBookDTO = new LoanBookDTO();
        loanBookDTO.setBookId(1L);
        loanBookDTO.setRequestedAmount(1);

        LoanDTO dto = new LoanDTO();
        dto.setUserId(1L);
        dto.setLocationId(1L);
        dto.setStart(LocalDate.now().plusDays(1));
        dto.setEnd(LocalDate.now().plusDays(15));
        dto.setBooks(new LoanBookDTO[] { loanBookDTO });
        return dto;
    }

    @Test
    void getOverdueLoans_returnsListOfDTOs() {
        loan.setStatus(LoanStatus.RECEIVED);
        loan.setEnd(LocalDate.now().minusDays(1));

        when(loanRepository.findOverdueLoans(anyList(), any(LocalDate.class), eq(1L)))
                .thenReturn(List.of(loan));

        List<LoanDTO> result = loanService.getOverdueLoans(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void getOverdueLoans_empty_returnsEmptyList() {
        when(loanRepository.findOverdueLoans(anyList(), any(LocalDate.class), eq(1L)))
                .thenReturn(List.of());

        List<LoanDTO> result = loanService.getOverdueLoans(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getOverdueLoansLength_returnsCount() {
        when(loanRepository.countOverdueLoans(anyList(), any(LocalDate.class), eq(1L)))
                .thenReturn(3);

        int result = loanService.getOverdueLoansLength(1L);

        assertThat(result).isEqualTo(3);
    }

    @Test
    void getOverdueLoansLength_noOverdue_returnsZero() {
        when(loanRepository.countOverdueLoans(anyList(), any(LocalDate.class), eq(1L)))
                .thenReturn(0);

        int result = loanService.getOverdueLoansLength(1L);

        assertThat(result).isEqualTo(0);
    }

    @Test
    void getDueSoonLoans_returnsListOfDTOs() {
        loan.setStatus(LoanStatus.RECEIVED);
        loan.setEnd(LocalDate.now().plusDays(3));

        when(loanRepository.findDueSoonLoans(anyList(), any(LocalDate.class), any(LocalDate.class), eq(1L)))
                .thenReturn(List.of(loan));

        List<LoanDTO> result = loanService.getDueSoonLoans(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void getDueSoonLoans_empty_returnsEmptyList() {
        when(loanRepository.findDueSoonLoans(anyList(), any(LocalDate.class), any(LocalDate.class), eq(1L)))
                .thenReturn(List.of());

        List<LoanDTO> result = loanService.getDueSoonLoans(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getDueSoonLoansLength_returnsCount() {
        when(loanRepository.countDueSoonLoans(anyList(), any(LocalDate.class), any(LocalDate.class), eq(1L)))
                .thenReturn(7);

        int result = loanService.getDueSoonLoansLength(1L);

        assertThat(result).isEqualTo(7);
    }

    @Test
    void getDueSoonLoansLength_noSoonDue_returnsZero() {
        when(loanRepository.countDueSoonLoans(anyList(), any(LocalDate.class), any(LocalDate.class), eq(1L)))
                .thenReturn(0);

        int result = loanService.getDueSoonLoansLength(1L);

        assertThat(result).isEqualTo(0);
    }

    @Test
    void getTopBooksThisMonth_returnsTopBooks() {
        LoanBook loanBook = new LoanBook();
        loanBook.setBook(book);
        loan.setLoanBooks(Set.of(loanBook));

        when(loanRepository.findByDateRangeWithBooks(any(), any(), anyList(), eq(1L)))
                .thenReturn(List.of(loan));

        List<TopBookDTO> result = loanService.getTopBooksThisMonth(1L);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).title()).isEqualTo("De brief voor de koning");
        assertThat(result.get(0).count()).isEqualTo(1);
    }

    @Test
    void getTopBooksThisMonth_limitsToFive() {
        LoanBook loanBook = new LoanBook();
        loanBook.setBook(book);

        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Het kerstvarken");
        LoanBook lb2 = new LoanBook();
        lb2.setBook(book2);

        Book book3 = new Book();
        book3.setId(3L);
        book3.setTitle("Onze versplinterde zielen");
        LoanBook lb3 = new LoanBook();
        lb3.setBook(book3);

        Book book4 = new Book();
        book4.setId(4L);
        book4.setTitle("Kruistocht");
        LoanBook lb4 = new LoanBook();
        lb4.setBook(book4);

        Book book5 = new Book();
        book5.setId(5L);
        book5.setTitle("Geef me de ruimte");
        LoanBook lb5 = new LoanBook();
        lb5.setBook(book5);

        Book book6 = new Book();
        book6.setId(6L);
        book6.setTitle("Extra boek");
        LoanBook lb6 = new LoanBook();
        lb6.setBook(book6);

        Loan l1 = new Loan();
        l1.setLoanBooks(Set.of(loanBook));
        Loan l2 = new Loan();
        l2.setLoanBooks(Set.of(lb2));
        Loan l3 = new Loan();
        l3.setLoanBooks(Set.of(lb3));
        Loan l4 = new Loan();
        l4.setLoanBooks(Set.of(lb4));
        Loan l5 = new Loan();
        l5.setLoanBooks(Set.of(lb5));
        Loan l6 = new Loan();
        l6.setLoanBooks(Set.of(lb6));

        when(loanRepository.findByDateRangeWithBooks(any(), any(), anyList(), eq(1L)))
                .thenReturn(List.of(l1, l2, l3, l4, l5, l6));

        List<TopBookDTO> result = loanService.getTopBooksThisMonth(1L);

        assertThat(result.size()).isLessThanOrEqualTo(5);
    }

    @Test
    void getTopBooksThisMonth_empty_returnsEmptyList() {
        when(loanRepository.findByDateRangeWithBooks(any(), any(), anyList(), eq(1L)))
                .thenReturn(List.of());

        List<TopBookDTO> result = loanService.getTopBooksThisMonth(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getTopGenresThisMonth_returnsTopGenres() {
        Object[] row = { "Fantasy", 5L };
        List<Object[]> rows = new ArrayList<>();
        rows.add(row);
        when(loanRepository.findTopGenres(anyList(), any(), any(), eq(1L)))
                .thenReturn(rows);

        List<TopBookDTO> result = loanService.getTopGenresThisMonth(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Fantasy");
        assertThat(result.get(0).count()).isEqualTo(5);
    }

    @Test
    void getTopGenresThisMonth_empty_returnsEmptyList() {
        when(loanRepository.findTopGenres(anyList(), any(), any(), eq(1L)))
                .thenReturn(List.of());

        List<TopBookDTO> result = loanService.getTopGenresThisMonth(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getTopGenresThisMonth_limitsToFive() {
        List<Object[]> rows = List.of(
                new Object[] { "Fantasy", 10L },
                new Object[] { "Horror", 9L },
                new Object[] { "Humor", 8L },
                new Object[] { "Avontuur", 7L },
                new Object[] { "Poëzie", 6L },
                new Object[] { "Romantiek", 5L });

        when(loanRepository.findTopGenres(anyList(), any(), any(), eq(1L)))
                .thenReturn(rows);

        List<TopBookDTO> result = loanService.getTopGenresThisMonth(1L);

        assertThat(result.size()).isLessThanOrEqualTo(5);
    }

    @Test
    void createLoan_multipleBooks_sameGroupId() {
        LoanBookDTO book1 = new LoanBookDTO();
        book1.setBookId(1L);
        book1.setRequestedAmount(1);

        Book book2entity = new Book();
        book2entity.setId(2L);
        book2entity.setTitle("Tweede boek");

        LocationBook locationBook2 = new LocationBook();
        locationBook2.setId(2L);
        locationBook2.setLocation(location);
        locationBook2.setBook(book2entity);
        locationBook2.setAmount(3);
        locationBook2.setCurrentAmount(3);

        LoanBookDTO book2 = new LoanBookDTO();
        book2.setBookId(2L);
        book2.setRequestedAmount(1);

        LoanDTO dto = validLoanDTO();
        dto.setBooks(new LoanBookDTO[] { book1, book2 });

        when(entityManager.find(User.class, 1L)).thenReturn(user);
        when(entityManager.find(Location.class, 1L)).thenReturn(location);
        when(entityManager.find(Book.class, 1L)).thenReturn(book);
        when(entityManager.find(Book.class, 2L)).thenReturn(book2entity);
        when(locationBookRepository.findByLocationIdAndBookId(1L, 1L)).thenReturn(Optional.of(locationBook));
        when(locationBookRepository.findByLocationIdAndBookId(1L, 2L)).thenReturn(Optional.of(locationBook2));
        when(loanRepository.save(any())).thenReturn(loan);
        when(loanBookRepository.save(any())).thenReturn(new LoanBook());

        List<LoanDTO> result = loanService.createLoan(dto);

        assertThat(result).hasSize(2);
    }

    @Test
    void createLoan_singleBook_groupIdIsNull() {
        LoanDTO dto = validLoanDTO();

        when(entityManager.find(User.class, 1L)).thenReturn(user);
        when(entityManager.find(Location.class, 1L)).thenReturn(location);
        when(entityManager.find(Book.class, 1L)).thenReturn(book);
        when(locationBookRepository.findByLocationIdAndBookId(1L, 1L)).thenReturn(Optional.of(locationBook));
        when(loanRepository.save(any())).thenReturn(loan);
        when(loanBookRepository.save(any())).thenReturn(new LoanBook());

        List<LoanDTO> result = loanService.createLoan(dto);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGroupId()).isNull();
    }
}