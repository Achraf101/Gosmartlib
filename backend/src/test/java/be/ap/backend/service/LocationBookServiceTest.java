package be.ap.backend.service;

import be.ap.backend.dto.LocationBookDTO;
import be.ap.backend.dto.LocationBookDetailDTO;
import be.ap.backend.entity.Author;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Location;
import be.ap.backend.entity.LocationBook;
import be.ap.backend.exception.ArgumentsInvalidException;
import be.ap.backend.exception.MissingArgumentsException;
import be.ap.backend.repository.LocationBookRepository;
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
public class LocationBookServiceTest {

    @Mock
    private LocationBookRepository locationBookRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private LocationBookService locationBookService;

    private Location location;
    private Book book;
    private LocationBook locationBook;

    @BeforeEach
    void setUp() {
        location = new Location();
        location.setId(1L);

        Author author = new Author();
        author.setName("Tonke Dragt");

        book = new Book();
        book.setId(1L);
        book.setTitle("De brief voor de koning");
        book.setAuthor(author);
        book.setCover("https://example.com/cover.jpg");

        locationBook = new LocationBook();
        locationBook.setId(1L);
        locationBook.setLocation(location);
        locationBook.setBook(book);
        locationBook.setAmount(3);
        locationBook.setCurrentAmount(3);
    }

    // ── createLocationBook ──────────────────────────────────────────

    @Test
    void createLocationBook_success() {
        LocationBookDTO dto = new LocationBookDTO();
        dto.setLocationId(1L);
        dto.setBookId(1L);
        dto.setAmount(3);
        dto.setCurrentAmount(3);

        when(locationBookRepository.existsByLocationIdAndBookId(1L, 1L)).thenReturn(false);
        when(entityManager.find(Location.class, 1L)).thenReturn(location);
        when(entityManager.find(Book.class, 1L)).thenReturn(book);
        when(locationBookRepository.save(any())).thenReturn(locationBook);

        LocationBook result = locationBookService.createLocationBook(dto);

        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(3);
        verify(locationBookRepository).save(any());
    }

    @Test
    void createLocationBook_missingLocationId_throwsMissingArgumentsException() {
        LocationBookDTO dto = new LocationBookDTO();
        dto.setBookId(1L);
        dto.setAmount(3);

        assertThatThrownBy(() -> locationBookService.createLocationBook(dto))
                .isInstanceOf(MissingArgumentsException.class)
                .hasMessage("Locatie en boek zijn verplicht.");
    }

    @Test
    void createLocationBook_missingBookId_throwsMissingArgumentsException() {
        LocationBookDTO dto = new LocationBookDTO();
        dto.setLocationId(1L);
        dto.setAmount(3);

        assertThatThrownBy(() -> locationBookService.createLocationBook(dto))
                .isInstanceOf(MissingArgumentsException.class)
                .hasMessage("Locatie en boek zijn verplicht.");
    }

    @Test
    void createLocationBook_nullAmount_throwsArgumentsInvalidException() {
        LocationBookDTO dto = new LocationBookDTO();
        dto.setLocationId(1L);
        dto.setBookId(1L);
        dto.setAmount(null);

        assertThatThrownBy(() -> locationBookService.createLocationBook(dto))
                .isInstanceOf(ArgumentsInvalidException.class)
                .hasMessage("Aantal moet minimaal 1 zijn.");
    }

    @Test
    void createLocationBook_zeroAmount_throwsArgumentsInvalidException() {
        LocationBookDTO dto = new LocationBookDTO();
        dto.setLocationId(1L);
        dto.setBookId(1L);
        dto.setAmount(0);

        assertThatThrownBy(() -> locationBookService.createLocationBook(dto))
                .isInstanceOf(ArgumentsInvalidException.class)
                .hasMessage("Aantal moet minimaal 1 zijn.");
    }

