package be.ap.backend.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import be.ap.backend.dto.CreateBookDTO;
import be.ap.backend.entity.Author;
import be.ap.backend.entity.Publisher;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.BookContributor;
import be.ap.backend.entity.BookType;
import be.ap.backend.entity.Genre;
import be.ap.backend.entity.Language;
import be.ap.backend.repository.BookRepository;
import jakarta.persistence.EntityManager;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    private final EntityManager entityManager;

    public BookService(BookRepository bookRepository, EntityManager entityManager) {
        this.bookRepository = bookRepository;
        this.entityManager = entityManager;
    }

    public Book saveBook(CreateBookDTO dto) {

        Book book = new Book();
        book.setId(null);

        // required fields
        book.setTitle(dto.getTitle());
        book.setBookType(entityManager.find(BookType.class,
                dto.getBookType()));
        book.setLanguage(entityManager.find(Language.class,
                dto.getLanguage()));
        book.setFiction(dto.getFiction());

        // optional fields
        if (dto.getAuthor() != null) {
            book.setAuthor(entityManager.find(Author.class, dto.getAuthor()));

        }

        if (dto.getPublisher() != null) {
            book.setPublisher(entityManager.find(Publisher.class, dto.getPublisher()));
        }

        if (dto.getGenres() != null) {
            Set<Genre> genres = dto.getGenres().stream()
                    .map(id -> entityManager.find(Genre.class, id))
                    .collect(Collectors.toSet());

            book.setGenres(genres);
        }

        if (dto.getContributors() != null) {
            Set<BookContributor> contributors = dto.getContributors().stream()
                    .map(id -> entityManager.find(BookContributor.class, id))
                    .collect(Collectors.toSet());

            book.setContributors(contributors);
        }

        if (dto.getFontSize() != null)
            book.setFontSize(dto.getFontSize());
        
        if (dto.getDescription() != null)
            book.setDescription(dto.getDescription());

        if (dto.getIsbn() != null)
            book.setIsbn(dto.getIsbn());

        if (dto.getPublished() != null)
            book.setPublished(dto.getPublished());

        if (dto.getPages() != 0)
            book.setPages(dto.getPages());

        // also check if ageStart is lower than ageEnd
        if (dto.getAgeStart() != 0 && dto.getAgeEnd() != 0
                && dto.getAgeStart() < dto.getAgeEnd()) {
            book.setAgeStart(dto.getAgeStart());
            book.setAgeEnd(dto.getAgeEnd());
        }

        return bookRepository.save(book);
    }
}
