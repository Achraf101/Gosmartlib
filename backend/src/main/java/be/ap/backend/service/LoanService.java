package be.ap.backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import be.ap.backend.dto.LoanBookDTO;
import be.ap.backend.dto.LoanDTO;
import be.ap.backend.dto.LoanLookupContextDTO;
import be.ap.backend.dto.TopBookDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.BookCopy;
import be.ap.backend.entity.Location;
import be.ap.backend.entity.LocationBook;
import be.ap.backend.entity.School;
import be.ap.backend.entity.Loan;
import be.ap.backend.entity.LoanBook;
import be.ap.backend.entity.User;
import be.ap.backend.queue.NotificationTask;
import be.ap.backend.queue.TaskQueueService;
import be.ap.backend.enums.CopyStatus;
import be.ap.backend.enums.LoanStatus;
import be.ap.backend.repository.BookCopyRepository;
import be.ap.backend.repository.LocationBookRepository;
import be.ap.backend.repository.LoanBookRepository;
import be.ap.backend.repository.LoanRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;

@Service
@Slf4j
public class LoanService {
    private final LoanRepository loanRepository;
    private final EntityManager entityManager;
    private final LoanBookRepository loanBookRepository;
    private final LocationBookRepository locationBookRepository;
    private final LocationBookService locationBookService;
    private final BookCopyRepository bookCopyRepository;
    private final SmartschoolLookupService lookupService;
    private final Executor lookupExecutor;
    private final TaskQueueService taskQueueService;

    public LoanService(LoanRepository loanRepository, EntityManager entityManager,
            LoanBookRepository loanBookRepository, LocationBookRepository locationBookRepository,
            LocationBookService locationBookService, SmartschoolLookupService lookupService,
            @Qualifier("lookupExecutor") Executor lookupExecutor, TaskQueueService taskQueueService,
            BookCopyRepository bookCopyRepository) {
        this.loanRepository = loanRepository;
        this.entityManager = entityManager;
        this.loanBookRepository = loanBookRepository;
        this.locationBookRepository = locationBookRepository;
        this.locationBookService = locationBookService;
        this.bookCopyRepository = bookCopyRepository;
        this.lookupService = lookupService;
        this.lookupExecutor = lookupExecutor;
        this.taskQueueService = taskQueueService;
    }

    @Transactional
    public List<LoanDTO> createLoan(LoanDTO dto) {
        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("userId is verplicht");
        }
        if (dto.getStart() == null || dto.getEnd() == null) {
            throw new IllegalArgumentException("Begin- en einddatum zijn verplicht");
        }
        if (dto.getStart().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Begindatum kan niet in het verleden liggen");
        }
        if (!dto.getStart().isBefore(dto.getEnd())) {
            throw new IllegalArgumentException("Begindatum moet voor einddatum zijn");
        }
        if (dto.getBooks() == null || dto.getBooks().length == 0) {
            throw new IllegalArgumentException("minstens 1 boek is verplicht");
        }

        User user = entityManager.find(User.class, dto.getUserId());
        if (user == null) {
            throw new EntityNotFoundException("Gebruiker niet gevonden met id: " + dto.getUserId());
        }
        Location location = entityManager.find(Location.class, dto.getLocationId());
        if (location == null) {
            throw new EntityNotFoundException("Locatie niet gevonden met id: " + dto.getLocationId());
        }
        School school = location.getSchool();
        if (school == null) {
            throw new EntityNotFoundException("Geen school gekoppeld aan locatie: " + dto.getLocationId());
        }
        if (user.getSchool() == null || !user.getSchool().getId().equals(school.getId())) {
            throw new IllegalArgumentException("Gebruiker behoort niet tot de school van deze locatie");
        }
        if (dto.getBooks().length > school.getBorrowLimit()) {
            throw new IllegalArgumentException(
                    "Aantal boeken is groter dan de ontleenlimiet van je locatie " + school.getBorrowLimit());
        }
        LocalDate expectedEnd = dto.getStart().plusDays(school.getBorrowPeriod());
        if (!dto.getEnd().equals(expectedEnd)) {
            throw new IllegalArgumentException(
                    "Einddatum moet exact " + school.getBorrowPeriod() + " dagen na begindatum zijn");
        }