    @Test
    void createLocationBook_alreadyExists_throwsBookAlreadyInLocationException() {
        LocationBookDTO dto = new LocationBookDTO();
        dto.setLocationId(1L);
        dto.setBookId(1L);
        dto.setAmount(2);

        when(locationBookRepository.existsByLocationIdAndBookId(1L, 1L)).thenReturn(true);

        when(locationBookRepository.existsByLocationIdAndBookId(1L, 1L)).thenReturn(true);
        when(locationBookRepository.findByLocationIdAndBookId(1L, 1L)).thenReturn(Optional.of(locationBook));
        when(locationBookRepository.save(locationBook)).thenReturn(locationBook);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> locationBookService.createLocationBook(dto));
    }

    // ── findAll ───────────────────────────────────────────────────

    @Test
    void findAll_returnsListOfDTOs() {
        when(locationBookRepository.findAll()).thenReturn(List.of(locationBook));

        List<LocationBookDetailDTO> result = locationBookService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBookTitle()).isEqualTo("De brief voor de koning");
        assertThat(result.get(0).getAuthorName()).isEqualTo("Tonke Dragt");
    }

    @Test
    void findAll_empty_returnsEmptyList() {
        when(locationBookRepository.findAll()).thenReturn(List.of());

        List<LocationBookDetailDTO> result = locationBookService.findAll();

        assertThat(result).isEmpty();
    }

    // ── findByLocation ──────────────────────────────────────────────

    @Test
    void findByLocation_returnsPageOfDTOs() {
        var pageable = PageRequest.of(0, 5);
        var page = new PageImpl<>(List.of(locationBook), pageable, 1);

        when(locationBookRepository.findByLocationId(1L, pageable)).thenReturn(page);

        var result = locationBookService.findByLocation(1L, 0, 5);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBookTitle()).isEqualTo("De brief voor de koning");
    }

    @Test
    void findByLocation_empty_returnsEmptyPage() {
        var pageable = PageRequest.of(0, 5);
        var page = new PageImpl<LocationBook>(List.of(), pageable, 0);

        when(locationBookRepository.findByLocationId(1L, pageable)).thenReturn(page);

        var result = locationBookService.findByLocation(1L, 0, 5);

        assertThat(result.getContent()).isEmpty();
    }

    // ── getLocationBook ─────────────────────────────────────────────

    @Test
    void getLocationBook_found_returnsDTO() {
        when(locationBookRepository.findByLocationIdAndBookId(1L, 1L))
                .thenReturn(Optional.of(locationBook));

        LocationBookDetailDTO result = locationBookService.getLocationBook(1L, 1L);

        assertThat(result.getBookTitle()).isEqualTo("De brief voor de koning");
        assertThat(result.getLocationId()).isEqualTo(1L);
        assertThat(result.getBookId()).isEqualTo(1L);
    }

    @Test
    void getLocationBook_notFound_throwsEntityNotFoundException() {
        when(locationBookRepository.findByLocationIdAndBookId(1L, 99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationBookService.getLocationBook(1L, 99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("locationId=1")
                .hasMessageContaining("bookId=99");
    }

    // ── updateCurrentAmount ───────────────────────────────────────

    @Test
    void updateCurrentAmount_reducesCurrentAmount() {
        // geen when() nodig — we testen alleen de mutatie van currentAmount
        when(locationBookRepository.save(locationBook)).thenReturn(locationBook);

        locationBookService.updateCurrentAmount(locationBook, 2);

        assertThat(locationBook.getCurrentAmount()).isEqualTo(1);
        verify(locationBookRepository).save(locationBook);
    }

    @Test
    void updateCurrentAmount_returnsUpdatedDTO() {
        locationBook.setCurrentAmount(3);
        when(locationBookRepository.save(locationBook)).thenReturn(locationBook);

        LocationBookDetailDTO result = locationBookService.updateCurrentAmount(locationBook, 3);

        assertThat(result.getCurrentAmount()).isEqualTo(0);
    }

    @Test
    void createLocationBook_alreadyExists_increasesAmountAndCurrentAmount() {
        LocationBookDTO dto = new LocationBookDTO();
        dto.setLocationId(1L);
        dto.setBookId(1L);
        dto.setAmount(2);

        when(locationBookRepository.existsByLocationIdAndBookId(1L, 1L)).thenReturn(true);
        when(locationBookRepository.findByLocationIdAndBookId(1L, 1L)).thenReturn(Optional.of(locationBook));
        when(locationBookRepository.save(locationBook)).thenReturn(locationBook);

        locationBookService.createLocationBook(dto);

        assertThat(locationBook.getAmount()).isEqualTo(5); // 3 + 2
        assertThat(locationBook.getCurrentAmount()).isEqualTo(5); // 3 + 2
        verify(locationBookRepository).save(locationBook);
    }

    @Test
    void createLocationBook_alreadyExists_savesUpdatedLocationBook() {
        LocationBookDTO dto = new LocationBookDTO();
        dto.setLocationId(1L);
        dto.setBookId(1L);
        dto.setAmount(4);

        when(locationBookRepository.existsByLocationIdAndBookId(1L, 1L)).thenReturn(true);
        when(locationBookRepository.findByLocationIdAndBookId(1L, 1L)).thenReturn(Optional.of(locationBook));
        when(locationBookRepository.save(locationBook)).thenReturn(locationBook);

        LocationBook result = locationBookService.createLocationBook(dto);

        assertThat(result).isNotNull();
        assertThat(locationBook.getAmount()).isEqualTo(7); // 3 + 4
        verify(locationBookRepository, times(1)).save(locationBook);
    }

}