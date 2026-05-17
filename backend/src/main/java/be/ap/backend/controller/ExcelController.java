package be.ap.backend.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import be.ap.backend.dto.BookLookupDTO;
import be.ap.backend.dto.BulkPreviewDTO;
import be.ap.backend.dto.BulkUploadDTO;
import be.ap.backend.dto.IncompleteBookDTO;
import be.ap.backend.entity.Author;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.BookType;
import be.ap.backend.entity.Clib;
import be.ap.backend.entity.FontSize;
import be.ap.backend.entity.Genre;
import be.ap.backend.entity.Language;
import be.ap.backend.entity.Publisher;
import be.ap.backend.entity.Theme;
import be.ap.backend.repository.AuthorRepository;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.BookTypeRepository;
import be.ap.backend.repository.GenreRepository;
import be.ap.backend.repository.LanguageRepository;
import be.ap.backend.repository.PublisherRepository;
import be.ap.backend.repository.ThemeRepository;
import be.ap.backend.service.IsbnLookupService;
import be.ap.backend.service.UploadService;

@RestController
@RequestMapping("/excel/book")
public class ExcelController {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;
    private final BookTypeRepository bookTypeRepository;
    private final GenreRepository genreRepository;
    private final LanguageRepository languageRepository;
    private final UploadService uploadService;
    private final ThemeRepository themeRepository;
    private final IsbnLookupService isbnLookupService;

    public ExcelController(AuthorRepository authorRepository,
            BookRepository bookRepository,
            PublisherRepository publisherRepository,
            BookTypeRepository bookTypeRepository,
            GenreRepository genreRepository,
            LanguageRepository languageRepository,
            UploadService uploadService,
            IsbnLookupService isbnLookupService,
            ThemeRepository themeRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.publisherRepository = publisherRepository;
        this.bookTypeRepository = bookTypeRepository;
        this.genreRepository = genreRepository;
        this.languageRepository = languageRepository;
        this.uploadService = uploadService;
        this.themeRepository = themeRepository;
        this.isbnLookupService = isbnLookupService;
    }

