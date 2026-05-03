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
import be.ap.backend.entity.Campus;
import be.ap.backend.entity.CampusBook;
import be.ap.backend.entity.Loan;
import be.ap.backend.entity.LoanBook;
import be.ap.backend.entity.LoanStatus;
import be.ap.backend.entity.User;
import be.ap.backend.repository.CampusBookRepository;
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
    private CampusBookRepository campusBookRepository;
    private CampusBookService campusBookService;

    @Autowired
    public LoanService(LoanRepository loanRepository, EntityManager entityManager,
            LoanBookRepository loanBookRepository, CampusBookRepository campusBookRepository,
            CampusBookService campusBookService) {
        this.loanRepository = loanRepository;
        this.entityManager = entityManager;
        this.loanBookRepository = loanBookRepository;
        this.campusBookRepository = campusBookRepository;
        this.campusBookService = campusBookService;
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
        Campus campus = entityManager.find(Campus.class, dto.getCampusId());
        if (campus == null) {
            throw new EntityNotFoundException("Campus niet gevonden met id: " + dto.getCampusId());
        }
        if (dto.getBooks().length > campus.getBorrowLimit()) {
            throw new IllegalArgumentException(
                    "Aantal boeken is groter dan de uitleen limiet van je campus " + campus.getBorrowLimit());
        }
        LocalDate expectedEnd = dto.getStart().plusDays(campus.getBorrowPeriod());
        if (!dto.getEnd().equals(expectedEnd)) {
            throw new IllegalArgumentException(
                    "Einddatum moet exact " + campus.getBorrowPeriod() + " dagen na begindatum zijn");
        }

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setCampus(campus);
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

                    CampusBook campusBook = campusBookRepository
                            .findByCampusIdAndBookId(dto.getCampusId(), lbDTO.getBookId())
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Boek niet gevonden in campus: " + lbDTO.getBookId()));

                    if (lbDTO.getRequestedAmount() > campusBook.getCurrentAmount()) {
                        throw new IllegalArgumentException(
                                "Gevraagde hoeveel voor boek " + lbDTO.getBookId() +
                                        " is niet meer beschikbaar, aantal beschikbaar: "
                                        + campusBook.getCurrentAmount());
                    }

                    campusBookService.updateCurrentAmount(campusBook, lbDTO.getRequestedAmount());

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
                CampusBook campusBook = campusBookRepository
                        .findByCampusIdAndBookId(loan.getCampus().getId(), lb.getBook().getId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Book not found on campus: " + lb.getId()));
                campusBookService.updateCurrentAmount(campusBook, -lb.getRequestedAmount());
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
        dto.setCampusId(loan.getCampus().getId());
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

    public int getOverdueLoansLength(Long campusId){
        List<LoanStatus> activeStatuses = List.of(LoanStatus.RECEIVED, LoanStatus.ACCEPTED);
        return loanRepository.countOverdueLoans(activeStatuses, LocalDate.now(), campusId);
    }

    public List<LoanDTO> getOverdueLoans(Long campusId) {
        List<LoanStatus> activeStatuses = List.of(LoanStatus.RECEIVED, LoanStatus.ACCEPTED);
        return loanRepository.findOverdueLoans(activeStatuses, LocalDate.now(), campusId)
            .stream().map(this::toDTO).toList();
    }

    public List<TopBookDTO> getTopBooksThisMonth(Long campusId) {
        LocalDate from = LocalDate.now().withDayOfMonth(1);
        LocalDate to = LocalDate.now();
        List<LoanStatus> statuses = List.of(LoanStatus.RECEIVED, LoanStatus.RETURNED, LoanStatus.ACCEPTED);

        return loanRepository.findByDateRangeWithBooks(from, to, statuses, campusId).stream()
            .flatMap(l -> l.getLoanBooks().stream())
            .collect(Collectors.groupingBy(
                lb -> lb.getBook().getTitle(),
                Collectors.summingInt(lb -> 1)
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .map(e -> new TopBookDTO(e.getKey(), e.getValue()))
            .toList();
    }

    public List<LoanDTO> getDueSoonLoans(Long campusId) {
        List<LoanStatus> activeStatuses = List.of(LoanStatus.RECEIVED, LoanStatus.ACCEPTED);
        return loanRepository.findDueSoonLoans(activeStatuses, LocalDate.now(), LocalDate.now().plusDays(7), campusId)
            .stream().map(this::toDTO).toList();
    }

    public int getDueSoonLoansLength(Long campusId){
        List<LoanStatus> activeStatuses = List.of(LoanStatus.RECEIVED, LoanStatus.ACCEPTED);
        return loanRepository.countDueSoonLoans(activeStatuses, LocalDate.now(), LocalDate.now().plusDays(7), campusId);
    }

    public List<TopBookDTO> getTopGenresThisMonth(Long campusId) {
        LocalDate from = LocalDate.now().withDayOfMonth(1);
        LocalDate to = LocalDate.now();
        List<LoanStatus> statuses = List.of(LoanStatus.RECEIVED, LoanStatus.RETURNED, LoanStatus.ACCEPTED);

        return loanRepository.findTopGenres(statuses, from, to, campusId).stream()
            .limit(5)
            .map(row -> new TopBookDTO((String) row[0], ((Long) row[1]).intValue()))
            .toList();
    }
}
