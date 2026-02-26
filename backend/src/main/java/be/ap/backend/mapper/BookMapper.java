package be.ap.backend.mapper;

import be.ap.backend.entity.Book;
import be.ap.backend.model.BookDTO;
import be.ap.backend.repository.AuthorRepository;
import be.ap.backend.repository.GenreRepository;
import be.ap.backend.repository.LanguageRepository;
import be.ap.backend.repository.PublisherRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookMapper {

    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final PublisherRepository publisherRepository;
    private final LanguageRepository languageRepository;

    public BookMapper(AuthorRepository authorRepository,
                      GenreRepository genreRepository,
                      PublisherRepository publisherRepository,
                      LanguageRepository languageRepository) {
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.publisherRepository = publisherRepository;
        this.languageRepository = languageRepository;
    }

    public BookDTO toDTO(Book book, List<Long> authorIds, List<Long> genreIds) {
        BookDTO dto = new BookDTO();

        dto.setId(book.getId());
        dto.setIsbn(book.getIsbn());
        dto.setTitle(book.getTitle());
        dto.setDescription(book.getDescription());
        dto.setFiction(book.getFiction());
        dto.setPublished(book.getPublished());
        dto.setCover(book.getCover());
        dto.setAgeStart(book.getAgeStart());
        dto.setAgeEnd(book.getAgeEnd());
        dto.setPages(book.getPages());
        dto.setRating(book.getRating());
        dto.setRatingCount(book.getRatingCount());

        // Publisher naam opzoeken via ID
        if (book.getPublisherId() != 0) {
            publisherRepository.findById(book.getPublisherId()).ifPresent(p ->
                dto.setPublisherName(p.getName())
            );
        }

        // Taal opzoeken via ID
        if (book.getLanguageId() != 0) {
            languageRepository.findById(book.getLanguageId()).ifPresent(l -> {
                dto.setLanguageName(l.getName());
                dto.setLanguageCode(l.getCode());
            });
        }

        // Auteurs opzoeken via meegegeven IDs (uit book_author tabel)
        if (authorIds != null && !authorIds.isEmpty()) {
            List<String> authors = authorIds.stream()
                .map(id -> authorRepository.findById(id))
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get().getName())
                .collect(Collectors.toList());
            dto.setAuthors(authors);
        } else {
            dto.setAuthors(Collections.emptyList());
        }

        // Genres opzoeken via meegegeven IDs (uit book_genre tabel)
        if (genreIds != null && !genreIds.isEmpty()) {
            List<String> genres = genreIds.stream()
                .map(id -> genreRepository.findById(id))
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get().getName())
                .collect(Collectors.toList());
            dto.setGenres(genres);
        } else {
            dto.setGenres(Collections.emptyList());
        }

        return dto;
    }
}
