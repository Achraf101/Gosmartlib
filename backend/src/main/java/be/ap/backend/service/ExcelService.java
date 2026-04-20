package be.ap.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import be.ap.backend.entity.Author;
import be.ap.backend.repository.AuthorRepository;

@Service
public class ExcelService {
    private final AuthorRepository authorRepository;

    public ExcelService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public List<Author> getAuthors() {
    return authorRepository.findAll();

    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Books");
}
}
