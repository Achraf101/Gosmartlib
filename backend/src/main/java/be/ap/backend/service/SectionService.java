package be.ap.backend.service;

import be.ap.backend.entity.Book;
import be.ap.backend.entity.Section;
import be.ap.backend.entity.SectionBook;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.SectionBookRepository;
import be.ap.backend.repository.SectionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;
    private final SectionBookRepository sectionBookRepository;
    private final BookRepository bookRepository;

    public SectionService(SectionRepository sectionRepository, SectionBookRepository sectionBookRepository, BookRepository bookRepository) {
        this.sectionRepository = sectionRepository;
        this.sectionBookRepository = sectionBookRepository;
        this.bookRepository = bookRepository;
    }

    public List<Section> getAllSections() {
        return sectionRepository.findByHiddenFalseOrderByRankingAsc();
    }

    public List<Book> getBooksBySection(Long sectionId) {
        return sectionBookRepository.findBySectionId(sectionId)
            .stream()
            .map(sb -> sb.getBook())
            .collect(Collectors.toList());
    }

    public Book getBookBySectionAndGrade(Long sectionId, Byte grade) {
        return sectionBookRepository.findBySectionIdAndGrade(sectionId, grade)
            .map(SectionBook::getBook)
            .orElse(null);
    }

    public Book setBookOfMonth(Long sectionId, Long bookId, Byte grade) {
        Section section = sectionRepository.findById(sectionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sectie niet gevonden"));

        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Boek niet gevonden"));

        
        sectionBookRepository.findBySectionIdAndGrade(sectionId, grade)
            .ifPresent(sectionBookRepository::delete);

        
        SectionBook sectionBook = new SectionBook();
        sectionBook.setSection(section);
        sectionBook.setBook(book);
        sectionBook.setGrade(grade);
        sectionBook.setRanking((short) 0);
        sectionBookRepository.save(sectionBook);

        return book;
    }
}