    @GetMapping("template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] xlsx = buildTemplateXlsx();
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=book-upload-template.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(xlsx);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Could not generate template", e);
        }
    }

    private byte[] buildTemplateXlsx() throws IOException {

        String sheetXml = buildSheetXml();

        String workbookXml = """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
                        xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                <sheets>
                    <sheet name="Books" sheetId="1" r:id="rId1"/>
                </sheets>
                </workbook>
                """;

        String contentTypes = """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                <Default Extension="xml" ContentType="application/xml"/>
                <Override PartName="/xl/workbook.xml"
                    ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                <Override PartName="/xl/worksheets/sheet1.xml"
                    ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                </Types>
                """;

        String rootRels = """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                <Relationship Id="rId1"
                    Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
                    Target="xl/workbook.xml"/>
                </Relationships>
                """;

        String workbookRels = """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                <Relationship Id="rId1"
                    Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet"
                    Target="worksheets/sheet1.xml"/>
                </Relationships>
                """;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            writeZipEntry(zos, "[Content_Types].xml", contentTypes);
            writeZipEntry(zos, "_rels/.rels", rootRels);
            writeZipEntry(zos, "xl/workbook.xml", workbookXml);
            writeZipEntry(zos, "xl/_rels/workbook.xml.rels", workbookRels);
            writeZipEntry(zos, "xl/worksheets/sheet1.xml", sheetXml);
        }
        return baos.toByteArray();    }

    private String buildSheetXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                <sheetViews>
                    <sheetView workbookViewId="0" tabSelected="1">
                    <pane ySplit="1" topLeftCell="A2" activePane="bottomLeft" state="frozen"/>
                    </sheetView>
                </sheetViews>
                <sheetData>
                    <row r="1">
                    <c r="A1" t="inlineStr"><is><t>ISBN (optioneel)</t></is></c>
                    <c r="B1" t="inlineStr"><is><t>Titel *</t></is></c>
                    <c r="C1" t="inlineStr"><is><t>Auteur *</t></is></c>
                    <c r="D1" t="inlineStr"><is><t>Beschrijving * (max 1000 tekens)</t></is></c>
                    <c r="E1" t="inlineStr"><is><t>Didactisch materiaal (JA/NEE)</t></is></c>
                    <c r="F1" t="inlineStr"><is><t>Uitgever (optioneel)</t></is></c>
                    <c r="G1" t="inlineStr"><is><t>CLIB (optioneel, A/B/C/D)</t></is></c>
                    <c r="H1" t="inlineStr"><is><t>Fictie (JA/NEE)</t></is></c>
                    <c r="I1" t="inlineStr"><is><t>Boektype *</t></is></c>
                    <c r="J1" t="inlineStr"><is><t>Genres * (gescheiden door spaties)</t></is></c>
                    <c r="K1" t="inlineStr"><is><t>Jaar van uitgave (optioneel)</t></is></c>
                    <c r="L1" t="inlineStr"><is><t>Taal *</t></is></c>
                    <c r="M1" t="inlineStr"><is><t>Aantal pagina's *</t></is></c>
                    <c r="N1" t="inlineStr"><is><t>Thema's (optioneel, gescheiden door spaties)</t></is></c>
                    <c r="O1" t="inlineStr"><is><t>Lettergrootte (optioneel: groot/medium/klein)</t></is></c>
                    <c r="P1" t="inlineStr"><is><t>Enkel zichtbaar voor deze school (JA/NEE)</t></is></c>
                    <c r="Q1" t="inlineStr"><is><t>Cover (optioneel, URL)</t></is></c>
                    </row>
                </sheetData>
                </worksheet>
                """;
    }

    private void writeZipEntry(ZipOutputStream zos, String name, String content) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    @PostMapping("bulk-preview")
    public ResponseEntity<BulkPreviewDTO> bulkPreview(@RequestParam("file") MultipartFile file) {
        BulkPreviewDTO result = new BulkPreviewDTO();

        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheet("Books");

            if (sheet == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ongeldig bestand: geen 'Books' tabblad gevonden. Gebruik de template.");
            }

            Set<String> seen = new HashSet<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                String isbn = getCellIsbn(row, 0);
                if (isbn.isBlank() || !seen.add(isbn))
                    continue;

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

        return ResponseEntity.ok(result);
    }

    @PostMapping("bulk-upload")
    public ResponseEntity<BulkUploadDTO> bulkUpload(@RequestParam("file") MultipartFile file) {
        Map<String, Author> authorMap = authorRepository.findAll().stream()
                .collect(Collectors.toMap(a -> a.getName().toLowerCase(), a -> a, (a, b) -> a));
        Map<String, Publisher> publisherMap = publisherRepository.findAll().stream()
                .collect(Collectors.toMap(p -> p.getName().toLowerCase(), p -> p, (a, b) -> a));
        Map<String, BookType> bookTypeMap = bookTypeRepository.findAll().stream()
                .collect(Collectors.toMap(b -> b.getName().toLowerCase(), b -> b, (a, b) -> a));
        Map<String, Genre> genreMap = genreRepository.findAll().stream()
                .collect(Collectors.toMap(g -> g.getName().toLowerCase(), g -> g, (a, b) -> a));
        Map<String, Language> languageMap = languageRepository.findAll().stream()
                .collect(Collectors.toMap(l -> l.getName().toLowerCase(), l -> l, (a, b) -> a));
        Map<String, Language> languageByCode = languageRepository.findAll().stream()
                .filter(l -> l.getCode() != null)
                .collect(Collectors.toMap(l -> l.getCode().toLowerCase(), l -> l, (a, b) -> a));
        Map<String, Theme> themeMap = themeRepository.findAll().stream()
                .collect(Collectors.toMap(t -> t.getName().toLowerCase(), t -> t, (a, b) -> a));

        Set<String> existingIsbns = bookRepository.findAllIsbns();
        Set<String> seenIsbns = new HashSet<>();
        Map<String, BookLookupDTO> lookupCache = new HashMap<>();
        BulkUploadDTO result = new BulkUploadDTO();

        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheet("Books");

            if (sheet == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ongeldig bestand: geen 'Books' tabblad gevonden. Gebruik de template.");
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                String isbn         = getCellIsbn(row, 0);
                String title        = getCellString(row, 1);
                String authorName   = getCellString(row, 2);
                String description  = getCellString(row, 3);
                String didactic     = getCellString(row, 4);
                String publisherName= getCellString(row, 5);
                String clib         = getCellString(row, 6);
                String fiction      = getCellString(row, 7);
                String bookTypeName = getCellString(row, 8);
                String genresRaw    = getCellString(row, 9);
                String yearStr      = getCellString(row, 10);
                String languageName = getCellString(row, 11);
                String pageStr      = getCellString(row, 12);
                String themesRaw    = getCellString(row, 13);
                String fontSize     = getCellString(row, 14);
                String schoolOnly   = getCellString(row, 15);
                String cover        = getCellString(row, 16);

                int rowNum = i + 1;

                if (!isbn.isBlank()) {
                    if (existingIsbns.contains(isbn)) {
                        result.addSkipped(rowNum, "ISBN bestaat al in de database: " + isbn);
                        continue;
                    }
                    if (seenIsbns.contains(isbn)) {
                        result.addSkipped(rowNum, "ISBN komt meerdere keren voor in dit bestand: " + isbn);
                        continue;
                    }
                    seenIsbns.add(isbn);
                }

                if (!isbn.isBlank() && !title.isBlank() && !authorName.isBlank()) {
                    BookLookupDTO lookup = lookupCache.computeIfAbsent(isbn,
                            key -> isbnLookupService.lookup(key).orElse(null));

                    if (lookup != null) {
                        boolean titleMatch  = lookup.getTitle() != null &&
                                lookup.getTitle().equalsIgnoreCase(title.trim());
                        boolean authorMatch = lookup.getAuthorName() != null &&
                                lookup.getAuthorName().equalsIgnoreCase(authorName.trim());

                        if (titleMatch && authorMatch) {
                            Author author = authorMap.get(authorName.toLowerCase());
                            if (author == null) {
                                Author newAuthor = new Author();
                                newAuthor.setName(lookup.getAuthorName());
                                author = authorRepository.save(newAuthor);
                                authorMap.put(author.getName().toLowerCase(), author);
                            }

                            Book book = new Book();
                            book.setIsbn(isbn);
                            book.setTitle(lookup.getTitle());
                            book.setAuthor(author);
                            book.setFiction(parseBoolean(fiction, true));

                            if (lookup.getDescription() != null)
                                book.setDescription(lookup.getDescription().length() > 1000
                                    ? lookup.getDescription().substring(0, 1000)
                                    : lookup.getDescription());
                            if (lookup.getPublishedYear() != null)
                                book.setPublished(Year.of(lookup.getPublishedYear()));
                            if (lookup.getPages() != null)
                                book.setPages(lookup.getPages());
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

                            List<String> missingFields = new ArrayList<>();
                            if (book.getBookType() == null)                           missingFields.add("boektype");
                            if (book.getGenres() == null || book.getGenres().isEmpty()) missingFields.add("genres");
                            if (book.getLanguage() == null)                           missingFields.add("taal");
                            if (book.getDescription() == null || book.getDescription().isBlank()) missingFields.add("beschrijving");

                            if (!missingFields.isEmpty()) {
                                result.addIncomplete(new IncompleteBookDTO(
                                    rowNum,
                                    isbn,
                                    lookup.getTitle(),
                                    lookup.getAuthorName(),
                                    lookup.getDescription(),
                                    lookup.getPublisherName(),
                                    lookup.getPublishedYear(),
                                    lookup.getPages(),
                                    lookup.getCoverUrl(),
                                    lookup.getLanguageCode(),
                                    missingFields
                                ));
                                continue;
                            }

                            bookRepository.save(book);
                            result.incrementAdded();
                            continue;
                        } else {
                            result.addSkipped(rowNum, "ISBN gevonden maar titel/auteur komt niet overeen: " + isbn);
                            continue;
                        }
                    }
                    else {
                        result.addSkipped(rowNum, "ISBN niet gevonden in externe database: " + isbn);
                        continue;
                    }
                }

                if (!isbn.isBlank()) {
                    BookLookupDTO lookup = lookupCache.computeIfAbsent(isbn,
                            key -> isbnLookupService.lookup(key).orElse(null));

                    if (lookup != null) {
                        if (title.isBlank() && lookup.getTitle() != null)
                            title = lookup.getTitle();
                        if (authorName.isBlank() && lookup.getAuthorName() != null)
                            authorName = lookup.getAuthorName();
                        if (description.isBlank() && lookup.getDescription() != null)
                            description = lookup.getDescription();
                        if (publisherName.isBlank() && lookup.getPublisherName() != null)
                            publisherName = lookup.getPublisherName();
                        if (yearStr.isBlank() && lookup.getPublishedYear() != null)
                            yearStr = String.valueOf(lookup.getPublishedYear());
                        if (pageStr.isBlank() && lookup.getPages() != null)
                            pageStr = String.valueOf(lookup.getPages());
                        if (cover.isBlank() && lookup.getCoverUrl() != null)
                            cover = lookup.getCoverUrl();
                        if (languageName.isBlank() && lookup.getLanguageCode() != null) {
                            Language byCode = languageByCode.get(lookup.getLanguageCode().toLowerCase());
                            if (byCode != null)
                                languageName = byCode.getName();
                        }

                        List<String> missingFields = new ArrayList<>();
        
                        if (bookTypeName.isBlank()) missingFields.add("boektype");
                        if (genresRaw.isBlank()) missingFields.add("genres");
                        if (languageName.isBlank() && lookup.getLanguageCode() != null) missingFields.add("taal");
                        if (description.isBlank()) missingFields.add("beschrijving");
                        
                        if (!missingFields.isEmpty() && !title.isBlank() && !authorName.isBlank()) {
                            result.addIncomplete(new IncompleteBookDTO(
                                rowNum,
                                isbn,
                                title,
                                authorName,
                                description.isBlank() ? null : description,
                                publisherName.isBlank() ? null : publisherName,
                                lookup.getPublishedYear(),
                                lookup.getPages(),
                                lookup.getCoverUrl(),
                                lookup.getLanguageCode(),
                                missingFields
                            ));
                            continue;
                        }
                    }
                }

                if (title.isBlank()) {
                    result.addError(rowNum, "Titel is verplicht");
                    continue;
                }
                if (authorName.isBlank()) {
                    result.addError(rowNum, "Auteur is verplicht");
                    continue;
                }
                if (description.isBlank()) {
                    result.addError(rowNum, "Beschrijving is verplicht");
                    continue;
                }
                if (description.length() > 1000) {
                    description = description.substring(0, 1000);
                }
                if (bookTypeName.isBlank()) {
                    result.addError(rowNum, "Boektype is verplicht");
                    continue;
                }
                if (genresRaw.isBlank()) {
                    result.addError(rowNum, "Minstens 1 genre is verplicht");
                    continue;
                }
                if (languageName.isBlank()) {
                    result.addError(rowNum, "Taal is verplicht");
                    continue;
                }

                Author author = authorMap.get(authorName.toLowerCase());
                if (author == null) {
                    result.addError(rowNum, "Onbekende auteur: " + authorName);
                    continue;
                }

                if (!didactic.isBlank() && !didactic.equalsIgnoreCase("JA")
                        && !didactic.equalsIgnoreCase("NEE")) {
                    result.addError(rowNum, "Didactisch materiaal moet JA of NEE zijn");
                    continue;
                }
                if (!fiction.isBlank() && !fiction.equalsIgnoreCase("JA")
                        && !fiction.equalsIgnoreCase("NEE")) {
                    result.addError(rowNum, "Fictie moet JA of NEE zijn");
                    continue;
                }
                if (!schoolOnly.isBlank() && !schoolOnly.equalsIgnoreCase("JA")
                        && !schoolOnly.equalsIgnoreCase("NEE")) {
                    result.addError(rowNum, "Enkel zichtbaar voor deze school moet JA of NEE zijn");
                    continue;
                }

                BookType bookType = bookTypeMap.get(bookTypeName.toLowerCase());
                if (bookType == null) {
                    result.addError(rowNum, "Onbekend boektype: " + bookTypeName);
                    continue;
                }

                Language language = languageMap.get(languageName.toLowerCase());
                if (language == null) {
                    result.addError(rowNum, "Onbekende taal: " + languageName);
                    continue;
                }

                Set<String> genreNames = new LinkedHashSet<>();
                for (String token : genresRaw.split("\\s+")) {
                    if (!token.isBlank()) genreNames.add(token.toLowerCase());
                }
                List<Genre> genres = new ArrayList<>();
                boolean genreError = false;
                for (String name : genreNames) {
                    Genre genre = genreMap.get(name);
                    if (genre == null) {
                        result.addError(rowNum, "Onbekend genre: " + name);
                        genreError = true;
                        break;
                    }
                    genres.add(genre);
                }
                if (genreError) continue;

                Set<String> themeNames = new LinkedHashSet<>();
                for (String token : themesRaw.split("\\s+")) {
                    if (!token.isBlank()) themeNames.add(token.toLowerCase());
                }
                List<Theme> themes = new ArrayList<>();
                boolean themeError = false;
                for (String name : themeNames) {
                    Theme theme = themeMap.get(name);
                    if (theme == null) {
                        result.addError(rowNum, "Onbekend thema: " + name);
                        themeError = true;
                        break;
                    }
                    themes.add(theme);
                }
                if (themeError) continue;

                Publisher publisher = null;
                if (!publisherName.isBlank()) {
                    publisher = publisherMap.get(publisherName.toLowerCase());
                    if (publisher == null) {
                        result.addError(rowNum, "Onbekende uitgever: " + publisherName);
                        continue;
                    }
                }

                if (!clib.isBlank() && !List.of("A", "B", "C", "D").contains(clib.toUpperCase())) {
                    result.addError(rowNum, "Ongeldig CLIB niveau: " + clib + " (A, B, C of D)");
                    continue;
                }
                if (!fontSize.isBlank() &&
                        !List.of("groot", "medium", "klein").contains(fontSize.toLowerCase())) {
                    result.addError(rowNum, "Ongeldige lettergrootte: " + fontSize);
                    continue;
                }

                Integer year = null;
                if (!yearStr.isBlank()) {
                    try {
                        year = Integer.parseInt(yearStr.replace(".0", "").trim());
                    } catch (NumberFormatException e) {
                        result.addError(rowNum, "Ongeldig jaar: " + yearStr);
                        continue;
                    }
                }

                Integer pages = null;
                if (!pageStr.isBlank()) {
                    try {
                        pages = Integer.parseInt(pageStr.replace(".0", "").trim());
                        if (pages < 1) {
                            result.addError(rowNum, "Aantal pagina's moet minimaal 1 zijn");
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        result.addError(rowNum, "Ongeldig aantal pagina's: " + pageStr);
                        continue;
                    }
                }

                Book book = new Book();
                book.setTitle(title);
                book.setAuthor(author);
                book.setDescription(description);
                book.setFiction(parseBoolean(fiction, true));
                book.setBookType(bookType);
                book.setGenres(new HashSet<>(genres));
                book.setThemes(new HashSet<>(themes));
                book.setLanguage(language);
                if (year != null)   book.setPublished(Year.of(year));
                if (pages != null)  book.setPages(pages);
                if (!isbn.isBlank()) book.setIsbn(isbn);
                if (publisher != null) book.setPublisher(publisher);

                if (!clib.isBlank()) {
                    try {
                        book.setClib(Clib.valueOf(clib.trim().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        result.addError(rowNum, "Ongeldige CLIB: " + clib);
                        continue;
                    }
                }
                if (!fontSize.isBlank()) {
                    try {
                        book.setFontSize(FontSize.valueOf(fontSize.trim().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        result.addError(rowNum, "Ongeldige lettergrootte: " + fontSize);
                        continue;
                    }
                }
                if (!cover.isBlank()) {
                    String saved = uploadService.saveCoverFromUrl(cover);
                    if (saved != null) {
                        book.setCover(saved);
                    } else {
                        System.out.println("Row " + rowNum + ": cover URL kon niet worden gedownload: " + cover);
                    }
                }

                bookRepository.save(book);
                result.incrementAdded();
            }

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Kon bestand niet lezen", e);
        }

        return ResponseEntity.ok(result);
    }

    private boolean parseBoolean(String value, boolean defaultValue) {
        if (value.isBlank()) return defaultValue;
        return value.equalsIgnoreCase("JA");
    }

    private String getCellString(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) yield "";
                yield new DataFormatter().formatCellValue(cell).trim();
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> new DataFormatter().formatCellValue(cell).trim();
            default      -> "";
        };
    }

    private String getCellIsbn(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case FORMULA -> new DataFormatter().formatCellValue(cell).trim();
            default      -> "";
        };
    }
}