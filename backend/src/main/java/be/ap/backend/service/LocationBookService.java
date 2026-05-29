package be.ap.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import be.ap.backend.dto.LocationBookDTO;
import be.ap.backend.dto.LocationBookDetailDTO;
import be.ap.backend.dto.SchoolStatsDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Location;
import be.ap.backend.entity.LocationBook;
import be.ap.backend.exception.ArgumentsInvalidException;
import be.ap.backend.exception.MissingArgumentsException;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.LocationBookRepository;
import be.ap.backend.repository.LocationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocationBookService {

    private final LocationBookRepository locationBookRepository;

    private final EntityManager entityManager;

    private final LocationRepository locationRepository;
    private final BookRepository bookRepository;

    public LocationBookDetailDTO createLocationBook(LocationBookDTO dto) {
        if (dto.getLocationId() == null || dto.getBookId() == null) {
            throw new MissingArgumentsException("Locatie en boek zijn verplicht.");
        }
        if (dto.getAmount() == null || dto.getAmount() < 1) {
            throw new ArgumentsInvalidException("Aantal moet minimaal 1 zijn.");
        }
        if (locationBookRepository.existsByLocationIdAndBookId(dto.getLocationId(), dto.getBookId())) {
            LocationBook existing = locationBookRepository
                    .findByLocationIdAndBookId(dto.getLocationId(), dto.getBookId())
                    .orElseThrow();
            existing.setAmount(existing.getAmount() + dto.getAmount());
            existing.setCurrentAmount(existing.getCurrentAmount() + dto.getAmount());
            return toDTO(locationBookRepository.save(existing));
        }

        LocationBook newLocationBook = new LocationBook();

        newLocationBook.setLocation(entityManager.find(Location.class, dto.getLocationId()));
        newLocationBook.setBook(entityManager.find(Book.class, dto.getBookId()));
        newLocationBook.setAmount(dto.getAmount());
        newLocationBook.setCurrentAmount(dto.getAmount());

        return toDTO(locationBookRepository.save(newLocationBook));
    }

    public List<LocationBookDetailDTO> findAll() {
        return locationBookRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public Page<LocationBookDetailDTO> findByLocation(Long locationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return locationBookRepository.findByLocationId(locationId, pageable)
                .map(this::toDTO);
    }

    public LocationBookDetailDTO getLocationBook(Long locationId, Long bookId) {
        String bookTitle = bookRepository.findById(bookId)
                .map(Book::getTitle)
                .orElse("id=" + bookId);

        String locationName = locationRepository.findById(locationId)
                .map(Location::getName)
                .orElse("id=" + locationId);

        LocationBook locationBook = locationBookRepository
                .findByLocationIdAndBookId(locationId, bookId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Het boek '" + bookTitle + "' is niet aanwezig in '" + locationName + "'."));
        return toDTO(locationBook);
    }

    public LocationBookDetailDTO updateCurrentAmount(LocationBook locationBook, int requestedAmount) {
        locationBook.setCurrentAmount(locationBook.getCurrentAmount() - requestedAmount);
        return toDTO(locationBookRepository.save(locationBook));
    }

    private LocationBookDetailDTO toDTO(LocationBook locationBook) {
        LocationBookDetailDTO dto = new LocationBookDetailDTO();
        dto.setId(locationBook.getId());
        dto.setLocationId(locationBook.getLocation().getId());
        dto.setBookId(locationBook.getBook().getId());
        dto.setBookTitle(locationBook.getBook().getTitle());
        dto.setAuthorName(
                locationBook.getBook().getAuthor() != null ? locationBook.getBook().getAuthor().getName() : null);
        dto.setBookCover(locationBook.getBook().getCover());
        dto.setAmount(locationBook.getAmount());
        dto.setCurrentAmount(locationBook.getCurrentAmount());
        return dto;
    }

    public SchoolStatsDTO getStatsForSchool(Long schoolId) {
        int total = locationBookRepository.sumAmountBySchoolId(schoolId);
        int available = locationBookRepository.sumCurrentAmountBySchoolId(schoolId);
        return new SchoolStatsDTO(total, available);
    }
}
