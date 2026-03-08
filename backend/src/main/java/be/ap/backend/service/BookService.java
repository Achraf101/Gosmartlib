package be.ap.backend.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import be.ap.backend.dto.CreateBookDTO;
import be.ap.backend.entity.Author;
import be.ap.backend.entity.Publisher;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.BookContributor;
import be.ap.backend.entity.BookType;
import be.ap.backend.entity.Genre;
import be.ap.backend.entity.Language;
import be.ap.backend.entity.Review;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.ReviewRepository;
import jakarta.persistence.EntityManager;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final EntityManager entityManager;
    private final ReviewRepository reviewRepository;

    public BookService(BookRepository bookRepository, EntityManager entityManager, ReviewRepository reviewRepository) {
        this.bookRepository = bookRepository;
        this.entityManager = entityManager;
        this.reviewRepository = reviewRepository;
    }

    public Book saveBook(CreateBookDTO dto) {

        Book book = new Book();

        // required fields
        book.setTitle(dto.getTitle());
        book.setBookType(entityManager.find(BookType.class, dto.getBookType()));
        book.setLanguage(entityManager.find(Language.class, dto.getLanguage()));
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

        if (dto.getAgeStart() != 0 && dto.getAgeEnd() != 0
                && dto.getAgeStart() < dto.getAgeEnd()) {
            book.setAgeStart(dto.getAgeStart());
            book.setAgeEnd(dto.getAgeEnd());
        }

        return bookRepository.save(book);
    }

    public Book getMonthlyBook() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Review> recentReviews = reviewRepository.findRecentReviews(thirtyDaysAgo);

        if (!recentReviews.isEmpty()) {
            return recentReviews.stream()
                .collect(Collectors.groupingBy(Review::getBookId,
                    Collectors.averagingDouble(r -> r.getRating())))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .flatMap(entry -> bookRepository.findById(entry.getKey()))
                .orElse(getFallbackBook());
        }
        return getFallbackBook();
    }

    private Book getFallbackBook() {
        return bookRepository.findAll().stream()
            .max(Comparator.comparingInt(Book::getRating))
            .orElse(null);
    }

    public List<Book> getFeaturedBooks() {
    return bookRepository.findAll()
        .stream()
        .limit(4)
        .collect(Collectors.toList());
}
}