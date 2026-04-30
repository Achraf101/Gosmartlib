package be.ap.backend.service;

import be.ap.backend.dto.CampusBookDTO;
import be.ap.backend.dto.CampusBookDetailDTO;
import be.ap.backend.entity.Author;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Campus;
import be.ap.backend.entity.CampusBook;
import be.ap.backend.exception.ArgumentsInvalidException;
import be.ap.backend.exception.BookAlreadyInCampusException;
import be.ap.backend.exception.MissingArgumentsException;
import be.ap.backend.repository.CampusBookRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CampusBookServiceTest {

    @Mock
    private CampusBookRepository campusBookRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private CampusBookService campusBookService;

    private Campus campus;
    private Book book;
    private CampusBook campusBook;

    @BeforeEach
    void setUp() {
        campus = new Campus();
        campus.setId(1L);

        Author author = new Author();
        author.setName("Tonke Dragt");

        book = new Book();
        book.setId(1L);
        book.setTitle("De brief voor de koning");
        book.setAuthor(author);
        book.setCover("https://example.com/cover.jpg");

        campusBook = new CampusBook();
        campusBook.setId(1L);
        campusBook.setCampus(campus);
        campusBook.setBook(book);
        campusBook.setAmount(3);
        campusBook.setCurrentAmount(3);
        campusBook.setLocation("Rek A");
    }

    // ── createCampusBook ──────────────────────────────────────────

    @Test
    void createCampusBook_success() {
        CampusBookDTO dto = new CampusBookDTO();
        dto.setCampusId(1L);
        dto.setBookId(1L);
        dto.setAmount(3);
        dto.setCurrentAmount(3);
        dto.setLocation("Rek A");

        when(campusBookRepository.existsByCampusIdAndBookId(1L, 1L)).thenReturn(false);
        when(entityManager.find(Campus.class, 1L)).thenReturn(campus);
        when(entityManager.find(Book.class, 1L)).thenReturn(book);
        when(campusBookRepository.save(any())).thenReturn(campusBook);

        CampusBook result = campusBookService.createCampusBook(dto);

        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(3);
        verify(campusBookRepository).save(any());
    }

    @Test
    void createCampusBook_missingCampusId_throwsMissingArgumentsException() {
        CampusBookDTO dto = new CampusBookDTO();
        dto.setBookId(1L);
        dto.setAmount(3);

        assertThatThrownBy(() -> campusBookService.createCampusBook(dto))
                .isInstanceOf(MissingArgumentsException.class)
                .hasMessage("Campus en boek zijn verplicht.");
    }

    @Test
    void createCampusBook_missingBookId_throwsMissingArgumentsException() {
        CampusBookDTO dto = new CampusBookDTO();
        dto.setCampusId(1L);
        dto.setAmount(3);

        assertThatThrownBy(() -> campusBookService.createCampusBook(dto))
                .isInstanceOf(MissingArgumentsException.class)
                .hasMessage("Campus en boek zijn verplicht.");
    }

    @Test
    void createCampusBook_nullAmount_throwsArgumentsInvalidException() {
        CampusBookDTO dto = new CampusBookDTO();
        dto.setCampusId(1L);
        dto.setBookId(1L);
        dto.setAmount(null);

        assertThatThrownBy(() -> campusBookService.createCampusBook(dto))
                .isInstanceOf(ArgumentsInvalidException.class)
                .hasMessage("Aantal moet minimaal 1 zijn.");
    }

    @Test
    void createCampusBook_zeroAmount_throwsArgumentsInvalidException() {
        CampusBookDTO dto = new CampusBookDTO();
        dto.setCampusId(1L);
        dto.setBookId(1L);
        dto.setAmount(0);

        assertThatThrownBy(() -> campusBookService.createCampusBook(dto))
                .isInstanceOf(ArgumentsInvalidException.class)
                .hasMessage("Aantal moet minimaal 1 zijn.");
    }

    @Test
    void createCampusBook_alreadyExists_throwsBookAlreadyInCampusException() {
        CampusBookDTO dto = new CampusBookDTO();
        dto.setCampusId(1L);
        dto.setBookId(1L);
        dto.setAmount(3);

        when(campusBookRepository.existsByCampusIdAndBookId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> campusBookService.createCampusBook(dto))
                .isInstanceOf(BookAlreadyInCampusException.class)
                .hasMessage("Dit boek is al toegevoegd aan deze campus.");
    }

    // ── findAll ───────────────────────────────────────────────────

    @Test
    void findAll_returnsListOfDTOs() {
        when(campusBookRepository.findAll()).thenReturn(List.of(campusBook));

        List<CampusBookDetailDTO> result = campusBookService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBookTitle()).isEqualTo("De brief voor de koning");
        assertThat(result.get(0).getAuthorName()).isEqualTo("Tonke Dragt");
    }

    @Test
    void findAll_empty_returnsEmptyList() {
        when(campusBookRepository.findAll()).thenReturn(List.of());

        List<CampusBookDetailDTO> result = campusBookService.findAll();

        assertThat(result).isEmpty();
    }

    // ── findByCampus ──────────────────────────────────────────────

    @Test
    void findByCampus_returnsPageOfDTOs() {
        var pageable = PageRequest.of(0, 5);
        var page = new PageImpl<>(List.of(campusBook), pageable, 1);

        when(campusBookRepository.findByCampusId(1L, pageable)).thenReturn(page);

        var result = campusBookService.findByCampus(1L, 0, 5);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBookTitle()).isEqualTo("De brief voor de koning");
    }

    @Test
    void findByCampus_empty_returnsEmptyPage() {
        var pageable = PageRequest.of(0, 5);
        var page = new PageImpl<CampusBook>(List.of(), pageable, 0);

        when(campusBookRepository.findByCampusId(1L, pageable)).thenReturn(page);

        var result = campusBookService.findByCampus(1L, 0, 5);

        assertThat(result.getContent()).isEmpty();
    }

    // ── getCampusBook ─────────────────────────────────────────────

    @Test
    void getCampusBook_found_returnsDTO() {
        when(campusBookRepository.findByCampusIdAndBookId(1L, 1L))
                .thenReturn(Optional.of(campusBook));

        CampusBookDetailDTO result = campusBookService.getCampusBook(1L, 1L);

        assertThat(result.getBookTitle()).isEqualTo("De brief voor de koning");
        assertThat(result.getCampusId()).isEqualTo(1L);
        assertThat(result.getBookId()).isEqualTo(1L);
    }

    @Test
    void getCampusBook_notFound_throwsEntityNotFoundException() {
        when(campusBookRepository.findByCampusIdAndBookId(1L, 99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> campusBookService.getCampusBook(1L, 99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("campusId=1")
                .hasMessageContaining("bookId=99");
    }

    // ── updateCurrentAmount ───────────────────────────────────────

    @Test
    void updateCurrentAmount_reducesCurrentAmount() {
        // geen when() nodig — we testen alleen de mutatie van currentAmount
        when(campusBookRepository.save(campusBook)).thenReturn(campusBook);

        campusBookService.updateCurrentAmount(campusBook, 2);

        assertThat(campusBook.getCurrentAmount()).isEqualTo(1);
        verify(campusBookRepository).save(campusBook);
    }

    @Test
    void updateCurrentAmount_returnsUpdatedDTO() {
        campusBook.setCurrentAmount(3);
        when(campusBookRepository.save(campusBook)).thenReturn(campusBook);

        CampusBookDetailDTO result = campusBookService.updateCurrentAmount(campusBook, 3);

        assertThat(result.getCurrentAmount()).isEqualTo(0);
    }
}