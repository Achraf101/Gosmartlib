package be.ap.backend.service;

import be.ap.backend.entity.Book;
import be.ap.backend.entity.Section;
import be.ap.backend.entity.SectionBook;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.SectionBookRepository;
import be.ap.backend.repository.SectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    public void getAllSections_returnsAllSections() {

        Section s1 = new Section();
        s1.setTitle("In de kijker");
        Section s2 = new Section();
        s2.setTitle("Boek van de maand");
        when(sectionRepository.findByHiddenFalseOrderByRankingAsc()).thenReturn(List.of(s1, s2));

        List<Section> result = sectionService.getAllSections();

        assertEquals(2, result.size());
        assertEquals("In de kijker", result.get(0).getTitle());
    }

    @Test
    public void getBooksBySection_returnsBooksForSection() {

        Book book = new Book();
        book.setTitle("De brief voor de koning");
        SectionBook sectionBook = new SectionBook();
        sectionBook.setBook(book);
        when(sectionBookRepository.findBySectionId(1L)).thenReturn(List.of(sectionBook));

        List<Book> result = sectionService.getBooksBySection(1L);

        assertEquals(1, result.size());
        assertEquals("De brief voor de koning", result.get(0).getTitle());
    }

    @Test
    public void getBooksBySection_returnsEmptyList_whenNoBooks() {

        when(sectionBookRepository.findBySectionId(99L)).thenReturn(List.of());

        List<Book> result = sectionService.getBooksBySection(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    public void getBookBySectionAndGrade_returnsBook() {

        Book book = new Book();
        book.setTitle("De brief voor de koning");
        SectionBook sectionBook = new SectionBook();
        sectionBook.setBook(book);
        when(sectionBookRepository.findBySectionIdAndGrade(1L, (byte) 1)).thenReturn(Optional.of(sectionBook));

        Book result = sectionService.getBookBySectionAndGrade(1L, (byte) 1);

        assertNotNull(result);
        assertEquals("De brief voor de koning", result.getTitle());
    }

    @Test
    public void setBookOfMonth_replacesExistingBook() {

        Section section = new Section();
        section.setId(1L);
        section.setTitle("Boek van de maand");

        Book newBook = new Book();
        newBook.setId(2L);
        newBook.setTitle("Harry Potter");

        SectionBook existing = new SectionBook();
        existing.setBook(new Book());

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(newBook));
        when(sectionBookRepository.findBySectionIdAndGrade(1L, (byte) 1)).thenReturn(Optional.of(existing));

        Book result = sectionService.setBookOfMonth(1L, 2L, (byte) 1);

        assertNotNull(result);
        assertEquals("Harry Potter", result.getTitle());
        verify(sectionBookRepository, times(1)).delete(existing);
        verify(sectionBookRepository, times(1)).save(any(SectionBook.class));
    }

    @Test
    public void setBookOfMonth_throwsNotFound_whenSectionNotFound() {

        when(sectionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            sectionService.setBookOfMonth(99L, 1L, (byte) 1);
        });
    }

    @Test
    public void setBookOfMonth_throwsNotFound_whenBookNotFound() {

        Section section = new Section();
        section.setId(1L);
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            sectionService.setBookOfMonth(1L, 99L, (byte) 1);
        });
    }
}