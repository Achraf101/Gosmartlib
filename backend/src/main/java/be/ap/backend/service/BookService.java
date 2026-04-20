package be.ap.backend.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import be.ap.backend.dto.GenreProjection;
import be.ap.backend.dto.BookResultDTO;
import be.ap.backend.dto.CreateBookDTO;
import be.ap.backend.dto.GenreDTO;
import be.ap.backend.entity.Author;
import be.ap.backend.entity.Publisher;
import be.ap.backend.entity.Series;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.BookContributor;
import be.ap.backend.entity.BookType;
import be.ap.backend.entity.Clib;
import be.ap.backend.entity.Genre;
import be.ap.backend.entity.Language;
import be.ap.backend.repository.BookRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final EntityManager entityManager;

    public Book saveBook(CreateBookDTO dto) {
        Book book = new Book();

        // required fields
        book.setTitle(dto.getTitle());
        book.setBookType(entityManager.find(BookType.class, dto.getBookType()));
        book.setLanguage(entityManager.find(Language.class, dto.getLanguage()));
        book.setFiction(dto.getFiction());

        if (dto.getAuthor() != null) {
            book.setAuthor(entityManager.find(Author.class, dto.getAuthor()));
        }

        if (dto.getPublisher() != null) {
            book.setPublisher(entityManager.find(Publisher.class, dto.getPublisher()));
        }

        if (dto.getSeries() != null) {
            book.setSeries(entityManager.find(Series.class, dto.getSeries()));
        }

        if (dto.getSeriesCount() != 0) {
            book.setSeriesNumber(dto.getSeriesCount());
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

        if (dto.getClib() != null) {
            book.setClib(dto.getClib());
        }

        return bookRepository.save(book);
    }

    public Page<Book> filter(
            List<Long> genres,
            Long language,
            Boolean fiction,
            List<Long> authorIds,
            List<Long> seriesIds,
            Integer pagesMin,
            Integer pagesMax,
            List<Clib> clibs,
            Pageable pageable) {

        // 🔥 BELANGRIJK
        if (seriesIds != null && seriesIds.isEmpty()) {
            seriesIds = null;
        }

        return bookRepository.filter(
                genres,
                language,
                fiction,
                authorIds,
                seriesIds,
                pagesMin,
                pagesMax,
                clibs,
                pageable);
    }

    public Page<BookResultDTO> getAllBookResults(Pageable pageable) {
        Page<BookResultDTO> page = bookRepository.getAllBookResults(pageable);

        List<Long> bookIds = page.getContent().stream()
                .map(BookResultDTO::getId)
                .toList();

        if (bookIds.isEmpty()) {
            return page;
        }

        List<GenreProjection> results = bookRepository.findGenresForBooks(bookIds);

        Map<Long, Set<GenreDTO>> genreMap = new HashMap<>();
        for (GenreProjection row : results) {
            genreMap.computeIfAbsent(row.getBookId(), k -> new HashSet<>())
                    .add(new GenreDTO(row.getGenreId(), row.getGenreName()));
        }

        page.getContent().forEach(dto -> dto.setGenres(genreMap.getOrDefault(dto.getId(), Set.of())));

        return page;
    }
}