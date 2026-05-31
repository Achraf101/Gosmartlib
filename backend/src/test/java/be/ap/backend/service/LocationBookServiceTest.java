package be.ap.backend.service;

import be.ap.backend.dto.LocationAvailabilityDTO;
import be.ap.backend.dto.LocationBookDTO;
import be.ap.backend.dto.LocationBookDetailDTO;
import be.ap.backend.dto.SchoolStatsDTO;
import be.ap.backend.entity.Author;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.BookCopy;
import be.ap.backend.entity.Location;
import be.ap.backend.entity.LocationBook;
import be.ap.backend.enums.CopyStatus;
import be.ap.backend.exception.ArgumentsInvalidException;
import be.ap.backend.exception.MissingArgumentsException;
import be.ap.backend.repository.BookCopyRepository;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.LocationBookRepository;
import be.ap.backend.repository.LocationRepository;
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
    private BookCopyService bookCopyService;
    @Mock
    private BookCopyRepository bookCopyRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private LocationBookService locationBookService;

    private Location location;
    private Book book;
    private LocationBook locationBook;

    @BeforeEach
    void setUp() {
        location = new Location();
        location.setId(1L);
        location.setName("Bibliotheek A");

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

        when(locationBookRepository.existsByLocationIdAndBookId(1L, 1L)).thenReturn(false);
        when(entityManager.find(Location.class, 1L)).thenReturn(location);
        when(entityManager.find(Book.class, 1L)).thenReturn(book);
        when(locationBookRepository.save(any())).thenReturn(locationBook);
        when(bookCopyService.createCopies(any(), eq(3))).thenReturn(List.of("ACC-001", "ACC-002", "ACC-003"));

        LocationBookDetailDTO result = locationBookService.createLocationBook(dto);

        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(3);
        assertThat(result.getNewAccessionIds()).containsExactly("ACC-001", "ACC-002", "ACC-003");
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
    void createLocationBook_alreadyExists_increasesAmountAndCurrentAmount() {
        LocationBookDTO dto = new LocationBookDTO();
        dto.setLocationId(1L);
        dto.setBookId(1L);
        dto.setAmount(2);

        when(locationBookRepository.existsByLocationIdAndBookId(1L, 1L)).thenReturn(true);
        when(locationBookRepository.findByLocationIdAndBookId(1L, 1L)).thenReturn(Optional.of(locationBook));
        when(locationBookRepository.save(locationBook)).thenReturn(locationBook);
        when(bookCopyService.createCopies(any(), eq(2))).thenReturn(List.of("ACC-004", "ACC-005"));

        locationBookService.createLocationBook(dto);

        assertThat(locationBook.getAmount()).isEqualTo(5);
        assertThat(locationBook.getCurrentAmount()).isEqualTo(5);
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
        when(bookCopyService.createCopies(any(), eq(4))).thenReturn(List.of());

        LocationBookDetailDTO result = locationBookService.createLocationBook(dto);

        assertThat(result).isNotNull();
        assertThat(locationBook.getAmount()).isEqualTo(7);
        verify(locationBookRepository, times(1)).save(locationBook);
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

    @Test
    void findAll_bookWithoutAuthor_mapsAuthorNameAsNull() {
        book.setAuthor(null);
        when(locationBookRepository.findAll()).thenReturn(List.of(locationBook));

        List<LocationBookDetailDTO> result = locationBookService.findAll();

        assertThat(result.get(0).getAuthorName()).isNull();
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
        // bookRepository and locationRepository are not called in the happy path
        when(locationBookRepository.findByLocationIdAndBookId(1L, 1L))
                .thenReturn(Optional.of(locationBook));

        LocationBookDetailDTO result = locationBookService.getLocationBook(1L, 1L);

        assertThat(result.getBookTitle()).isEqualTo("De brief voor de koning");
        assertThat(result.getLocationId()).isEqualTo(1L);
        assertThat(result.getBookId()).isEqualTo(1L);
    }

    @Test
    void getLocationBook_notFound_throwsEntityNotFoundException() {
        // Both lookups fall back to "id=X" when not found in their respective repos
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());
        when(locationBookRepository.findByLocationIdAndBookId(1L, 99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationBookService.getLocationBook(1L, 99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("id=99")
                .hasMessageContaining("id=1");
    }

    @Test
    void getLocationBook_notFound_messageContainsBookTitleAndLocationName() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(locationBookRepository.findByLocationIdAndBookId(1L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationBookService.getLocationBook(1L, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("De brief voor de koning")
                .hasMessageContaining("Bibliotheek A");
    }

    // ── updateCurrentAmount ───────────────────────────────────────

    @Test
    void updateCurrentAmount_reducesCurrentAmount() {
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
    void updateCurrentAmount_fullAmount_currentAmountBecomesZero() {
        locationBook.setCurrentAmount(5);
        when(locationBookRepository.save(locationBook)).thenReturn(locationBook);

        locationBookService.updateCurrentAmount(locationBook, 5);

        assertThat(locationBook.getCurrentAmount()).isEqualTo(0);
    }

    // ── getAvailabilityByBook ─────────────────────────────────────

    @Test
    void getAvailabilityByBook_returnsDTOWithCounts() {
        BookCopy damagedCopy = new BookCopy();
        damagedCopy.setStatus(CopyStatus.DAMAGED);
        damagedCopy.setNote(null);

        BookCopy notedCopy = new BookCopy();
        notedCopy.setStatus(CopyStatus.AVAILABLE);
        notedCopy.setNote("Omslag beschadigd");

        BookCopy normalCopy = new BookCopy();
        normalCopy.setStatus(CopyStatus.AVAILABLE);
        normalCopy.setNote(null);

        when(locationBookRepository.findByBookIdAndLocationSchoolId(1L, 10L))
                .thenReturn(List.of(locationBook));
        when(bookCopyRepository.findByLocationBookId(1L))
                .thenReturn(List.of(damagedCopy, notedCopy, normalCopy));

        List<LocationAvailabilityDTO> result = locationBookService.getAvailabilityByBook(1L, 10L);

        assertThat(result).hasSize(1);
        LocationAvailabilityDTO dto = result.get(0);
        assertThat(dto.getLocationId()).isEqualTo(1L);
        assertThat(dto.getLocationName()).isEqualTo("Bibliotheek A");
        assertThat(dto.getAmount()).isEqualTo(3);
        assertThat(dto.getCurrentAmount()).isEqualTo(3);
        assertThat(dto.getDamagedCount()).isEqualTo(1);
        assertThat(dto.getNotedCount()).isEqualTo(1);
    }

    @Test
    void getAvailabilityByBook_noCopies_countsAreZero() {
        when(locationBookRepository.findByBookIdAndLocationSchoolId(1L, 10L))
                .thenReturn(List.of(locationBook));
        when(bookCopyRepository.findByLocationBookId(1L)).thenReturn(List.of());

        List<LocationAvailabilityDTO> result = locationBookService.getAvailabilityByBook(1L, 10L);

        assertThat(result.get(0).getDamagedCount()).isEqualTo(0);
        assertThat(result.get(0).getNotedCount()).isEqualTo(0);
    }

    @Test
    void getAvailabilityByBook_noLocations_returnsEmptyList() {
        when(locationBookRepository.findByBookIdAndLocationSchoolId(1L, 10L))
                .thenReturn(List.of());

        List<LocationAvailabilityDTO> result = locationBookService.getAvailabilityByBook(1L, 10L);

        assertThat(result).isEmpty();
    }

    // ── getStatsForSchool ─────────────────────────────────────────

    @Test
    void getStatsForSchool_returnsCorrectTotals() {
        when(locationBookRepository.sumAmountBySchoolId(10L)).thenReturn(50);
        when(locationBookRepository.sumCurrentAmountBySchoolId(10L)).thenReturn(30);

        SchoolStatsDTO result = locationBookService.getStatsForSchool(10L);

        assertThat(result.totalBooks()).isEqualTo(50);
        assertThat(result.availableBooks()).isEqualTo(30);
    }

    @Test
    void getStatsForSchool_nullRepositoryResults_defaultToZero() {
        when(locationBookRepository.sumAmountBySchoolId(10L)).thenReturn(null);
        when(locationBookRepository.sumCurrentAmountBySchoolId(10L)).thenReturn(null);

        SchoolStatsDTO result = locationBookService.getStatsForSchool(10L);

        assertThat(result.totalBooks()).isEqualTo(0);
        assertThat(result.availableBooks()).isEqualTo(0);
    }
}