package be.ap.backend.service;

import be.ap.backend.dto.SectionBookDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Section;
import be.ap.backend.entity.SectionBook;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.SectionBookRepository;
import be.ap.backend.repository.SectionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing homepage sections and their book assignments.
 */
@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sectionRepository;
    private final SectionBookRepository sectionBookRepository;
    private final BookRepository bookRepository;

    public List<Section> getAllSections(Long schoolId) {
        return sectionRepository.findByHiddenFalseAndSchoolIdOrderByRankingAsc(schoolId);
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

    /**
     * Assigns a book as the book of the month for the given grade, replacing any
     * existing assignment.
     *
     * @throws EntityNotFoundException if the section or book does not exist
     */
    public Book setBookOfMonth(Long sectionId, Long bookId, Byte grade) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Sectie niet gevonden"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Boek niet gevonden"));

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

    /**
     * Assigns a book to a spotlight position, replacing any existing book at that
     * ranking.
     *
     * @throws EntityNotFoundException  if the section or book does not exist
     * @throws IllegalArgumentException if the book is already in the spotlight
     *                                  section
     */
    public Book setSpotlightBook(Long sectionId, Long bookId, Short ranking) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Sectie niet gevonden"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Boek niet gevonden"));

        boolean alreadyExists = sectionBookRepository.findBySectionIdAndGradeIsNullOrderByRankingAsc(sectionId)
                .stream()
                .anyMatch(sb -> sb.getBook().getId().equals(bookId));

        if (alreadyExists) {
            throw new IllegalArgumentException("Dit boek staat al in de kijker");
        }

        sectionBookRepository.findBySectionIdAndRanking(sectionId, ranking)
                .ifPresent(sectionBookRepository::delete);

        SectionBook sectionBook = new SectionBook();
        sectionBook.setSection(section);
        sectionBook.setBook(book);
        sectionBook.setRanking(ranking);
        sectionBookRepository.save(sectionBook);

        return book;
    }

    /** Returns all spotlight books for the given section, ordered by ranking. */
    public List<SectionBookDTO> getSpotlightBooks(Long sectionId) {
        return sectionBookRepository.findBySectionIdAndGradeIsNullOrderByRankingAsc(sectionId)
                .stream()
                .map(sb -> new SectionBookDTO(sb.getRanking(), sb.getBook()))
                .collect(Collectors.toList());
    }
}