package be.ap.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import be.ap.backend.entity.Author;
import be.ap.backend.repository.AuthorRepository;

@SpringBootTest
public class AuthorControllerTest {

    @MockitoBean
    private AuthorRepository authorRepository;

    @Autowired
    private AuthorController controller;

    @Test
    void givenAuthorId_whenGetById_thenReturnAuthor() {
        
        Author author = new Author();
        author.setId(1L);
        author.setName("Tonke Dragt");
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

        
        Author result = controller.getById(1L);

        
        assertNotNull(result);
        assertEquals("Tonke Dragt", result.getName());
        verify(authorRepository, times(1)).findById(1L);
    }

    @Test
    void givenAuthorId_whenGetById_thenReturnNull_whenNotFound() {
        
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        
        Author result = controller.getById(99L);

        
        assertNull(result);
        verify(authorRepository, times(1)).findById(99L);
    }

    @Test
    void givenQuery_whenSearch_thenReturnMatchingAuthors() {
      
        Author author = new Author();
        author.setId(1L);
        author.setName("Tonke Dragt");
        when(authorRepository.searchByName("tonke")).thenReturn(List.of(author));

        
        List<Author> result = controller.searchAuthor("tonke");

      
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Tonke Dragt", result.get(0).getName());
        verify(authorRepository, times(1)).searchByName("tonke");
    }

    @Test
    void givenQuery_whenSearch_thenReturnEmpty_whenNoMatch() {
        
        when(authorRepository.searchByName("xyz")).thenReturn(List.of());

       
        List<Author> result = controller.searchAuthor("xyz");

        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(authorRepository, times(1)).searchByName("xyz");
    }
}