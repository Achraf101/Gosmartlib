package be.ap.backend.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import be.ap.backend.dto.LoanBookDTO;
import be.ap.backend.dto.LoanDTO;
import be.ap.backend.dto.TopBookDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Location;
import be.ap.backend.entity.LocationBook;
import be.ap.backend.entity.School;
import be.ap.backend.entity.Loan;
import be.ap.backend.entity.LoanBook;
import be.ap.backend.entity.LoanStatus;
import be.ap.backend.entity.User;
import be.ap.backend.repository.LocationBookRepository;
import be.ap.backend.repository.LoanBookRepository;
import be.ap.backend.repository.LoanRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class LoanService {
    private LoanRepository loanRepository;
    private EntityManager entityManager;
    private LoanBookRepository loanBookRepository;
    private LocationBookRepository locationBookRepository;
    private LocationBookService locationBookService;

    @Autowired
    public LoanService(LoanRepository loanRepository, EntityManager entityManager,
            LoanBookRepository loanBookRepository, LocationBookRepository locationBookRepository,
            LocationBookService locationBookService) {
        this.loanRepository = loanRepository;
        this.entityManager = entityManager;
        this.loanBookRepository = loanBookRepository;
        this.locationBookRepository = locationBookRepository;
        this.locationBookService = locationBookService;
    }

    @Transactional
    public LoanDTO createLoan(LoanDTO dto) {
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

        for (LoanBookDTO lbDTO : dto.getBooks()) {
            if (lbDTO.getBookId() == null) {
                throw new IllegalArgumentException("bookId is verplicht voor elk boek");
            }
            if (lbDTO.getRequestedAmount() == null || lbDTO.getRequestedAmount() <= 0) {
                throw new IllegalArgumentException("aangevraagde hoeveelheid moet groter zijn dan 0 voor elk boek");
            }
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
                    "Aantal boeken is groter dan de uitleen limiet van je location " + school.getBorrowLimit());
        }
        LocalDate expectedEnd = dto.getStart().plusDays(school.getBorrowPeriod());
        if (!dto.getEnd().equals(expectedEnd)) {
            throw new IllegalArgumentException(
                    "Einddatum moet exact " + school.getBorrowPeriod() + " dagen na begindatum zijn");
        }

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setLocation(location);
        loan.setExtended(dto.getExtended());
        loan.setStart(dto.getStart());
        loan.setEnd(dto.getEnd());
        loan.setNote(dto.getNote());
        loan.setStatus(dto.getStatus());
        loan.setClosed(dto.getClosed());

        Loan savedLoan = loanRepository.save(loan);

        List<LoanBook> loanBooks = Arrays.stream(dto.getBooks())
                .map(lbDTO -> {
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

                    LoanBook lb = new LoanBook();
                    lb.setLoan(savedLoan);
                    lb.setBook(entityManager.find(Book.class, lbDTO.getBookId()));
                    lb.setRequestedAmount(lbDTO.getRequestedAmount());
                    lb.setReceivedAmount(0);
                    lb.setReturnedAmount(0);
                    return lb;
                })
                .collect(Collectors.toList());

        loanBookRepository.saveAll(loanBooks);
        savedLoan.setLoanBooks(new HashSet<>(loanBooks));
        return toDTO(savedLoan);
    }

    public List<LoanDTO> getRequested() {
        return loanRepository.findByStatusWithBooks(LoanStatus.REQUESTED).stream()
                .map(this::toDTO)
                .toList();
    }

    public LoanDTO updateNote(Long id, String note) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Loan niet gevonden met id: " + id));
        if (note.length() > 255) {
            throw new IllegalArgumentException("Notitie is te lang!");
        }
        loan.setNote(note);
        return toDTO(loanRepository.save(loan));
    }

    public LoanDTO updateStatus(Long id, LoanStatus status) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Loan niet gevonden met id: " + id));
        loan.setStatus(status);
        if (status == LoanStatus.DECLINED) {
            loan.getLoanBooks().forEach(lb -> {
                LocationBook locationBook = locationBookRepository
                        .findByLocationIdAndBookId(loan.getLocation().getId(), lb.getBook().getId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Book niet gevonden in locatie: " + lb.getId()));
                locationBookService.updateCurrentAmount(locationBook, -lb.getRequestedAmount());
            });
        }
        return toDTO(loanRepository.save(loan));
    }

    public List<LoanDTO> getByUserId(Long userId) {
        return loanRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .toList();
    }

    public void deleteLoan(Long id) {
        if (!loanRepository.existsById(id)) {
            throw new RuntimeException("Uitlening niet gevonden!");
        }

        loanRepository.deleteById(id);
    }

    private LoanDTO toDTO(Loan loan) {
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
        dto.setUsername(loan.getUser().getUsername());

        LoanBookDTO[] books = loan.getLoanBooks().stream()
                .map(lb -> {
                    LoanBookDTO lbDto = new LoanBookDTO();
                    lbDto.setId(lb.getId());
                    lbDto.setBookId(lb.getBook().getId());
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

    public int getOverdueLoansLength(Long locationId) {
        List<LoanStatus> activeStatuses = List.of(LoanStatus.RECEIVED, LoanStatus.ACCEPTED);
        return loanRepository.countOverdueLoans(activeStatuses, LocalDate.now(), locationId);
    }

    public List<LoanDTO> getOverdueLoans(Long locationId) {
        List<LoanStatus> activeStatuses = List.of(LoanStatus.RECEIVED, LoanStatus.ACCEPTED);
        return loanRepository.findOverdueLoans(activeStatuses, LocalDate.now(), locationId)
                .stream().map(this::toDTO).toList();
    }

    public List<TopBookDTO> getTopBooksThisMonth(Long locationId) {
        LocalDate from = LocalDate.now().withDayOfMonth(1);
        LocalDate to = LocalDate.now();
        List<LoanStatus> statuses = List.of(LoanStatus.RECEIVED, LoanStatus.RETURNED, LoanStatus.ACCEPTED);

        return loanRepository.findByDateRangeWithBooks(from, to, statuses, locationId).stream()
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

    public List<LoanDTO> getDueSoonLoans(Long locationId) {
        List<LoanStatus> activeStatuses = List.of(LoanStatus.RECEIVED, LoanStatus.ACCEPTED);
        return loanRepository.findDueSoonLoans(activeStatuses, LocalDate.now(), LocalDate.now().plusDays(7), locationId)
                .stream().map(this::toDTO).toList();
    }

    public int getDueSoonLoansLength(Long locationId) {
        List<LoanStatus> activeStatuses = List.of(LoanStatus.RECEIVED, LoanStatus.ACCEPTED);
        return loanRepository.countDueSoonLoans(activeStatuses, LocalDate.now(), LocalDate.now().plusDays(7),
                locationId);
    }

    public List<TopBookDTO> getTopGenresThisMonth(Long locationId) {
        LocalDate from = LocalDate.now().withDayOfMonth(1);
        LocalDate to = LocalDate.now();
        List<LoanStatus> statuses = List.of(LoanStatus.RECEIVED, LoanStatus.RETURNED, LoanStatus.ACCEPTED);

        return loanRepository.findTopGenres(statuses, from, to, locationId).stream()
                .limit(5)
                .map(row -> new TopBookDTO((String) row[0], ((Long) row[1]).intValue()))
                .toList();
    }
}
