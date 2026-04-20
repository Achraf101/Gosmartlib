package be.ap.backend.controller;

import be.ap.backend.entity.Book;
import be.ap.backend.entity.Section;
import be.ap.backend.service.SectionService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("section")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @GetMapping
    public List<Section> getAllSections() {
        return sectionService.getAllSections();
    }

    @GetMapping("/{id}/books")
    public List<Book> getBooksBySection(@PathVariable Long id) {
        return sectionService.getBooksBySection(id);
    }

    @GetMapping("/{id}/books/grade")
    public Book getBookBySectionAndGrade(
            @PathVariable Long id,
            @RequestParam Byte grade) {
        return sectionService.getBookBySectionAndGrade(id, grade);
    }

    @PutMapping("/{id}/book")
    public Book setBookOfMonth(
            @PathVariable Long id,
            @RequestParam Long bookId,
            @RequestParam Byte grade) {
        return sectionService.setBookOfMonth(id, bookId, grade);
    }
}