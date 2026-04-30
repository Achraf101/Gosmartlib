package be.ap.backend.service;

import be.ap.backend.dto.LoanBookDTO;
import be.ap.backend.dto.LoanDTO;
import be.ap.backend.entity.*;
import be.ap.backend.repository.CampusBookRepository;
import be.ap.backend.repository.LoanBookRepository;
import be.ap.backend.repository.LoanRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
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
    private CampusBookRepository campusBookRepository;

    @Mock
    private CampusBookService campusBookService;

    @InjectMocks
    private LoanService loanService;

    private User user;
    private Campus campus;
    private Book book;
    private CampusBook campusBook;
    private Loan loan;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        campus = new Campus();
        campus.setId(1L);
        campus.setBorrowLimit(5);
        campus.setBorrowPeriod(14);

        book = new Book();
        book.setId(1L);
        book.setTitle("De brief voor de koning");

        campusBook = new CampusBook();
        campusBook.setId(1L);
        campusBook.setCampus(campus);
        campusBook.setBook(book);
        campusBook.setAmount(3);
        campusBook.setCurrentAmount(3);

        loan = new Loan();
        loan.setId(1L);
        loan.setUser(user);
        loan.setCampus(campus);
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
        when(entityManager.find(Campus.class, 1L)).thenReturn(campus);
        when(entityManager.find(Book.class, 1L)).thenReturn(book);
        when(campusBookRepository.findByCampusIdAndBookId(1L, 1L)).thenReturn(Optional.of(campusBook));
        when(loanRepository.save(any())).thenReturn(loan);
        when(loanBookRepository.saveAll(any())).thenReturn(List.of());

        LoanDTO result = loanService.createLoan(dto);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        verify(loanRepository).save(any());
        verify(loanBookRepository).saveAll(any());
        verify(campusBookService).updateCurrentAmount(campusBook, 1);
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
    void createLoan_campusNotFound_throwsEntityNotFoundException() {
        LoanDTO dto = validLoanDTO();

        when(entityManager.find(User.class, 1L)).thenReturn(user);
        when(entityManager.find(Campus.class, 1L)).thenReturn(null);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Campus niet gevonden");
    }

    @Test
    void createLoan_exceedsBorrowLimit_throwsIllegalArgument() {
        campus.setBorrowLimit(1);
        LoanDTO dto = validLoanDTO();

        LoanBookDTO extra = new LoanBookDTO();
        extra.setBookId(2L);
        extra.setRequestedAmount(1);
        dto.setBooks(new LoanBookDTO[] { dto.getBooks()[0], extra });

        when(entityManager.find(User.class, 1L)).thenReturn(user);
        when(entityManager.find(Campus.class, 1L)).thenReturn(campus);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("uitleen limiet");
    }

    @Test
    void createLoan_wrongEndDate_throwsIllegalArgument() {
        LoanDTO dto = validLoanDTO();
        dto.setEnd(dto.getStart().plusDays(99));

        when(entityManager.find(User.class, 1L)).thenReturn(user);
        when(entityManager.find(Campus.class, 1L)).thenReturn(campus);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("14 dagen na begindatum");
    }

    @Test
    void createLoan_notEnoughCurrentAmount_throwsIllegalArgument() {
        campusBook.setCurrentAmount(0);
        LoanDTO dto = validLoanDTO();

        when(entityManager.find(User.class, 1L)).thenReturn(user);
        when(entityManager.find(Campus.class, 1L)).thenReturn(campus);
        when(entityManager.find(Book.class, 1L)).thenReturn(book);
        when(campusBookRepository.findByCampusIdAndBookId(1L, 1L)).thenReturn(Optional.of(campusBook));

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("niet meer beschikbaar");
    }

    @Test
    void createLoan_bookNotFoundInEntityManager_throwsEntityNotFoundException() {
        LoanDTO dto = validLoanDTO();

        when(entityManager.find(User.class, 1L)).thenReturn(user);
        when(entityManager.find(Campus.class, 1L)).thenReturn(campus);
        when(entityManager.find(Book.class, 1L)).thenReturn(null);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Boek niet gevonden met id");
    }

    @Test
    void createLoan_bookNotFoundOnCampus_throwsEntityNotFoundException() {
        LoanDTO dto = validLoanDTO();

        when(entityManager.find(User.class, 1L)).thenReturn(user);
        when(entityManager.find(Campus.class, 1L)).thenReturn(campus);
        when(entityManager.find(Book.class, 1L)).thenReturn(book);
        when(campusBookRepository.findByCampusIdAndBookId(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Boek niet gevonden in campus");
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
        when(campusBookRepository.findByCampusIdAndBookId(1L, 1L)).thenReturn(Optional.of(campusBook));
        when(loanRepository.save(loan)).thenReturn(loan);

        loanService.updateStatus(1L, LoanStatus.DECLINED);

        verify(campusBookService).updateCurrentAmount(campusBook, -2);
    }

    @Test
    void updateStatus_declined_campusBookNotFound_throwsEntityNotFoundException() {
        LoanBook loanBook = new LoanBook();
        loanBook.setBook(book);
        loanBook.setRequestedAmount(2);
        loan.setLoanBooks(Set.of(loanBook));

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(campusBookRepository.findByCampusIdAndBookId(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.updateStatus(1L, LoanStatus.DECLINED))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book not found on campus");
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
        dto.setCampusId(1L);
        dto.setStart(LocalDate.now().plusDays(1));
        dto.setEnd(LocalDate.now().plusDays(15));
        dto.setBooks(new LoanBookDTO[] { loanBookDTO });
        return dto;
    }
}