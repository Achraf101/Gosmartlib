package be.ap.backend.service;

import be.ap.backend.entity.Book;
import be.ap.backend.entity.Section;
import be.ap.backend.entity.SectionBook;
import be.ap.backend.repository.SectionBookRepository;
import be.ap.backend.repository.SectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SectionServiceTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private SectionBookRepository sectionBookRepository;

    @InjectMocks
    private SectionService sectionService;

    @Test
    public void getAllSections_returnsAllSections() {
        Section s1 = new Section();
        s1.setTitle("In de kijker");
        Section s2 = new Section();
        s2.setTitle("Boek van de maand");

        when(sectionRepository.findAll()).thenReturn(List.of(s1, s2));

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
}