package be.ap.backend.controller;

import be.ap.backend.entity.Book;
import be.ap.backend.entity.Section;
import be.ap.backend.service.SectionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SectionControllerTest {

    @Mock
    private SectionService sectionService;

    @InjectMocks
    private SectionController sectionController;

    @Test
    public void getAllSections_returnsStatus200() {
        when(sectionService.getAllSections()).thenReturn(List.of());

        List<Section> result = sectionController.getAllSections();

        assertNotNull(result);
    }

    @Test
    public void getBooksBySection_returnsStatus200() {
        Book book = new Book();
        book.setTitle("Harry Potter en de vuurbeker");

        when(sectionService.getBooksBySection(1L)).thenReturn(List.of(book));

        List<Book> result = sectionController.getBooksBySection(1L);

        assertNotNull(result);
        assertEquals("Harry Potter en de vuurbeker", result.get(0).getTitle());
    }
}