package be.ap.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import be.ap.backend.dto.BookLookupDTO;
import be.ap.backend.dto.BulkPreviewDTO;
import be.ap.backend.dto.BulkUploadDTO;
import be.ap.backend.dto.IncompleteBookDTO;
import be.ap.backend.entity.Author;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.BookType;
import be.ap.backend.entity.Genre;
import be.ap.backend.entity.Language;
import be.ap.backend.entity.Publisher;
import be.ap.backend.entity.Theme;
import be.ap.backend.enums.Clib;
import be.ap.backend.enums.FontSize;
import be.ap.backend.repository.AuthorRepository;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.BookTypeRepository;
import be.ap.backend.repository.GenreRepository;
import be.ap.backend.repository.LanguageRepository;
import be.ap.backend.repository.PublisherRepository;
import be.ap.backend.repository.ThemeRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BookBulkUploadService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;
    private final BookTypeRepository bookTypeRepository;
    private final GenreRepository genreRepository;
    private final LanguageRepository languageRepository;
    private final ThemeRepository themeRepository;
    private final UploadService uploadService;
    private final IsbnLookupService isbnLookupService;

    public BookBulkUploadService(
            AuthorRepository authorRepository,
            BookRepository bookRepository,
            PublisherRepository publisherRepository,
            BookTypeRepository bookTypeRepository,
            GenreRepository genreRepository,
            LanguageRepository languageRepository,
            ThemeRepository themeRepository,
            UploadService uploadService,
            IsbnLookupService isbnLookupService) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.publisherRepository = publisherRepository;
        this.bookTypeRepository = bookTypeRepository;
        this.genreRepository = genreRepository;
        this.languageRepository = languageRepository;
        this.themeRepository = themeRepository;
        this.uploadService = uploadService;
        this.isbnLookupService = isbnLookupService;
    }

    public BulkPreviewDTO generatePreview(InputStream inputStream) {
        BulkPreviewDTO result = new BulkPreviewDTO();

        try (Workbook wb = WorkbookFactory.create(inputStream)) {
            Sheet sheet = getValidatedSheet(wb);

            Set<String> seen = new HashSet<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String isbn = getCellIsbn(row, 0);
                if (isbn.isBlank() || !seen.add(isbn)) continue;

                int rowNum = i + 1;
                Optional<BookLookupDTO> lookup = isbnLookupService.lookup(isbn);
                if (lookup.isPresent()) {
                    BookLookupDTO data = lookup.get();
                    result.addFound(rowNum, isbn, data.getTitle(), data.getAuthorName(), data.getCoverUrl());
                } else {
                    result.addNotFound(rowNum, isbn);
                }
            }
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Kon bestand niet lezen", e);
        }

        return result;
    }

    public BulkUploadDTO processUpload(InputStream inputStream) {
        Map<String, Author> authorMap = loadAuthors();
        Map<String, Publisher> publisherMap = loadPublishers();
        Map<String, BookType> bookTypeMap = loadBookTypes();
        Map<String, Genre> genreMap = loadGenres();
        Map<String, Language> languageMap = loadLanguages();
        Map<String, Language> languageByCode = loadLanguagesByCode();
        Map<String, Theme> themeMap = loadThemes();

        Set<String> existingIsbns = bookRepository.findAllIsbns();
        Set<String> seenIsbns = new HashSet<>();
        Map<String, BookLookupDTO> lookupCache = new HashMap<>();
        BulkUploadDTO result = new BulkUploadDTO();

        try (Workbook wb = WorkbookFactory.create(inputStream)) {
            Sheet sheet = getValidatedSheet(wb);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                processRow(row, i + 1, result, authorMap, publisherMap, bookTypeMap,
                        genreMap, languageMap, languageByCode, themeMap,
                        existingIsbns, seenIsbns, lookupCache);
            }
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Kon bestand niet lezen", e);
        }

        return result;
    }

    private void processRow(Row row, int rowNum, BulkUploadDTO result,
                           Map<String, Author> authorMap,
                           Map<String, Publisher> publisherMap,
                           Map<String, BookType> bookTypeMap,
                           Map<String, Genre> genreMap,
                           Map<String, Language> languageMap,
                           Map<String, Language> languageByCode,
                           Map<String, Theme> themeMap,
                           Set<String> existingIsbns,
                           Set<String> seenIsbns,
                           Map<String, BookLookupDTO> lookupCache) {

        String isbn = getCellIsbn(row, 0);
        String title = getCellString(row, 1);
        String authorName = getCellString(row, 2);
        String description = getCellString(row, 3);
        String didactic = getCellString(row, 4);
        String publisherName = getCellString(row, 5);
        String clib = getCellString(row, 6);
        String fiction = getCellString(row, 7);
        String bookTypeName = getCellString(row, 8);
        String genresRaw = getCellString(row, 9);
        String yearStr = getCellString(row, 10);
        String languageName = getCellString(row, 11);
        String pageStr = getCellString(row, 12);
        String themesRaw = getCellString(row, 13);
        String fontSize = getCellString(row, 14);
        String schoolOnly = getCellString(row, 15);
        String cover = getCellString(row, 16);

        if (!isbn.isBlank()) {
            if (existingIsbns.contains(isbn)) {
                result.addSkipped(rowNum, "ISBN bestaat al in de database: " + isbn);
                return;
            }
            if (seenIsbns.contains(isbn)) {
                result.addSkipped(rowNum, "ISBN komt meerdere keren voor in dit bestand: " + isbn);
                return;
            }
        }

        if (!isbn.isBlank() && !title.isBlank() && !authorName.isBlank()) {
            processIsbnLookup(rowNum, isbn, title, authorName, fiction, bookTypeName, genresRaw,
                    themesRaw, fontSize, didactic, schoolOnly, clib, result, authorMap,
                    publisherMap, bookTypeMap, languageByCode, seenIsbns, lookupCache);
            return;
        }

        processManualEntry(rowNum, isbn, title, authorName, description, didactic, publisherName,
                clib, fiction, bookTypeName, genresRaw, yearStr, languageName, pageStr, themesRaw,
                fontSize, schoolOnly, cover, result, authorMap, publisherMap, bookTypeMap,
                genreMap, languageMap, themeMap);
    }

    private void processIsbnLookup(int rowNum, String isbn, String title, String authorName,
                                   String fiction, String bookTypeName, String genresRaw,
                                   String themesRaw, String fontSize, String didactic,
                                   String schoolOnly, String clib, BulkUploadDTO result,
                                   Map<String, Author> authorMap, Map<String, Publisher> publisherMap,
                                   Map<String, BookType> bookTypeMap,
                                   Map<String, Language> languageByCode, Set<String> seenIsbns,
                                   Map<String, BookLookupDTO> lookupCache) {

        BookLookupDTO lookup = lookupCache.computeIfAbsent(isbn,
                key -> isbnLookupService.lookup(key).orElse(null));

        if (lookup == null) {
            result.addSkipped(rowNum, "ISBN niet gevonden in externe database: " + isbn);
            seenIsbns.add(isbn);
            return;
        }

        boolean titleMatch = lookup.getTitle() != null &&
                lookup.getTitle().equalsIgnoreCase(title.trim());
        boolean authorMatch = lookup.getAuthorName() != null &&
                lookup.getAuthorName().equalsIgnoreCase(authorName.trim());

        if (!titleMatch || !authorMatch) {
            result.addSkipped(rowNum, "ISBN gevonden maar titel/auteur komt niet overeen: " + isbn);
            seenIsbns.add(isbn);
            return;
        }

        Author author = getOrCreateAuthor(authorName, lookup.getAuthorName(), authorMap);

        Book book = new Book();
        book.setIsbn(isbn);
        book.setTitle(lookup.getTitle());
        book.setAuthor(author);
        book.setFiction(parseBoolean(fiction, true));

        populateBookFromLookup(book, lookup, bookTypeName, publisherMap, bookTypeMap, languageByCode);

        List<String> missingFields = validateBook(book);
        seenIsbns.add(isbn);

        if (!missingFields.isEmpty()) {
            result.addIncomplete(new IncompleteBookDTO(
                    rowNum, isbn, lookup.getTitle(), lookup.getAuthorName(),
                    lookup.getDescription(), lookup.getPublisherName(),
                    lookup.getPublishedYear(), lookup.getPages(), lookup.getCoverUrl(),
                    lookup.getLanguageCode(), null, bookTypeName.isBlank() ? null : bookTypeName,
                    genresRaw.isBlank() ? null : genresRaw, themesRaw.isBlank() ? null : themesRaw,
                    fontSize.isBlank() ? null : fontSize, fiction.isBlank() ? "JA" : fiction,
                    didactic.isBlank() ? "NEE" : didactic, schoolOnly.isBlank() ? "NEE" : schoolOnly,
                    clib.isBlank() ? null : clib, missingFields, new ArrayList<>()));
            return;
        }

        Book saved = bookRepository.save(book);
        result.addAddedBook(saved.getId(), saved.getTitle());
        result.incrementAdded();
    }

    private void processManualEntry(int rowNum, String isbn, String title, String authorName,
                                    String description, String didactic, String publisherName,
                                    String clib, String fiction, String bookTypeName,
                                    String genresRaw, String yearStr, String languageName,
                                    String pageStr, String themesRaw, String fontSize,
                                    String schoolOnly, String cover, BulkUploadDTO result,
                                    Map<String, Author> authorMap, Map<String, Publisher> publisherMap,
                                    Map<String, BookType> bookTypeMap, Map<String, Genre> genreMap,
                                    Map<String, Language> languageMap, Map<String, Theme> themeMap) {

        Author author = authorMap.get(authorName.toLowerCase());
        if (author == null) {
            result.addError(rowNum, "Onbekende auteur: " + authorName);
            return;
        }

        if (!validateBooleanField(didactic, "Didactisch materiaal", rowNum, result) ||
            !validateBooleanField(fiction, "Fictie", rowNum, result) ||
            !validateBooleanField(schoolOnly, "Enkel zichtbaar voor deze school", rowNum, result)) {
            return;
        }

        Integer year = parseYear(yearStr, rowNum, result);
        if (year == null && !yearStr.isBlank()) return;

        Integer pages = parsePages(pageStr, rowNum, result);
        if (pages == null && !pageStr.isBlank()) return;

        BookType bookType = bookTypeName.isBlank() ? null : bookTypeMap.get(bookTypeName.toLowerCase());
        Language language = languageName.isBlank() ? null : languageMap.get(languageName.toLowerCase());

        List<Genre> genres = new ArrayList<>();
        List<String> unknownGenres = resolveGenres(genresRaw, genreMap, genres);

        List<Theme> themes = new ArrayList<>();
        List<String> unknownThemes = resolveThemes(themesRaw, themeMap, themes);

        Publisher publisher = null;
        boolean publisherInvalid = false;
        if (!publisherName.isBlank()) {
            publisher = publisherMap.get(publisherName.toLowerCase());
            if (publisher == null) publisherInvalid = true;
        }

        boolean clibInvalid = !clib.isBlank() &&
                !List.of("A", "B", "C", "D").contains(clib.toUpperCase());
        boolean fontSizeInvalid = !fontSize.isBlank() &&
                !List.of("groot", "medium", "klein").contains(fontSize.toLowerCase());

        List<String> missingFields = new ArrayList<>();
        List<String> invalidFields = new ArrayList<>();

        if (description.isBlank()) missingFields.add("beschrijving");
        if (bookType == null) missingFields.add("boektype");
        if (genres.isEmpty()) missingFields.add("genres");
        if (language == null) missingFields.add("taal");
        if (!unknownGenres.isEmpty()) invalidFields.add("genres: onbekend: " + String.join(", ", unknownGenres));
        if (!unknownThemes.isEmpty()) invalidFields.add("themas: onbekend: " + String.join(", ", unknownThemes));
        if (publisherInvalid) invalidFields.add("uitgever: onbekend: " + publisherName);
        if (clibInvalid) invalidFields.add("clib: ongeldige waarde: " + clib);
        if (fontSizeInvalid) invalidFields.add("lettergrootte: ongeldige waarde: " + fontSize);

        if (!missingFields.isEmpty() || !invalidFields.isEmpty()) {
            result.addIncomplete(new IncompleteBookDTO(
                    rowNum, isbn.isBlank() ? null : isbn, title, authorName,
                    description.isBlank() ? null : description, publisherInvalid ? null : publisherName,
                    year, pages, cover.isBlank() ? null : cover, null,
                    languageName.isBlank() || language == null ? null : languageName,
                    bookTypeName.isBlank() || bookType == null ? null : bookTypeName,
                    genresRaw.isBlank() ? null : genresRaw, themesRaw.isBlank() ? null : themesRaw,
                    fontSizeInvalid ? null : fontSize, fiction.isBlank() ? "JA" : fiction,
                    didactic.isBlank() ? "NEE" : didactic, schoolOnly.isBlank() ? "NEE" : schoolOnly,
                    clibInvalid ? null : clib, missingFields, invalidFields));
            return;
        }

        Book book = createBook(title, author, description, fiction, bookType, genres, themes,
                language, year, pages, isbn, publisher, clib, fontSize, cover, rowNum, result);

        if (book != null) {
            Book saved = bookRepository.save(book);
            result.addAddedBook(saved.getId(), saved.getTitle());
            result.incrementAdded();
        }
    }

    private Book createBook(String title, Author author, String description, String fiction,
                           BookType bookType, List<Genre> genres, List<Theme> themes,
                           Language language, Integer year, Integer pages, String isbn,
                           Publisher publisher, String clib, String fontSize, String cover,
                           int rowNum, BulkUploadDTO result) {

        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setDescription(description);
        book.setFiction(parseBoolean(fiction, true));
        book.setBookType(bookType);
        book.setGenres(new HashSet<>(genres));
        book.setThemes(new HashSet<>(themes));
        book.setLanguage(language);
        if (year != null) book.setPublished(Year.of(year));
        if (pages != null) book.setPages(pages);
        if (!isbn.isBlank()) book.setIsbn(isbn);
        if (publisher != null) book.setPublisher(publisher);

        if (!clib.isBlank()) {
            try {
                book.setClib(Clib.valueOf(clib.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                result.addError(rowNum, "Ongeldige CLIB: " + clib);
                return null;
            }
        }

        if (!fontSize.isBlank()) {
            try {
                book.setFontSize(FontSize.valueOf(fontSize.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                result.addError(rowNum, "Ongeldige lettergrootte: " + fontSize);
                return null;
            }
        }

        if (!cover.isBlank()) {
            String saved = uploadService.saveCoverFromUrl(cover);
            if (saved != null) {
                book.setCover(saved);
            } else {
                log.warn("Row {}: cover URL kon niet worden gedownload: {}", rowNum, cover);
            }
        }

        return book;
    }

    private Map<String, Author> loadAuthors() {
        return authorRepository.findAll().stream()
                .collect(Collectors.toMap(a -> a.getName().toLowerCase(), a -> a, (a, b) -> a));
    }

    private Map<String, Publisher> loadPublishers() {
        return publisherRepository.findAll().stream()
                .collect(Collectors.toMap(p -> p.getName().toLowerCase(), p -> p, (a, b) -> a));
    }

    private Map<String, BookType> loadBookTypes() {
        return bookTypeRepository.findAll().stream()
                .collect(Collectors.toMap(b -> b.getName().toLowerCase(), b -> b, (a, b) -> a));
    }

    private Map<String, Genre> loadGenres() {
        return genreRepository.findAll().stream()
                .collect(Collectors.toMap(g -> g.getName().toLowerCase(), g -> g, (a, b) -> a));
    }

    private Map<String, Language> loadLanguages() {
        return languageRepository.findAll().stream()
                .collect(Collectors.toMap(l -> l.getName().toLowerCase(), l -> l, (a, b) -> a));
    }

    private Map<String, Language> loadLanguagesByCode() {
        return languageRepository.findAll().stream()
                .filter(l -> l.getCode() != null)
                .collect(Collectors.toMap(l -> l.getCode().toLowerCase(), l -> l, (a, b) -> a));
    }

    private Map<String, Theme> loadThemes() {
        return themeRepository.findAll().stream()
                .collect(Collectors.toMap(t -> t.getName().toLowerCase(), t -> t, (a, b) -> a));
    }

    private Author getOrCreateAuthor(String authorName, String lookupName, Map<String, Author> authorMap) {
        Author author = authorMap.get(authorName.toLowerCase());
        if (author == null) {
            Author newAuthor = new Author();
            newAuthor.setName(lookupName);
            author = authorRepository.save(newAuthor);
            authorMap.put(author.getName().toLowerCase(), author);
        }
        return author;
    }

    private void populateBookFromLookup(Book book, BookLookupDTO lookup, String bookTypeName,
                                       Map<String, Publisher> publisherMap,
                                       Map<String, BookType> bookTypeMap,
                                       Map<String, Language> languageByCode) {
        if (lookup.getDescription() != null) {
            book.setDescription(lookup.getDescription().length() > 1000
                    ? lookup.getDescription().substring(0, 1000)
                    : lookup.getDescription());
        }
        if (lookup.getPublishedYear() != null) {
            book.setPublished(Year.of(lookup.getPublishedYear()));
        }
        if (lookup.getPages() != null) {
            book.setPages(lookup.getPages());
        }
        if (lookup.getCoverUrl() != null) {
            String saved = uploadService.saveCoverFromUrl(lookup.getCoverUrl());
            if (saved != null) book.setCover(saved);
        }
        if (lookup.getPublisherName() != null) {
            Publisher p = publisherMap.get(lookup.getPublisherName().toLowerCase());
            if (p != null) book.setPublisher(p);
        }
        if (!bookTypeName.isBlank()) {
            BookType bt = bookTypeMap.get(bookTypeName.toLowerCase());
            if (bt != null) book.setBookType(bt);
        }
        if (lookup.getLanguageCode() != null) {
            Language lang = languageByCode.get(lookup.getLanguageCode().toLowerCase());
            if (lang != null) book.setLanguage(lang);
        }
    }

    private List<String> validateBook(Book book) {
        List<String> missingFields = new ArrayList<>();
        if (book.getBookType() == null) missingFields.add("boektype");
        if (book.getGenres() == null || book.getGenres().isEmpty()) missingFields.add("genres");
        if (book.getLanguage() == null) missingFields.add("taal");
        if (book.getDescription() == null || book.getDescription().isBlank()) missingFields.add("beschrijving");
        return missingFields;
    }

    private List<String> resolveGenres(String genresRaw, Map<String, Genre> genreMap, List<Genre> genres) {
        Set<String> genreNameSet = new LinkedHashSet<>();
        for (String token : genresRaw.split(",")) {
            if (!token.isBlank()) genreNameSet.add(token.trim().toLowerCase());
        }

        List<String> unknownGenres = new ArrayList<>();
        for (String name : genreNameSet) {
            Genre genre = genreMap.get(name);
            if (genre == null) unknownGenres.add(name);
            else genres.add(genre);
        }
        return unknownGenres;
    }

    private List<String> resolveThemes(String themesRaw, Map<String, Theme> themeMap, List<Theme> themes) {
        Set<String> themeNameSet = new LinkedHashSet<>();
        for (String token : themesRaw.split(",")) {
            if (!token.isBlank()) themeNameSet.add(token.trim().toLowerCase());
        }

        List<String> unknownThemes = new ArrayList<>();
        for (String name : themeNameSet) {
            Theme theme = themeMap.get(name);
            if (theme == null) unknownThemes.add(name);
            else themes.add(theme);
        }
        return unknownThemes;
    }

    private boolean validateBooleanField(String value, String fieldName, int rowNum, BulkUploadDTO result) {
        if (!value.isBlank() && !value.equalsIgnoreCase("JA") && !value.equalsIgnoreCase("NEE")) {
            result.addError(rowNum, fieldName + " moet JA of NEE zijn");
            return false;
        }
        return true;
    }

    private Integer parseYear(String yearStr, int rowNum, BulkUploadDTO result) {
        if (yearStr.isBlank()) return null;
        try {
            return Integer.parseInt(yearStr.replace(".0", "").trim());
        } catch (NumberFormatException e) {
            result.addError(rowNum, "Ongeldig jaar: " + yearStr);
            return null;
        }
    }

    private Integer parsePages(String pageStr, int rowNum, BulkUploadDTO result) {
        if (pageStr.isBlank()) return null;
        try {
            int pages = Integer.parseInt(pageStr.replace(".0", "").trim());
            if (pages < 1) {
                result.addError(rowNum, "Aantal pagina's moet minimaal 1 zijn");
                return null;
            }
            return pages;
        } catch (NumberFormatException e) {
            result.addError(rowNum, "Ongeldig aantal pagina's: " + pageStr);
            return null;
        }
    }

    private boolean parseBoolean(String value, boolean defaultValue) {
        if (value.isBlank()) return defaultValue;
        return value.equalsIgnoreCase("JA");
    }

    private Sheet getValidatedSheet(Workbook wb) {
        Sheet sheet = wb.getSheet("Books");
        if (sheet == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ongeldig bestand: geen 'Books' tabblad gevonden. Gebruik de template.");
        }
        return sheet;
    }

    private String getCellString(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) yield "";
                yield new DataFormatter().formatCellValue(cell).trim();
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> new DataFormatter().formatCellValue(cell).trim();
            default -> "";
        };
    }

    private String getCellIsbn(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case FORMULA -> new DataFormatter().formatCellValue(cell).trim();
            default -> "";
        };
    }
}