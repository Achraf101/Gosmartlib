package be.ap.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import be.ap.backend.dto.CampusBookDTO;
import be.ap.backend.dto.CampusBookDetailDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.Campus;
import be.ap.backend.entity.CampusBook;
import be.ap.backend.exception.ArgumentsInvalidException;
import be.ap.backend.exception.BookAlreadyInCampusException;
import be.ap.backend.exception.MissingArgumentsException;
import be.ap.backend.repository.CampusBookRepository;
import jakarta.persistence.EntityManager;

@Service
public class CampusBookService {
    @Autowired
    private CampusBookRepository campusBookRepository;

    @Autowired
    EntityManager entityManager;

    public CampusBook createCampusBook(CampusBookDTO dto) {
        if (dto.getCampusId() == null || dto.getBookId() == null) {
            throw new MissingArgumentsException("Campus en boek zijn verplicht.");
        }
        if (dto.getAmount() == null || dto.getAmount() < 1) {
            throw new ArgumentsInvalidException("Aantal moet minimaal 1 zijn.");
        }
        if (campusBookRepository.existsByCampusIdAndBookId(dto.getCampusId(), dto.getBookId())) {
            throw new BookAlreadyInCampusException("Dit boek is al toegevoegd aan deze campus.");
        }

        CampusBook newCampusBook = new CampusBook();

        newCampusBook.setCampus(entityManager.find(Campus.class, dto.getCampusId()));
        newCampusBook.setBook(entityManager.find(Book.class, dto.getBookId()));
        newCampusBook.setAmount(dto.getAmount());
        newCampusBook.setCurrentAmount(dto.getCurrentAmount());

        if (dto.getLocation() != null)
            newCampusBook.setLocation(dto.getLocation());

        return campusBookRepository.save(newCampusBook);
    }

    public List<CampusBookDetailDTO> findAll() {
        return campusBookRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public Page<CampusBookDetailDTO> findByCampus(Long campusId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return campusBookRepository.findByCampusId(campusId, pageable)
                .map(this::toDTO);
    }

    private CampusBookDetailDTO toDTO(CampusBook campusBook) {
        CampusBookDetailDTO dto = new CampusBookDetailDTO();
        dto.setId(campusBook.getId());
        dto.setCampusId(campusBook.getCampus().getId());
        dto.setBookId(campusBook.getBook().getId());
        dto.setBookTitle(campusBook.getBook().getTitle());
        dto.setAuthorName(campusBook.getBook().getAuthor() != null ? campusBook.getBook().getAuthor().getName() : null);
        dto.setBookCover(campusBook.getBook().getCover());
        dto.setAmount(campusBook.getAmount());
        dto.setCurrentAmount(campusBook.getCurrentAmount());
        dto.setLocation(campusBook.getLocation());
        return dto;
    }
}
