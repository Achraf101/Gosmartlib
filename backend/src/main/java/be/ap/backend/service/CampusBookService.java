package be.ap.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import be.ap.backend.dto.CampusBookDTO;
import be.ap.backend.entity.CampusBook;
import be.ap.backend.repository.CampusBookRepository;

@Service
public class CampusBookService {
    @Autowired
    private CampusBookRepository campusBookRepository;

    public CampusBook createCampusBook(CampusBookDTO dto) {
        CampusBook newCampusBook = new CampusBook();

        newCampusBook.setCampusId(dto.getCampusId());
        newCampusBook.setBookId(dto.getBookId());
        newCampusBook.setAmount(dto.getAmount());
        newCampusBook.setCurrentAmount(dto.getCurrentAmount());

        if (dto.getLocation() != null)
            newCampusBook.setLocation(dto.getLocation());

        return campusBookRepository.save(newCampusBook);
    }
}