        String groupId = dto.getBooks().length > 1 ? java.util.UUID.randomUUID().toString() : null;

        List<LoanDTO> result = new ArrayList<>();

        for (LoanBookDTO lbDTO : dto.getBooks()) {
            if (lbDTO.getBookId() == null) {
                throw new IllegalArgumentException("bookId is verplicht voor elk boek");
            }
            if (lbDTO.getRequestedAmount() == null || lbDTO.getRequestedAmount() <= 0) {
                throw new IllegalArgumentException("aangevraagde hoeveelheid moet groter zijn dan 0");
            }
            Book book = entityManager.find(Book.class, lbDTO.getBookId());
            if (book == null) {
                throw new EntityNotFoundException("Boek niet gevonden met id: " + lbDTO.getBookId());
            }

            LocationBook locationBook = locationBookRepository
                    .findByLocationIdAndBookId(dto.getLocationId(), lbDTO.getBookId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Boek niet gevonden in locatie: " + lbDTO.getBookId()));

            if (lbDTO.getRequestedAmount() > locationBook.getCurrentAmount()) {
                throw new IllegalArgumentException(
                        "Gevraagde hoeveel voor boek " + lbDTO.getBookId() +
                                " is niet meer beschikbaar, aantal beschikbaar: "
                                + locationBook.getCurrentAmount());
            }

            locationBookService.updateCurrentAmount(locationBook, lbDTO.getRequestedAmount());

            Loan loan = new Loan();
            loan.setUser(user);
            loan.setLocation(location);
            loan.setExtended(dto.getExtended());
            loan.setStart(dto.getStart());
            loan.setEnd(dto.getEnd());
            loan.setNote(dto.getNote());
            loan.setStatus(dto.getStatus());
            loan.setClosed(dto.getClosed());
            loan.setGroupId(groupId);

            Loan savedLoan = loanRepository.save(loan);

            LoanBook lb = new LoanBook();
            lb.setLoan(savedLoan);
            lb.setBook(book);
            lb.setRequestedAmount(lbDTO.getRequestedAmount());
            lb.setReceivedAmount(0);
            lb.setReturnedAmount(0);

            loanBookRepository.save(lb);
            savedLoan.setLoanBooks(new HashSet<>(List.of(lb)));

            result.add(buildDTO(savedLoan));
        }
        return result;
    }

    public List<LoanDTO> getRequested() {
        return toDTOs(loanRepository.findByStatusWithBooks(LoanStatus.REQUESTED));
    }

    public LoanDTO updateNote(Long id, String note) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Loan niet gevonden met id: " + id));
        if (note.length() > 255) {
            throw new IllegalArgumentException("Notitie is te lang!");
        }
        loan.setNote(note);

