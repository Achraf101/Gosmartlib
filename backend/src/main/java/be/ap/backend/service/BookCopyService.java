package be.ap.backend.service;

import be.ap.backend.dto.BookCopyDetailDTO;
import be.ap.backend.entity.BookCopy;
import be.ap.backend.entity.LocationBook;
import be.ap.backend.enums.CopyStatus;
import be.ap.backend.repository.BookCopyRepository;
import be.ap.backend.repository.LocationBookRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookCopyService {

    private final BookCopyRepository bookCopyRepository;
    private final LocationBookRepository locationBookRepository;

    public List<String> createCopies(LocationBook locationBook, int count) {
        int nextSeq = bookCopyRepository.findMaxSequenceNumber() + 1;
        List<BookCopy> copies = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            BookCopy copy = new BookCopy();
            copy.setAccessionId(String.format("LIB-%06d", nextSeq + i));
            copy.setLocationBook(locationBook);
            copy.setStatus(CopyStatus.AVAILABLE);
            copies.add(copy);
        }
        bookCopyRepository.saveAll(copies);
        return copies.stream().map(BookCopy::getAccessionId).toList();
    }

    public BookCopyDetailDTO findByAccessionId(String accessionId) {
        BookCopy copy = bookCopyRepository.findByAccessionId(accessionId)
                .orElseThrow(() -> new EntityNotFoundException("Exemplaar niet gevonden: " + accessionId));
        return toDTO(copy);
    }

    public List<BookCopyDetailDTO> findByLocationBookId(Long locationBookId) {
        return bookCopyRepository.findByLocationBookId(locationBookId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public BookCopyDetailDTO updateStatus(Long id, CopyStatus status) {
        BookCopy copy = bookCopyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exemplaar niet gevonden: " + id));
        copy.setStatus(status);
        return toDTO(bookCopyRepository.save(copy));
    }

    public void deleteCopy(String accessionId) {
        BookCopy copy = bookCopyRepository.findByAccessionId(accessionId)
                .orElseThrow(() -> new EntityNotFoundException("Exemplaar niet gevonden: " + accessionId));
        LocationBook lb = copy.getLocationBook();
        lb.setAmount(lb.getAmount() - 1);
        lb.setCurrentAmount(Math.max(0, lb.getCurrentAmount() - 1));
        locationBookRepository.save(lb);
        bookCopyRepository.delete(copy);
    }

    private BookCopyDetailDTO toDTO(BookCopy copy) {
        LocationBook lb = copy.getLocationBook();
        BookCopyDetailDTO dto = new BookCopyDetailDTO();
        dto.setId(copy.getId());
        dto.setAccessionId(copy.getAccessionId());
        dto.setStatus(copy.getStatus());
        dto.setLocationBookId(lb.getId());
        dto.setBookId(lb.getBook().getId());
        dto.setBookTitle(lb.getBook().getTitle());
        dto.setAuthorName(lb.getBook().getAuthor() != null ? lb.getBook().getAuthor().getName() : null);
        dto.setBookCover(lb.getBook().getCover());
        dto.setIsbn(lb.getBook().getIsbn());
        dto.setLocationId(lb.getLocation().getId());
        dto.setLocationName(lb.getLocation().getName());
        dto.setAmount(lb.getAmount());
        dto.setCurrentAmount(lb.getCurrentAmount());
        dto.setNote(copy.getNote());
        return dto;
    }
}
