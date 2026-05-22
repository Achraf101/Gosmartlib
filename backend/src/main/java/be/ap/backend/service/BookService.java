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
import be.ap.backend.dto.ThemeDTO;
import be.ap.backend.dto.ThemeProjectionDTO;
import be.ap.backend.dto.UpdateBookDTO;
import be.ap.backend.dto.BookCardDTO;
import be.ap.backend.dto.BookResultDTO;
import be.ap.backend.dto.CreateBookDTO;
import be.ap.backend.dto.GenreDTO;
import be.ap.backend.entity.Author;
import be.ap.backend.entity.Publisher;
import be.ap.backend.entity.Series;
import be.ap.backend.entity.Theme;
import be.ap.backend.exception.ArgumentsInvalidException;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.BookContributor;
import be.ap.backend.entity.BookType;
import be.ap.backend.entity.Clib;
import be.ap.backend.entity.Genre;
import be.ap.backend.entity.Language;
import be.ap.backend.repository.BookRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final EntityManager entityManager;
    private final UploadService uploadService;

    public Book saveBook(CreateBookDTO dto) {
        Book book = new Book();

        book.setTitle(dto.getTitle());
        book.setBookType(entityManager.find(BookType.class, dto.getBookType()));
        book.setLanguage(entityManager.find(Language.class, dto.getLanguage()));
        book.setFiction(dto.getFiction());
        book.setDidactic(dto.getDidactic());

        if (dto.getAuthor() != null) {
            book.setAuthor(entityManager.find(Author.class, dto.getAuthor()));
        }

        if (dto.getPublisher() != null) {
            book.setPublisher(entityManager.find(Publisher.class, dto.getPublisher()));
        }

        if (dto.getSeries() != null) {
            book.setSeries(entityManager.find(Series.class, dto.getSeries()));
        }

        if (dto.getSeriesCount() != null && dto.getSeriesCount() != 0) {
            book.setSeriesNumber(dto.getSeriesCount());
        }

        if (dto.getGenres() != null) {
            Set<Genre> genres = dto.getGenres().stream()
                    .map(id -> entityManager.find(Genre.class, id))
                    .collect(Collectors.toSet());
            book.setGenres(genres);
        }

        if (dto.getThemes() != null) {
            Set<Theme> themes = dto.getThemes().stream()
                    .map(id -> entityManager.find(Theme.class, id))
                    .collect(Collectors.toSet());
            book.setThemes(themes);
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

        if (dto.getCoverUrl() != null && !dto.getCoverUrl().isBlank()) {
            String savedCover = uploadService.saveCoverFromUrl(dto.getCoverUrl());
            if (savedCover != null) {
                book.setCover(savedCover);
            }
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
            List<Long> themes,
            Boolean didactic,
            Pageable pageable) {

        if (seriesIds != null && seriesIds.isEmpty()) {
            seriesIds = null;
        }

        if (pagesMin != null && pagesMax != null && pagesMin > pagesMax) {
            throw new ArgumentsInvalidException("pagesMin moet kleiner zijn dan pagesMax");
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
                themes,
                didactic,
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

        List<ThemeProjectionDTO> themes = bookRepository.findThemesForBooks(bookIds);

        Map<Long, Set<ThemeDTO>> themeMap = new HashMap<>();
        for (ThemeProjectionDTO row : themes) {
            themeMap.computeIfAbsent(row.getBookId(), k -> new HashSet<>())
                    .add(new ThemeDTO(row.getThemeId(), row.getThemeName()));
        }

        page.getContent().forEach(dto -> dto.setThemes(themeMap.getOrDefault(dto.getId(), Set.of())));

        return page;
    }

    public Book updateBook(Long id, UpdateBookDTO dto) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Boek niet gevonden met id: " + id));

        if (dto.getTitle() != null)
            book.setTitle(dto.getTitle());
        if (dto.getIsbn() != null)
            book.setIsbn(dto.getIsbn());
        if (dto.getDescription() != null)
            book.setDescription(dto.getDescription());
        if (dto.getFiction() != null)
            book.setFiction(dto.getFiction());
        if (dto.getDidactic() != null)
            book.setDidactic(dto.getDidactic());
        if (dto.getPages() != null)
            book.setPages(dto.getPages());
        if (dto.getPublished() != null)
            book.setPublished(dto.getPublished());
        if (dto.getCover() != null)
            book.setCover(dto.getCover());
        if (dto.getFontSize() != null)
            book.setFontSize(dto.getFontSize());
        if (dto.getClib() != null)
            book.setClib(dto.getClib());
        if (dto.getSeriesNumber() != null)
            book.setSeriesNumber(dto.getSeriesNumber());
        if (dto.getAuthor() != null)
            book.setAuthor(entityManager.find(Author.class, dto.getAuthor()));
        if (dto.getPublisher() != null)
            book.setPublisher(entityManager.find(Publisher.class, dto.getPublisher()));
        if (dto.getLanguage() != null)
            book.setLanguage(entityManager.find(Language.class, dto.getLanguage()));
        if (dto.getBookType() != null)
            book.setBookType(entityManager.find(BookType.class, dto.getBookType()));
        if (dto.getSeries() != null)
            book.setSeries(entityManager.find(Series.class, dto.getSeries()));
        if (dto.getGenres() != null) {
            Set<Genre> genres = dto.getGenres().stream()
                    .map(gid -> entityManager.find(Genre.class, gid))
                    .collect(Collectors.toSet());
            book.setGenres(genres);
        }
        if (dto.getThemes() != null) {
            Set<Theme> themes = dto.getThemes().stream()
                    .map(tid -> entityManager.find(Theme.class, tid))
                    .collect(Collectors.toSet());
            book.setThemes(themes);
        }

        return bookRepository.save(book);
    }

    public Page<Book> getAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    public Book getById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Boek niet gevonden met id: " + id));
    }

    public Page<Book> search(String query, Pageable pageable) {
        return bookRepository.search(query, pageable);
    }

    public List<BookCardDTO> getRelated(Long id) {
        return bookRepository.findRelated(id);
    }
}