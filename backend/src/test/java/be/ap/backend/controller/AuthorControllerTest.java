package be.ap.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import be.ap.backend.entity.Author;
import be.ap.backend.service.AuthorService;
import jakarta.persistence.EntityNotFoundException;

@SpringBootTest
public class AuthorControllerTest {

    @MockitoBean
    private AuthorService authorService;

    @Autowired
    private AuthorController controller;

    @Test
    void givenAuthorId_whenGetById_thenReturnAuthor() {
        Author author = new Author();
        author.setId(1L);
        author.setName("Tonke Dragt");
        when(authorService.getById(1L)).thenReturn(author);

        Author result = controller.getById(1L).getBody();

        assertNotNull(result);
        assertEquals("Tonke Dragt", result.getName());
        verify(authorService, times(1)).getById(1L);
    }

    @Test
    void givenAuthorId_whenGetById_thenReturnNull_whenNotFound() {
        when(authorService.getById(99L)).thenThrow(new EntityNotFoundException("Auteur niet gevonden met id: 99"));

        assertThrows(EntityNotFoundException.class, () -> controller.getById(99L));
        verify(authorService, times(1)).getById(99L);
    }

    @Test
    void givenQuery_whenSearch_thenReturnMatchingAuthors() {
        Author author = new Author();
        author.setId(1L);
        author.setName("Tonke Dragt");
        when(authorService.search("tonke")).thenReturn(List.of(author));

        List<Author> result = controller.searchAuthor("tonke").getBody();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Tonke Dragt", result.get(0).getName());
        verify(authorService, times(1)).search("tonke");
    }

    @Test
    void givenQuery_whenSearch_thenReturnEmpty_whenNoMatch() {
        when(authorService.search("xyz")).thenReturn(List.of());

        List<Author> result = controller.searchAuthor("xyz").getBody();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(authorService, times(1)).search("xyz");
    }
}