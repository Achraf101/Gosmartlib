package be.ap.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import be.ap.backend.dto.LocationBookDTO;
import be.ap.backend.dto.LocationBookDetailDTO;
import be.ap.backend.dto.LocationStatsDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Location;
import be.ap.backend.entity.LocationBook;
import be.ap.backend.exception.ArgumentsInvalidException;
import be.ap.backend.exception.BookAlreadyInLocationException;
import be.ap.backend.exception.MissingArgumentsException;
import be.ap.backend.repository.LocationBookRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocationBookService {

    private final LocationBookRepository locationBookRepository;

    private final EntityManager entityManager;

    public LocationBook createLocationBook(LocationBookDTO dto) {
        if (dto.getLocationId() == null || dto.getBookId() == null) {
            throw new MissingArgumentsException("Locatie en boek zijn verplicht.");
        }
        if (dto.getAmount() == null || dto.getAmount() < 1) {
            throw new ArgumentsInvalidException("Aantal moet minimaal 1 zijn.");
        }
        if (locationBookRepository.existsByLocationIdAndBookId(dto.getLocationId(), dto.getBookId())) {
            throw new BookAlreadyInLocationException("Dit boek is al toegevoegd aan deze locatie.");
        }

        LocationBook newLocationBook = new LocationBook();

        newLocationBook.setLocation(entityManager.find(Location.class, dto.getLocationId()));
        newLocationBook.setBook(entityManager.find(Book.class, dto.getBookId()));
        newLocationBook.setAmount(dto.getAmount());
        newLocationBook.setCurrentAmount(dto.getCurrentAmount());

        if (dto.getNote() != null)
            newLocationBook.setNote(dto.getNote());

        return locationBookRepository.save(newLocationBook);
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
        LocationBook locationBook = locationBookRepository
                .findByLocationIdAndBookId(locationId, bookId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "LocationBook niet gevonden voor locationId=" + locationId + ", bookId=" + bookId));
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
        dto.setNote(locationBook.getNote());
        return dto;
    }

    public LocationStatsDTO getStatsForLocation(Long locationId) {
        int total = locationBookRepository.sumAmountByLocationId(locationId);
        int available = locationBookRepository.sumCurrentAmountByLocationId(locationId);
        return new LocationStatsDTO(total, available);
    }
}
