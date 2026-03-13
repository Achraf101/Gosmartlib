package be.ap.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.dto.CampusBookDTO;
import be.ap.backend.entity.CampusBook;
import be.ap.backend.service.CampusBookService;

@RestController
@RequestMapping("campusbook")
public class CampusBookController {
    private final CampusBookService campusBookService;

    public CampusBookController(CampusBookService campusBookService) {
        this.campusBookService = campusBookService;
    }

    @PostMapping
    public CampusBook createCampusBook(@RequestBody CampusBookDTO dto) {
        CampusBook savedCampusBook = campusBookService.createCampusBook(dto);
        return savedCampusBook;
    }
}
