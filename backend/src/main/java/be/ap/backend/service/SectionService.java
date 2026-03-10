package be.ap.backend.service;

import be.ap.backend.entity.Book;
import be.ap.backend.entity.Section;
import be.ap.backend.repository.SectionBookRepository;
import be.ap.backend.repository.SectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;
    private final SectionBookRepository sectionBookRepository;

    public SectionService(SectionRepository sectionRepository, SectionBookRepository sectionBookRepository) {
        this.sectionRepository = sectionRepository;
        this.sectionBookRepository = sectionBookRepository;
    }

    public List<Section> getAllSections() {
         return sectionRepository.findByHiddenFalseOrderByRankingAsc();
    }

    public List<Book> getBooksBySection(Long sectionId) {
        return sectionBookRepository.findBySectionId(sectionId)
            .stream()
            .map(sb -> sb.getBook())
            .collect(Collectors.toList());
    }
}