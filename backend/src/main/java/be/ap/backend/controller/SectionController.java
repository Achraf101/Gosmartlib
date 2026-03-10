package be.ap.backend.controller;

import be.ap.backend.entity.Book;
import be.ap.backend.entity.Section;
import be.ap.backend.service.SectionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("section")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping
    public List<Section> getAllSections() {
        return sectionService.getAllSections();
    }

    @GetMapping("/{id}/books")
    public List<Book> getBooksBySection(@PathVariable Long id) {
        return sectionService.getBooksBySection(id);
    }
}