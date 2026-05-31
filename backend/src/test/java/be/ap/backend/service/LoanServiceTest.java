package be.ap.backend.service;

import be.ap.backend.dto.LoanBookDTO;
import be.ap.backend.dto.LoanDTO;
import be.ap.backend.dto.TopBookDTO;
import be.ap.backend.entity.*;
import be.ap.backend.enums.LoanStatus;
import be.ap.backend.queue.NotificationTask;
import be.ap.backend.queue.TaskQueueService;
import be.ap.backend.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

    // ── Mocks ─────────────────────────────────────────────────────

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
    private BookCopyRepository bookCopyRepository;
    @Mock
    private SmartschoolLookupService lookupService;
    @Mock
    private TaskQueueService taskQueueService;

    /**
     * A direct executor so CompletableFuture.supplyAsync runs synchronously in
     * tests,
     * avoiding thread-pool leaks and race conditions.
     */
    private final Executor syncExecutor = Runnable::run;

    private LoanService loanService;

    // ── Shared fixtures ───────────────────────────────────────────

    private School school;
    private User student;
    private Location location;
    private Book book;
    private LocationBook locationBook;
    private Loan loan;
    private LoanBook loanBook;

    @BeforeEach
    void setUp() {
        // Wire service manually so we can inject the sync executor
        loanService = new LoanService(
                loanRepository, entityManager, loanBookRepository,
                locationBookRepository, locationBookService, lookupService,
                syncExecutor, taskQueueService, bookCopyRepository);

        school = new School();
        school.setId(1L);
        school.setBorrowLimit(5);
        school.setBorrowPeriod(14);
        school.setExtendPeriod(7);
        school.setExtendLimit(2);

        student = new User();
        student.setId(10L);
        student.setUsername("anna");
        student.setOneRosterId("sis-001");
        student.setRoles(new HashSet<>(Set.of(UserRole.STUDENT)));
        student.setSchool(school);

        location = new Location();
        location.setId(20L);
        location.setSchool(school);

        book = new Book();
        book.setId(30L);
        book.setTitle("De Tijger");
        book.setCover("cover.jpg");

        locationBook = new LocationBook();
        locationBook.setId(40L);
        locationBook.setBook(book);
        locationBook.setLocation(location);
        locationBook.setCurrentAmount(3);

        loanBook = new LoanBook();
        loanBook.setId(50L);
        loanBook.setBook(book);
        loanBook.setRequestedAmount(1);
        loanBook.setReceivedAmount(0);
        loanBook.setReturnedAmount(0);
        loanBook.setScannedCopyIds(new HashSet<>());
        loanBook.setReturnedCopyIds(new HashSet<>());

        loan = new Loan();
        loan.setId(100L);
        loan.setUser(student);
        loan.setLocation(location);
        loan.setStart(LocalDate.now());
        loan.setEnd(LocalDate.now().plusDays(14));
        loan.setStatus(LoanStatus.REQUESTED);
        loan.setExtended((byte) 0);
        loan.setLoanBooks(new HashSet<>(Set.of(loanBook)));
        loanBook.setLoan(loan);
    }

    // ── Helpers ───────────────────────────────────────────────────

    /** Stub lookupService for the shared student so buildDTO succeeds. */
    private void stubLookup() {
        when(lookupService.getUser(school.getId(), student.getOneRosterId(), student.getRoles()))
                .thenReturn(Map.of("givenName", "Anna", "familyName", "Aerts"));
    }

    /** Build a minimal valid LoanDTO for createLoan. */
    private LoanDTO validCreateDTO() {
        LoanDTO dto = new LoanDTO();
        dto.setUserId(student.getId());
        dto.setLocationId(location.getId());
        dto.setStart(LocalDate.now().plusDays(1));
        dto.setEnd(LocalDate.now().plusDays(1).plusDays(school.getBorrowPeriod()));
        dto.setStatus(LoanStatus.REQUESTED);
        dto.setExtended((byte) 0);

        LoanBookDTO lbDTO = new LoanBookDTO();
        lbDTO.setBookId(book.getId());
        lbDTO.setRequestedAmount(1);
        dto.setBooks(new LoanBookDTO[] { lbDTO });
        return dto;
    }

    // ── createLoan — validation ───────────────────────────────────

    @Test
    void createLoan_nullUserId_throwsIllegalArgument() {
        LoanDTO dto = validCreateDTO();
        dto.setUserId(null);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId is verplicht");
    }

    @Test
    void createLoan_nullStartDate_throwsIllegalArgument() {
        LoanDTO dto = validCreateDTO();
        dto.setStart(null);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Begin- en einddatum");
    }

    @Test
    void createLoan_nullEndDate_throwsIllegalArgument() {
        LoanDTO dto = validCreateDTO();
        dto.setEnd(null);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Begin- en einddatum");
    }

    @Test
    void createLoan_startInPast_throwsIllegalArgument() {
        LoanDTO dto = validCreateDTO();
        dto.setStart(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("verleden");
    }

    @Test
    void createLoan_endBeforeStart_throwsIllegalArgument() {
        LoanDTO dto = validCreateDTO();
        dto.setStart(LocalDate.now().plusDays(5));
        dto.setEnd(LocalDate.now().plusDays(3));

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Begindatum moet voor einddatum zijn");
    }

    @Test
    void createLoan_startEqualsEnd_throwsIllegalArgument() {
        LoanDTO dto = validCreateDTO();
        LocalDate same = LocalDate.now().plusDays(2);
        dto.setStart(same);
        dto.setEnd(same);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Begindatum moet voor einddatum zijn");
    }

    @Test
    void createLoan_nullBooks_throwsIllegalArgument() {
        LoanDTO dto = validCreateDTO();
        dto.setBooks(null);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minstens 1 boek");
    }

    @Test
    void createLoan_emptyBooks_throwsIllegalArgument() {
        LoanDTO dto = validCreateDTO();
        dto.setBooks(new LoanBookDTO[0]);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minstens 1 boek");
    }

    @Test
    void createLoan_userNotFound_throwsEntityNotFound() {
        LoanDTO dto = validCreateDTO();
        when(entityManager.find(User.class, student.getId())).thenReturn(null);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Gebruiker niet gevonden");
    }

    @Test
    void createLoan_locationNotFound_throwsEntityNotFound() {
        LoanDTO dto = validCreateDTO();
        when(entityManager.find(User.class, student.getId())).thenReturn(student);
        when(entityManager.find(Location.class, location.getId())).thenReturn(null);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Locatie niet gevonden");
    }

    @Test
    void createLoan_schoolMismatch_throwsIllegalArgument() {
        School otherSchool = new School();
        otherSchool.setId(99L);
        student.setSchool(otherSchool);

        LoanDTO dto = validCreateDTO();
        when(entityManager.find(User.class, student.getId())).thenReturn(student);
        when(entityManager.find(Location.class, location.getId())).thenReturn(location);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("school");
    }

    @Test
    void createLoan_exceedsBorrowLimit_throwsIllegalArgument() {
        school.setBorrowLimit(1);

        LoanBookDTO lb1 = new LoanBookDTO();
        lb1.setBookId(1L);
        lb1.setRequestedAmount(1);
        LoanBookDTO lb2 = new LoanBookDTO();
        lb2.setBookId(2L);
        lb2.setRequestedAmount(1);

        LoanDTO dto = validCreateDTO();
        dto.setBooks(new LoanBookDTO[] { lb1, lb2 });

        when(entityManager.find(User.class, student.getId())).thenReturn(student);
        when(entityManager.find(Location.class, location.getId())).thenReturn(location);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ontleenlimiet");
    }

    @Test
    void createLoan_wrongEndDate_throwsIllegalArgument() {
        LoanDTO dto = validCreateDTO();
        dto.setEnd(dto.getStart().plusDays(999));

        when(entityManager.find(User.class, student.getId())).thenReturn(student);
        when(entityManager.find(Location.class, location.getId())).thenReturn(location);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dagen na begindatum");
    }

    @Test
    void createLoan_bookNotFound_throwsEntityNotFound() {
        LoanDTO dto = validCreateDTO();
        when(entityManager.find(User.class, student.getId())).thenReturn(student);
        when(entityManager.find(Location.class, location.getId())).thenReturn(location);
        when(entityManager.find(Book.class, book.getId())).thenReturn(null);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Boek niet gevonden");
    }

    @Test
    void createLoan_bookNotAtLocation_throwsEntityNotFound() {
        LoanDTO dto = validCreateDTO();
        when(entityManager.find(User.class, student.getId())).thenReturn(student);
        when(entityManager.find(Location.class, location.getId())).thenReturn(location);
        when(entityManager.find(Book.class, book.getId())).thenReturn(book);
        when(locationBookRepository.findByLocationIdAndBookId(location.getId(), book.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Boek niet gevonden in locatie");
    }

    @Test
    void createLoan_requestedAmountExceedsStock_throwsIllegalArgument() {
        locationBook.setCurrentAmount(0);
        LoanDTO dto = validCreateDTO();
        when(entityManager.find(User.class, student.getId())).thenReturn(student);
        when(entityManager.find(Location.class, location.getId())).thenReturn(location);
        when(entityManager.find(Book.class, book.getId())).thenReturn(book);
        when(locationBookRepository.findByLocationIdAndBookId(location.getId(), book.getId()))
                .thenReturn(Optional.of(locationBook));

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("niet meer beschikbaar");
    }

    @Test
    void createLoan_nullBookId_throwsIllegalArgument() {
        LoanDTO dto = validCreateDTO();
        dto.getBooks()[0].setBookId(null);

        when(entityManager.find(User.class, student.getId())).thenReturn(student);
        when(entityManager.find(Location.class, location.getId())).thenReturn(location);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bookId is verplicht");
    }

    @Test
    void createLoan_zeroRequestedAmount_throwsIllegalArgument() {
        LoanDTO dto = validCreateDTO();
        dto.getBooks()[0].setRequestedAmount(0);

        when(entityManager.find(User.class, student.getId())).thenReturn(student);
        when(entityManager.find(Location.class, location.getId())).thenReturn(location);

        assertThatThrownBy(() -> loanService.createLoan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hoeveelheid moet groter zijn dan 0");
    }

    @Test
    void createLoan_success_returnsDTOList() {
        LoanDTO dto = validCreateDTO();
        when(entityManager.find(User.class, student.getId())).thenReturn(student);
        when(entityManager.find(Location.class, location.getId())).thenReturn(location);
        when(entityManager.find(Book.class, book.getId())).thenReturn(book);
        when(locationBookRepository.findByLocationIdAndBookId(location.getId(), book.getId()))
                .thenReturn(Optional.of(locationBook));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> {
            Loan l = inv.getArgument(0);
            l.setId(100L);
            return l;
        });
        when(loanBookRepository.save(any(LoanBook.class))).thenAnswer(inv -> inv.getArgument(0));
        stubLookup();

        List<LoanDTO> result = loanService.createLoan(dto);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(student.getId());
    }

    @Test
    void createLoan_multipleBooks_groupIdIsSet() {
        Book book2 = new Book();
        book2.setId(31L);
        book2.setTitle("Boek 2");
        LocationBook lb2 = new LocationBook();
        lb2.setId(41L);
        lb2.setBook(book2);
        lb2.setLocation(location);
        lb2.setCurrentAmount(2);

        LoanBookDTO lbDTO1 = new LoanBookDTO();
        lbDTO1.setBookId(book.getId());
        lbDTO1.setRequestedAmount(1);
        LoanBookDTO lbDTO2 = new LoanBookDTO();
        lbDTO2.setBookId(book2.getId());
        lbDTO2.setRequestedAmount(1);

        LoanDTO dto = validCreateDTO();
        dto.setBooks(new LoanBookDTO[] { lbDTO1, lbDTO2 });

        when(entityManager.find(User.class, student.getId())).thenReturn(student);
        when(entityManager.find(Location.class, location.getId())).thenReturn(location);
        when(entityManager.find(Book.class, book.getId())).thenReturn(book);
        when(entityManager.find(Book.class, book2.getId())).thenReturn(book2);
        when(locationBookRepository.findByLocationIdAndBookId(location.getId(), book.getId()))
                .thenReturn(Optional.of(locationBook));
        when(locationBookRepository.findByLocationIdAndBookId(location.getId(), book2.getId()))
                .thenReturn(Optional.of(lb2));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> {
            Loan l = inv.getArgument(0);
            l.setId(new Random().nextLong());
            return l;
        });
        when(loanBookRepository.save(any(LoanBook.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lookupService.getUser(anyLong(), anyString(), any()))
                .thenReturn(Map.of("givenName", "Anna", "familyName", "Aerts"));

        List<LoanDTO> result = loanService.createLoan(dto);

        assertThat(result).hasSize(2);
        String groupId = result.get(0).getGroupId();
        assertThat(groupId).isNotNull();
        assertThat(result.get(1).getGroupId()).isEqualTo(groupId);
    }

    @Test
    void createLoan_singleBook_groupIdIsNull() {
        LoanDTO dto = validCreateDTO();
        when(entityManager.find(User.class, student.getId())).thenReturn(student);
        when(entityManager.find(Location.class, location.getId())).thenReturn(location);
        when(entityManager.find(Book.class, book.getId())).thenReturn(book);
        when(locationBookRepository.findByLocationIdAndBookId(location.getId(), book.getId()))
                .thenReturn(Optional.of(locationBook));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> {
            Loan l = inv.getArgument(0);
            l.setId(1L);
            return l;
        });
        when(loanBookRepository.save(any(LoanBook.class))).thenAnswer(inv -> inv.getArgument(0));
        stubLookup();

        List<LoanDTO> result = loanService.createLoan(dto);

        assertThat(result.get(0).getGroupId()).isNull();
    }

    // ── updateNote ────────────────────────────────────────────────

    @Test
    void updateNote_success_updatesNote() {
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(loanRepository.save(loan)).thenReturn(loan);
        stubLookup();

        LoanDTO result = loanService.updateNote(loan.getId(), "nieuwe notitie");

        assertThat(result.getNote()).isEqualTo("nieuwe notitie");
    }

    @Test
    void updateNote_tooLong_throwsIllegalArgument() {
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        String tooLong = "x".repeat(256);

        assertThatThrownBy(() -> loanService.updateNote(loan.getId(), tooLong))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("te lang");
    }

    @Test
    void updateNote_loanNotFound_throwsEntityNotFound() {
        when(loanRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.updateNote(999L, "text"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Loan niet gevonden");
    }

    // ── updateStatus ──────────────────────────────────────────────

    @Test
    void updateStatus_toReceived_pushesNotification() {
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(loanRepository.save(loan)).thenReturn(loan);
        stubLookup();

        loanService.updateStatus(loan.getId(), LoanStatus.RECEIVED);

        ArgumentCaptor<NotificationTask> captor = ArgumentCaptor.forClass(NotificationTask.class);
        verify(taskQueueService).push(captor.capture());
    }

    @Test
    void updateStatus_toDeclined_restoresStock() {
        loan.setStatus(LoanStatus.REQUESTED);
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(loanRepository.save(loan)).thenReturn(loan);
        when(locationBookRepository.findByLocationIdAndBookId(location.getId(), book.getId()))
                .thenReturn(Optional.of(locationBook));
        stubLookup();

        loanService.updateStatus(loan.getId(), LoanStatus.DECLINED);

        verify(locationBookService).updateCurrentAmount(eq(locationBook), eq(-loanBook.getRequestedAmount()));
    }

    @Test
    void updateStatus_toReturned_restoresStock() {
        loan.setStatus(LoanStatus.RECEIVED);
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(loanRepository.save(loan)).thenReturn(loan);
        when(locationBookRepository.findByLocationIdAndBookId(location.getId(), book.getId()))
                .thenReturn(Optional.of(locationBook));
        stubLookup();

        loanService.updateStatus(loan.getId(), LoanStatus.RETURNED);

        verify(locationBookService).updateCurrentAmount(eq(locationBook), eq(-loanBook.getRequestedAmount()));
    }

    @Test
    void updateStatus_loanNotFound_throwsEntityNotFound() {
        when(loanRepository.findById(999L)).thenReturn(Optional.empty());
        // RECEIVED check happens before the findById call
        when(loanRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.updateStatus(999L, LoanStatus.ACCEPTED))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ── scanPickup ────────────────────────────────────────────────

    @Test
    void scanPickup_allReceived_setsStatusReceivedAndPushesNotification() {
        BookCopy copy = buildCopy(60L);
        loanBook.setRequestedAmount(1);
        loanBook.setReceivedAmount(0);

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(bookCopyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));
        when(loanBookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(loanRepository.save(any())).thenReturn(loan);
        stubLookup();

        loanService.scanPickup(loan.getId(), copy.getId());

        assertThat(loan.getStatus()).isEqualTo(LoanStatus.RECEIVED);
        verify(taskQueueService).push(any(NotificationTask.class));
    }

    @Test
    void scanPickup_alreadyScanned_throwsIllegalArgument() {
        BookCopy copy = buildCopy(60L);
        loanBook.setReceivedAmount(0);
        loanBook.getScannedCopyIds().add(copy.getId());

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(bookCopyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));

        assertThatThrownBy(() -> loanService.scanPickup(loan.getId(), copy.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al gescand");
    }

    @Test
    void scanPickup_allAlreadyReceived_throwsIllegalArgument() {
        BookCopy copy = buildCopy(60L);
        loanBook.setRequestedAmount(1);
        loanBook.setReceivedAmount(1);

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(bookCopyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));

        assertThatThrownBy(() -> loanService.scanPickup(loan.getId(), copy.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al ontvangen");
    }

    @Test
    void scanPickup_wrongBook_throwsIllegalArgument() {
        Book otherBook = new Book();
        otherBook.setId(999L);
        LocationBook otherLB = new LocationBook();
        otherLB.setBook(otherBook);
        BookCopy copy = new BookCopy();
        copy.setId(60L);
        copy.setAccessionId("ACC-60");
        copy.setLocationBook(otherLB);

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(bookCopyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));

        assertThatThrownBy(() -> loanService.scanPickup(loan.getId(), copy.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hoort niet bij dit boek");
    }

    // ── scanReturn ────────────────────────────────────────────────

    @Test
    void scanReturn_allReturned_setsStatusReturnedAndRestoresStock() {
        BookCopy copy = buildCopy(60L);
        loanBook.setRequestedAmount(1);
        loanBook.setReceivedAmount(1);
        loanBook.getScannedCopyIds().add(copy.getId());

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(bookCopyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));
        when(loanBookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(bookCopyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(locationBookRepository.findByLocationIdAndBookId(location.getId(), book.getId()))
                .thenReturn(Optional.of(locationBook));
        when(loanRepository.save(any())).thenReturn(loan);
        stubLookup();

        loanService.scanReturn(loan.getId(), copy.getId(), null, false);

        assertThat(loan.getStatus()).isEqualTo(LoanStatus.RETURNED);
        verify(locationBookService).updateCurrentAmount(eq(locationBook), eq(-loanBook.getRequestedAmount()));
    }

    @Test
    void scanReturn_alreadyReturned_throwsIllegalArgument() {
        BookCopy copy = buildCopy(60L);
        loanBook.setReceivedAmount(1);
        loanBook.setReturnedAmount(1);

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(bookCopyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));

        assertThatThrownBy(() -> loanService.scanReturn(loan.getId(), copy.getId(), null, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al teruggebracht");
    }

    @Test
    void scanReturn_copyNotInLoan_throwsIllegalArgument() {
        BookCopy copy = buildCopy(60L);
        loanBook.setReceivedAmount(1);
        loanBook.getScannedCopyIds().add(999L); // different copy was scanned

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(bookCopyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));

        assertThatThrownBy(() -> loanService.scanReturn(loan.getId(), copy.getId(), null, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("werd niet uitgeleend");
    }

    @Test
    void scanReturn_alreadyReturnedCopy_throwsIllegalArgument() {
        BookCopy copy = buildCopy(60L);
        loanBook.setReceivedAmount(1);
        loanBook.getScannedCopyIds().add(copy.getId());
        loanBook.getReturnedCopyIds().add(copy.getId()); // already returned this specific copy

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(bookCopyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));

        assertThatThrownBy(() -> loanService.scanReturn(loan.getId(), copy.getId(), null, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al teruggebracht");
    }

    // ── extendLoan ────────────────────────────────────────────────

    @Test
    void extendLoan_maxExtensionsReached_throwsIllegalArgument() {
        loan.setStatus(LoanStatus.RECEIVED);
        loan.setExtended((byte) school.getExtendLimit()); // already at limit

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.extendLoan(loan.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Maximum aantal verlengingen");
    }

    @Test
    void extendLoan_wrongStatus_throwsIllegalArgument() {
        loan.setStatus(LoanStatus.REQUESTED);

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.extendLoan(loan.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("kan niet verlengd worden");
    }

    @Test
    void extendLoan_loanNotFound_throwsEntityNotFound() {
        when(loanRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.extendLoan(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Lening niet gevonden");
    }

    @Test
    void extendLoan_acceptedStatus_isAllowed() {
        loan.setStatus(LoanStatus.ACCEPTED);
        loan.setExtended((byte) 0);

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(loanRepository.save(loan)).thenReturn(loan);
        stubLookup();

        assertThatCode(() -> loanService.extendLoan(loan.getId())).doesNotThrowAnyException();
    }

    // ── deleteLoan ────────────────────────────────────────────────

    @Test
    void deleteLoan_success_callsDelete() {
        when(loanRepository.existsById(loan.getId())).thenReturn(true);

        loanService.deleteLoan(loan.getId());

        verify(loanRepository).deleteById(loan.getId());
    }

    @Test
    void deleteLoan_notFound_throwsRuntimeException() {
        when(loanRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> loanService.deleteLoan(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("niet gevonden");
    }

    // ── getOverdueLoansLength / getDueSoonLoansLength ─────────────

    @Test
    void getOverdueLoansLength_delegatesToRepository() {
        when(loanRepository.countOverdueLoans(any(), any(), eq(school.getId()))).thenReturn(3);

        assertThat(loanService.getOverdueLoansLength(school.getId())).isEqualTo(3);
    }

    @Test
    void getDueSoonLoansLength_delegatesToRepository() {
        when(loanRepository.countDueSoonLoans(any(), any(), any(), eq(school.getId()))).thenReturn(5);

        assertThat(loanService.getDueSoonLoansLength(school.getId())).isEqualTo(5);
    }

    // ── getTopBooksThisMonth ──────────────────────────────────────

    @Test
    void getTopBooksThisMonth_returnsTop5SortedByCount() {
        List<Loan> loans = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Book b = new Book();
            b.setId((long) i);
            b.setTitle("Book " + i);
            LoanBook lb = new LoanBook();
            lb.setBook(b);
            lb.setRequestedAmount(1);
            Loan l = new Loan();
            l.setLoanBooks(new HashSet<>(Set.of(lb)));
            loans.add(l);
        }
        // give Book 0 two entries so it ranks first
        Book topBook = loans.get(0).getLoanBooks().iterator().next().getBook();
        LoanBook extra = new LoanBook();
        extra.setBook(topBook);
        extra.setRequestedAmount(1);
        Loan extraLoan = new Loan();
        extraLoan.setLoanBooks(new HashSet<>(Set.of(extra)));
        loans.add(extraLoan);

        when(loanRepository.findByDateRangeWithBooks(any(), any(), any(), eq(school.getId())))
                .thenReturn(loans);

        List<TopBookDTO> result = loanService.getTopBooksThisMonth(school.getId());

        assertThat(result).hasSizeLessThanOrEqualTo(5);
        // The top entry must be "Book 0" (appeared twice) with count 2
        TopBookDTO top = result.get(0);
        assertThat(top).extracting("title", "count")
                .containsExactly("Book 0", 2);
    }

    // ── username fallback (null oneRosterId) ──────────────────────

    @Test
    void getByUserId_nullOneRosterId_fallsBackToUsername() {
        student.setOneRosterId(null);
        when(loanRepository.findByUserId(student.getId())).thenReturn(List.of(loan));

        List<LoanDTO> result = loanService.getByUserId(student.getId());

        assertThat(result.get(0).getUsername()).isEqualTo(student.getUsername());
        verify(lookupService, never()).getUser(any(), any(), any());
    }

    // ── Private helper ────────────────────────────────────────────

    private BookCopy buildCopy(long id) {
        BookCopy copy = new BookCopy();
        copy.setId(id);
        copy.setAccessionId("ACC-" + id);
        copy.setLocationBook(locationBook);
        return copy;
    }
}