package be.ap.backend.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import be.ap.backend.dto.LoanBookDTO;
import be.ap.backend.dto.LoanDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Loan;
import be.ap.backend.entity.LoanBook;
import be.ap.backend.entity.LoanStatus;
import be.ap.backend.entity.User;
import be.ap.backend.repository.LoanBookRepository;
import be.ap.backend.repository.LoanRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

@Service
public class LoanService {

    private LoanRepository loanRepository;
    private EntityManager entityManager;
    private LoanBookRepository loanBookRepository;

    @Autowired
    public LoanService(LoanRepository loanRepository, EntityManager entityManager,
            LoanBookRepository loanBookRepository) {
        this.loanRepository = loanRepository;
        this.entityManager = entityManager;
        this.loanBookRepository = loanBookRepository;
    }

    public LoanDTO createLoan(LoanDTO dto) {
        Loan loan = new Loan();
        loan.setUser(entityManager.find(User.class, dto.getUserId()));
        // loan.setCampus(entityManager.find(Campus.class, dto.getCampusId()));
        loan.setExtended(dto.getExtended());
        loan.setStart(dto.getStart());
        loan.setEnd(dto.getEnd());
        loan.setNote(dto.getNote());
        loan.setStatus(dto.getStatus());
        loan.setClosed(dto.getClosed());

        Loan savedLoan = loanRepository.save(loan);

        List<LoanBook> loanBooks = Arrays.stream(dto.getBooks())
                .map(lbDTO -> {
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
        loan.setNote(note);
        return toDTO(loanRepository.save(loan));
    }

    public LoanDTO updateStatus(Long id, LoanStatus status) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Loan niet gevonden met id: " + id));
        loan.setStatus(status);
        return toDTO(loanRepository.save(loan));
    }

    private LoanDTO toDTO(Loan loan) {
        LoanDTO dto = new LoanDTO();
        dto.setId(loan.getId());
        dto.setUserId(loan.getUser().getId());
        // dto.setCampusId(loan.getCampus().getId());
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
                    return lbDto;
                })
                .toArray(LoanBookDTO[]::new);
        dto.setBooks(books);
        return dto;
    }
}