        return buildDTO(loanRepository.save(loan));
    }

    public LoanDTO updateStatus(Long id, LoanStatus status) {
        // if received add notification to the queue
        if(status == LoanStatus.RECEIVED) {
            taskQueueService.push(new NotificationTask(NotificationTask.Type.LOAN, id));
        }

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Loan niet gevonden met id: " + id));
        loan.setStatus(status);
        if (status == LoanStatus.DECLINED || status == LoanStatus.RETURNED) {
            loan.getLoanBooks().forEach(lb -> {
                LocationBook locationBook = locationBookRepository
                        .findByLocationIdAndBookId(loan.getLocation().getId(), lb.getBook().getId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Book not found on location: " + lb.getId()));
                locationBookService.updateCurrentAmount(locationBook, -lb.getRequestedAmount());
            });
        }
        return buildDTO(loanRepository.save(loan));
    }

    @Transactional
    public LoanDTO scanPickup(Long loanId, Long bookCopyId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Lening niet gevonden: " + loanId));
        BookCopy copy = bookCopyRepository.findById(bookCopyId)
                .orElseThrow(() -> new EntityNotFoundException("Exemplaar niet gevonden: " + bookCopyId));

        LoanBook lb = loan.getLoanBooks().stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Geen boek gevonden in lening"));

        if (!copy.getLocationBook().getBook().getId().equals(lb.getBook().getId())) {
            throw new IllegalArgumentException("Exemplaar hoort niet bij dit boek");
        }
        if (lb.getReceivedAmount() >= lb.getRequestedAmount()) {
            throw new IllegalArgumentException("Alle exemplaren zijn al ontvangen");
        }
        if (lb.getScannedCopyIds().contains(bookCopyId)) {
            throw new IllegalArgumentException(
                    "Exemplaar " + copy.getAccessionId() + " is al gescand voor deze uitlening");
        }

        lb.getScannedCopyIds().add(bookCopyId);
        lb.setReceivedAmount(lb.getReceivedAmount() + 1);
        lb.setBookCopy(copy);
        loanBookRepository.save(lb);

        if (lb.getReceivedAmount() >= lb.getRequestedAmount()) {
            loan.setStatus(LoanStatus.RECEIVED);
        }
        return buildDTO(loanRepository.save(loan));
    }

    @Transactional
    public LoanDTO scanReturn(Long loanId, Long bookCopyId, String note, boolean damaged) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Lening niet gevonden: " + loanId));
        BookCopy copy = bookCopyRepository.findById(bookCopyId)
                .orElseThrow(() -> new EntityNotFoundException("Exemplaar niet gevonden: " + bookCopyId));

        LoanBook lb = loan.getLoanBooks().stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Geen boek gevonden in lening"));

        if (!copy.getLocationBook().getBook().getId().equals(lb.getBook().getId())) {
            throw new IllegalArgumentException("Exemplaar hoort niet bij dit boek");
        }
        if (lb.getReturnedAmount() >= lb.getReceivedAmount()) {
            throw new IllegalArgumentException("Alle exemplaren zijn al teruggebracht");
        }
        if (lb.getReturnedCopyIds().contains(bookCopyId)) {
            throw new IllegalArgumentException(
                    "Exemplaar " + copy.getAccessionId() + " is al teruggebracht voor deze uitlening");
        }
        if (!lb.getScannedCopyIds().isEmpty() && !lb.getScannedCopyIds().contains(bookCopyId)) {
            throw new IllegalArgumentException(
                    "Exemplaar " + copy.getAccessionId() + " werd niet uitgeleend voor deze uitlening");
        }

        lb.getReturnedCopyIds().add(bookCopyId);
        lb.setReturnedAmount(lb.getReturnedAmount() + 1);
        loanBookRepository.save(lb);

        if (note != null && !note.isBlank())
            copy.setNote(note.trim());
        if (damaged)
            copy.setStatus(CopyStatus.DAMAGED);
        bookCopyRepository.save(copy);

        if (lb.getReturnedAmount() >= lb.getReceivedAmount()) {
            LocationBook locationBook = locationBookRepository
                    .findByLocationIdAndBookId(loan.getLocation().getId(), lb.getBook().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Boek niet gevonden in locatie"));
            locationBookService.updateCurrentAmount(locationBook, -lb.getRequestedAmount());
            loan.setStatus(LoanStatus.RETURNED);
        }
        return buildDTO(loanRepository.save(loan));
    }

    @Transactional
    public LoanDTO pickupLoan(Long loanId, Long bookCopyId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Lening niet gevonden met id: " + loanId));

        for (LoanBook lb : loan.getLoanBooks()) {
            BookCopy copy;
            if (bookCopyId != null) {
                copy = bookCopyRepository.findById(bookCopyId)
                        .orElseThrow(() -> new EntityNotFoundException("Exemplaar niet gevonden: " + bookCopyId));
            } else {
                LocationBook locationBook = locationBookRepository
                        .findByLocationIdAndBookId(loan.getLocation().getId(), lb.getBook().getId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Boek niet gevonden in locatie voor exemplaar toewijzing"));
                List<BookCopy> available = bookCopyRepository
                        .findByLocationBookIdAndStatus(locationBook.getId(), CopyStatus.AVAILABLE);
                copy = available.isEmpty() ? null : available.get(0);
            }
            lb.setBookCopy(copy);
            loanBookRepository.save(lb);
        }

        loan.setStatus(LoanStatus.RECEIVED);
        return buildDTO(loanRepository.save(loan));
    }

    public List<LoanDTO> getByUserId(Long userId) {
        return toDTOs(loanRepository.findByUserId(userId));
    }

    public void deleteLoan(Long id) {
        if (!loanRepository.existsById(id)) {
            throw new RuntimeException("Uitlening niet gevonden!");
        }

        loanRepository.deleteById(id);
    }

    public List<LoanDTO> getByStateAndSchool(LoanStatus state, Long schoolId) {
        return toDTOs(loanRepository.findByStateAndSchool(state, schoolId));
    }

    /**
     * Performs the Smartschool lookup (with null guard on oneRosterId) and builds
     * the DTO. Use this for synchronous single-loan call sites.
     */
    private LoanDTO buildDTO(Loan loan) {
        String oneRosterId = loan.getUser().getOneRosterId();
        Map<String, Object> userInfo = null;
        if (oneRosterId != null) {
            userInfo = lookupService.getUser(
                    loan.getUser().getSchool(),
                    oneRosterId,
                    loan.getUser().getRoles());
        } else {
            log.warn("OneRoster ID is null voor gebruiker {}, Smartschool lookup overgeslagen",
                    loan.getUser().getId());
        }
        return toDTO(loan, userInfo);
    }

    private LoanDTO toDTO(Loan loan, Map<String, Object> userInfo) {
        LoanDTO dto = new LoanDTO();
        dto.setId(loan.getId());
        dto.setUserId(loan.getUser().getId());
        dto.setLocationId(loan.getLocation().getId());
        dto.setExtended(loan.getExtended());
        dto.setStart(loan.getStart());
        dto.setEnd(loan.getEnd());
        dto.setNote(loan.getNote());
        dto.setStatus(loan.getStatus());
        dto.setClosed(loan.getClosed());
        dto.setCreated(loan.getCreated());
        dto.setGroupId(loan.getGroupId());
        dto.setExtendPeriod(loan.getLocation().getSchool().getExtendPeriod());

        if (userInfo != null) {
            String firstName = (String) userInfo.get("givenName");
            String lastName = (String) userInfo.get("familyName");
            dto.setUsername(firstName + " " + lastName);
        } else {
            dto.setUsername(loan.getUser().getUsername());
        }

        LoanBookDTO[] books = loan.getLoanBooks().stream()
                .map(lb -> {
                    LoanBookDTO lbDto = new LoanBookDTO();
                    lbDto.setId(lb.getId());
                    lbDto.setBookId(lb.getBook().getId());
                    lbDto.setBookCopyId(lb.getBookCopy() != null ? lb.getBookCopy().getId() : null);
                    lbDto.setRequestedAmount(lb.getRequestedAmount());
                    lbDto.setReceivedAmount(lb.getReceivedAmount());
                    lbDto.setReturnedAmount(lb.getReturnedAmount());
                    lbDto.setBookTitle(lb.getBook().getTitle());
                    lbDto.setCover(lb.getBook().getCover());
                    lbDto.setAuthor(lb.getBook().getAuthor());
                    return lbDto;
                })
                .toArray(LoanBookDTO[]::new);
        dto.setBooks(books);
        return dto;
    }

    private List<LoanDTO> toDTOs(List<Loan> loans) {
        List<LoanLookupContextDTO> prepared = loans.stream()
                .map(loan -> {
                    loan.getLoanBooks().size();
                    LoanLookupContextDTO ctx = new LoanLookupContextDTO();
                    ctx.setLoan(loan);
                    ctx.setOneRosterId(loan.getUser().getOneRosterId());
                    ctx.setSchool(loan.getUser().getSchool());
                    ctx.setRoles(loan.getUser().getRoles());
                    return ctx;
                })
                .toList();

        List<CompletableFuture<LoanDTO>> futures = prepared.stream()
                .map(ctx -> CompletableFuture.supplyAsync(() -> {
                    Map<String, Object> userInfo = null;
                    if (ctx.getOneRosterId() != null) {
                        userInfo = lookupService.getUser(ctx.getSchool(), ctx.getOneRosterId(), ctx.getRoles());
                    } else {
                        log.warn("OneRoster ID is null voor loan {}, Smartschool lookup overgeslagen",
                                ctx.getLoan().getId());
                    }
                    return toDTO(ctx.getLoan(), userInfo);
                }, lookupExecutor))
                .toList();

        return futures.stream()
                .map(future -> {
                    try {
                        return future.join();
                    } catch (CompletionException e) {
                        log.error("Lookup mislukt: {}", e.getMessage());
                        throw new RuntimeException("Gebruiker kon niet opgehaald worden", e.getCause());
                    }
                })
                .toList();
    }

    public int getOverdueLoansLength(Long schoolId) {
        List<LoanStatus> activeStatuses = List.of(LoanStatus.RECEIVED, LoanStatus.ACCEPTED);
        return loanRepository.countOverdueLoans(activeStatuses, LocalDate.now(), schoolId);
    }

    public List<LoanDTO> getOverdueLoans(Long schoolId) {
        List<LoanStatus> activeStatuses = List.of(LoanStatus.RECEIVED, LoanStatus.ACCEPTED);
        return toDTOs(loanRepository.findOverdueLoans(activeStatuses, LocalDate.now(), schoolId));

    }

    public List<TopBookDTO> getTopBooksThisMonth(Long schoolId) {
        LocalDate from = LocalDate.now().withDayOfMonth(1);
        LocalDate to = LocalDate.now();
        List<LoanStatus> statuses = List.of(LoanStatus.RECEIVED, LoanStatus.RETURNED, LoanStatus.ACCEPTED);

        return loanRepository.findByDateRangeWithBooks(from, to, statuses, schoolId).stream()
                .flatMap(l -> l.getLoanBooks().stream())
                .collect(Collectors.groupingBy(
                        lb -> lb.getBook().getTitle(),
                        Collectors.summingInt(lb -> 1)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(e -> new TopBookDTO(e.getKey(), e.getValue()))
                .toList();
    }

    public List<LoanDTO> getDueSoonLoans(Long schoolId) {
        List<LoanStatus> activeStatuses = List.of(LoanStatus.RECEIVED, LoanStatus.ACCEPTED);
        return toDTOs(loanRepository.findDueSoonLoans(activeStatuses, LocalDate.now(), LocalDate.now().plusDays(7),
                schoolId));

    }

    public int getDueSoonLoansLength(Long schoolId) {
        List<LoanStatus> activeStatuses = List.of(LoanStatus.RECEIVED, LoanStatus.ACCEPTED);
        return loanRepository.countDueSoonLoans(activeStatuses, LocalDate.now(), LocalDate.now().plusDays(7),
                schoolId);
    }

    public List<TopBookDTO> getTopGenresThisMonth(Long schoolId) {
        LocalDate from = LocalDate.now().withDayOfMonth(1);
        LocalDate to = LocalDate.now();
        List<LoanStatus> statuses = List.of(LoanStatus.RECEIVED, LoanStatus.RETURNED, LoanStatus.ACCEPTED);

        return loanRepository.findTopGenres(statuses, from, to, schoolId).stream()
                .limit(5)
                .map(row -> new TopBookDTO((String) row[0], ((Long) row[1]).intValue()))
                .toList();
    }

    public LoanDTO extendLoan(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lening niet gevonden met id: " + id));

        if (loan.getStatus() != LoanStatus.RECEIVED && loan.getStatus() != LoanStatus.ACCEPTED) {
            throw new IllegalArgumentException("Lening kan niet verlengd worden met status: " + loan.getStatus());
        }

        School school = loan.getLocation().getSchool();

        if (loan.getExtended() >= school.getExtendLimit()) {
            throw new IllegalArgumentException("Maximum aantal verlengingen bereikt.");
        }

        loan.setEnd(loan.getEnd().plusDays(school.getExtendPeriod()));
        loan.setExtended((byte) (loan.getExtended() + 1));

        return buildDTO(loanRepository.save(loan));
    }

}
