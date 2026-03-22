package be.ap.backend.controller;

import be.ap.backend.entity.Book;
import be.ap.backend.entity.Section;
import be.ap.backend.service.SectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@WebMvcTest(SectionController.class)
@ContextConfiguration(classes = SectionController.class)
public class SectionControllerTest {

    @MockitoBean
    private SectionService sectionService;

    @Autowired
    private SectionController sectionController;

    @Test
    public void getAllSections_returnsStatus200() {

        when(sectionService.getAllSections()).thenReturn(List.of());

        List<Section> result = sectionController.getAllSections();

        assertNotNull(result);
        verify(sectionService, times(1)).getAllSections();
    }

    @Test
    public void getBooksBySection_returnsStatus200() {

        Book book = new Book();
        book.setTitle("Harry Potter en de vuurbeker");
        when(sectionService.getBooksBySection(1L)).thenReturn(List.of(book));

        List<Book> result = sectionController.getBooksBySection(1L);

        assertNotNull(result);
        assertEquals("Harry Potter en de vuurbeker", result.get(0).getTitle());
        verify(sectionService, times(1)).getBooksBySection(1L);
    }

    @Test
    public void getBookBySectionAndGrade_returnsBook() {

        Book book = new Book();
        book.setTitle("De brief voor de koning");
        when(sectionService.getBookBySectionAndGrade(1L, (byte) 1)).thenReturn(book);

        Book result = sectionController.getBookBySectionAndGrade(1L, (byte) 1);

        assertNotNull(result);
        assertEquals("De brief voor de koning", result.getTitle());
        verify(sectionService, times(1)).getBookBySectionAndGrade(1L, (byte) 1);
    }

    @Test
    public void getBookBySectionAndGrade_returnsNull_whenNoBook() {

        when(sectionService.getBookBySectionAndGrade(1L, (byte) 2)).thenReturn(null);

        Book result = sectionController.getBookBySectionAndGrade(1L, (byte) 2);

        assertNull(result);
        verify(sectionService, times(1)).getBookBySectionAndGrade(1L, (byte) 2);
    }

    @Test
    public void setBookOfMonth_returnsBook() {

        Book book = new Book();
        book.setTitle("Harry Potter en de vuurbeker");
        when(sectionService.setBookOfMonth(1L, 2L, (byte) 1)).thenReturn(book);

        Book result = sectionController.setBookOfMonth(1L, 2L, (byte) 1);

        assertNotNull(result);
        assertEquals("Harry Potter en de vuurbeker", result.getTitle());
        verify(sectionService, times(1)).setBookOfMonth(1L, 2L, (byte) 1);
    }
}