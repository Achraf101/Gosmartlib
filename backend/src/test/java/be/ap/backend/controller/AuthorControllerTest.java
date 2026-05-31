package be.ap.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.ap.backend.entity.Author;
import be.ap.backend.service.AuthorService;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class) // No Spring context — no property resolution
public class AuthorControllerTest {

    @Mock
    private AuthorService authorService;

    @InjectMocks
    private AuthorController controller; // Constructed via @RequiredArgsConstructor

    // --- getAll ---

    @Test
    void whenGetAll_thenReturnAllAuthors() {
        Author a1 = new Author();
        a1.setId(1L);
        a1.setName("Tonke Dragt");
        Author a2 = new Author();
        a2.setId(2L);
        a2.setName("Annie M.G. Schmidt");

        when(authorService.getAll()).thenReturn(List.of(a1, a2));

        List<Author> result = controller.getAll().getBody();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Tonke Dragt", result.get(0).getName());
        assertEquals("Annie M.G. Schmidt", result.get(1).getName());
        verify(authorService, times(1)).getAll();
    }

    @Test
    void whenGetAll_thenReturnEmptyList_whenNoAuthorsExist() {
        when(authorService.getAll()).thenReturn(List.of());

        List<Author> result = controller.getAll().getBody();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(authorService, times(1)).getAll();
    }

    // --- getById ---

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
    void givenUnknownAuthorId_whenGetById_thenThrowEntityNotFoundException() {
        when(authorService.getById(99L))
                .thenThrow(new EntityNotFoundException("Auteur niet gevonden met id: 99"));

        assertThrows(EntityNotFoundException.class, () -> controller.getById(99L));
        verify(authorService, times(1)).getById(99L);
    }

    // --- searchAuthor ---

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
    void givenQuery_whenSearch_thenReturnEmptyList_whenNoMatch() {
        when(authorService.search("xyz")).thenReturn(List.of());

        List<Author> result = controller.searchAuthor("xyz").getBody();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(authorService, times(1)).search("xyz");
    }

    // --- addAuthor ---

    @Test
    void givenAuthor_whenAddAuthor_thenReturnPersistedAuthor() {
        Author input = new Author();
        input.setName("Roald Dahl");
        Author persisted = new Author();
        persisted.setId(3L);
        persisted.setName("Roald Dahl");

        when(authorService.addAuthor(input)).thenReturn(persisted);

        Author result = controller.addAuthor(input).getBody();

        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("Roald Dahl", result.getName());
        verify(authorService, times(1)).addAuthor(input);
    }

    @Test
    void givenAuthor_whenAddAuthor_thenPropagateException_whenServiceFails() {
        Author input = new Author();
        input.setName("");

        when(authorService.addAuthor(input))
                .thenThrow(new IllegalArgumentException("Author name must not be blank"));

        assertThrows(IllegalArgumentException.class, () -> controller.addAuthor(input));
        verify(authorService, times(1)).addAuthor(input);
    }
}