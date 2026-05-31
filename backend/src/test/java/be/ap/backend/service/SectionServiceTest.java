package be.ap.backend.service;

import be.ap.backend.dto.SectionBookDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Section;
import be.ap.backend.entity.SectionBook;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.SectionBookRepository;
import be.ap.backend.repository.SectionRepository;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SectionServiceTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private SectionBookRepository sectionBookRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private SectionService sectionService;

    private Section section;
    private Book book;
    private SectionBook sectionBook;

    @BeforeEach
    void setUp() {
        section = new Section();
        section.setId(1L);
        section.setTitle("In de kijker");

        book = new Book();
        book.setId(1L);
        book.setTitle("De brief voor de koning");

        sectionBook = new SectionBook();
        sectionBook.setSection(section);
        sectionBook.setBook(book);
        sectionBook.setRanking((short) 1);
    }

    // -------------------------------------------------------------------------
    // getAllSections
    // -------------------------------------------------------------------------

    @Test
    void getAllSections_returnsAllSections() {
        Section s1 = new Section();
        s1.setTitle("In de kijker");
        Section s2 = new Section();
        s2.setTitle("Boek van de maand");
        when(sectionRepository.findByHiddenFalseAndSchoolIdOrderByRankingAsc(1L))
                .thenReturn(List.of(s1, s2));

        List<Section> result = sectionService.getAllSections(1L);

        assertEquals(2, result.size());
        assertEquals("In de kijker", result.get(0).getTitle());
    }

    // -------------------------------------------------------------------------
    // getBooksBySection
    // -------------------------------------------------------------------------

    @Test
    void getBooksBySection_returnsBooksForSection() {
        SectionBook sb = new SectionBook();
        sb.setBook(book);
        when(sectionBookRepository.findBySectionId(1L)).thenReturn(List.of(sb));

        List<Book> result = sectionService.getBooksBySection(1L);

        assertEquals(1, result.size());
        assertEquals("De brief voor de koning", result.get(0).getTitle());
    }

    @Test
    void getBooksBySection_returnsEmptyList_whenNoBooks() {
        when(sectionBookRepository.findBySectionId(99L)).thenReturn(List.of());

        List<Book> result = sectionService.getBooksBySection(99L);

        assertTrue(result.isEmpty());
    }

    // -------------------------------------------------------------------------
    // getBookBySectionAndGrade
    // -------------------------------------------------------------------------

    @Test
    void getBookBySectionAndGrade_returnsBook() {
        when(sectionBookRepository.findBySectionIdAndGrade(1L, (byte) 1))
                .thenReturn(Optional.of(sectionBook));

        Book result = sectionService.getBookBySectionAndGrade(1L, (byte) 1);

        assertNotNull(result);
        assertEquals("De brief voor de koning", result.getTitle());
    }

    @Test
    void getBookBySectionAndGrade_returnsNull_whenNotFound() {
        when(sectionBookRepository.findBySectionIdAndGrade(99L, (byte) 1))
                .thenReturn(Optional.empty());

        Book result = sectionService.getBookBySectionAndGrade(99L, (byte) 1);

        assertNull(result);
    }

    // -------------------------------------------------------------------------
    // setBookOfMonth
    // -------------------------------------------------------------------------

    @Test
    void setBookOfMonth_replacesExistingBook() {
        Book newBook = new Book();
        newBook.setId(2L);
        newBook.setTitle("Harry Potter");

        SectionBook existing = new SectionBook();
        existing.setBook(new Book());

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(newBook));
        when(sectionBookRepository.findBySectionIdAndGrade(1L, (byte) 1))
                .thenReturn(Optional.of(existing));

        Book result = sectionService.setBookOfMonth(1L, 2L, (byte) 1);

        assertNotNull(result);
        assertEquals("Harry Potter", result.getTitle());
        verify(sectionBookRepository).delete(existing);
        verify(sectionBookRepository).save(any(SectionBook.class));
    }

    @Test
    void setBookOfMonth_savesNewBook_whenNoExistingEntry() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(sectionBookRepository.findBySectionIdAndGrade(1L, (byte) 2))
                .thenReturn(Optional.empty());

        Book result = sectionService.setBookOfMonth(1L, 1L, (byte) 2);

        assertNotNull(result);
        assertEquals("De brief voor de koning", result.getTitle());
        verify(sectionBookRepository, never()).delete(any());
        verify(sectionBookRepository).save(any(SectionBook.class));
    }

    @Test
    void setBookOfMonth_throwsEntityNotFoundException_whenSectionNotFound() {
        when(sectionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.setBookOfMonth(99L, 1L, (byte) 1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Sectie niet gevonden");
    }

    @Test
    void setBookOfMonth_throwsEntityNotFoundException_whenBookNotFound() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.setBookOfMonth(1L, 99L, (byte) 1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Boek niet gevonden");
    }

    // -------------------------------------------------------------------------
    // setSpotlightBook
    // -------------------------------------------------------------------------

    @Test
    void setSpotlightBook_success() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(sectionBookRepository.findBySectionIdAndGradeIsNullOrderByRankingAsc(1L))
                .thenReturn(List.of());
        when(sectionBookRepository.findBySectionIdAndRanking(1L, (short) 1))
                .thenReturn(Optional.empty());

        Book result = sectionService.setSpotlightBook(1L, 1L, (short) 1);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("De brief voor de koning");
        verify(sectionBookRepository).save(any());
    }

    @Test
    void setSpotlightBook_replacesExistingRanking() {
        SectionBook existing = new SectionBook();
        existing.setRanking((short) 1);

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(sectionBookRepository.findBySectionIdAndGradeIsNullOrderByRankingAsc(1L))
                .thenReturn(List.of());
        when(sectionBookRepository.findBySectionIdAndRanking(1L, (short) 1))
                .thenReturn(Optional.of(existing));

        sectionService.setSpotlightBook(1L, 1L, (short) 1);

        verify(sectionBookRepository).delete(existing);
        verify(sectionBookRepository).save(any());
    }

    @Test
    void setSpotlightBook_throwsIllegalArgumentException_whenBookAlreadyInSpotlight() {
        // sectionBook (book id=1) is already present in the spotlight list
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(sectionBookRepository.findBySectionIdAndGradeIsNullOrderByRankingAsc(1L))
                .thenReturn(List.of(sectionBook));

        assertThatThrownBy(() -> sectionService.setSpotlightBook(1L, 1L, (short) 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Dit boek staat al in de kijker");

        verify(sectionBookRepository, never()).delete(any());
        verify(sectionBookRepository, never()).save(any());
    }

    @Test
    void setSpotlightBook_throwsEntityNotFoundException_whenSectionNotFound() {
        when(sectionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.setSpotlightBook(99L, 1L, (short) 1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Sectie niet gevonden");
    }

    @Test
    void setSpotlightBook_throwsEntityNotFoundException_whenBookNotFound() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.setSpotlightBook(1L, 99L, (short) 1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Boek niet gevonden");
    }

    // -------------------------------------------------------------------------
    // getSpotlightBooks
    // -------------------------------------------------------------------------

    @Test
    void getSpotlightBooks_returnsListOfDTOs() {
        when(sectionBookRepository.findBySectionIdAndGradeIsNullOrderByRankingAsc(1L))
                .thenReturn(List.of(sectionBook));

        List<SectionBookDTO> result = sectionService.getSpotlightBooks(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).ranking()).isEqualTo((short) 1);
        assertThat(result.get(0).book().getTitle()).isEqualTo("De brief voor de koning");
    }

    @Test
    void getSpotlightBooks_empty_returnsEmptyList() {
        when(sectionBookRepository.findBySectionIdAndGradeIsNullOrderByRankingAsc(1L))
                .thenReturn(List.of());

        List<SectionBookDTO> result = sectionService.getSpotlightBooks(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getSpotlightBooks_orderedByRanking() {
        SectionBook sb1 = new SectionBook();
        sb1.setRanking((short) 1);
        sb1.setBook(book);
        SectionBook sb2 = new SectionBook();
        sb2.setRanking((short) 3);
        sb2.setBook(book);

        when(sectionBookRepository.findBySectionIdAndGradeIsNullOrderByRankingAsc(1L))
                .thenReturn(List.of(sb1, sb2));

        List<SectionBookDTO> result = sectionService.getSpotlightBooks(1L);

        assertThat(result.get(0).ranking()).isEqualTo((short) 1);
        assertThat(result.get(1).ranking()).isEqualTo((short) 3);
    }
}