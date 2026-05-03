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
import be.ap.backend.entity.Author;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.BookType;
import be.ap.backend.entity.Clib;
import be.ap.backend.entity.FontSize;
import be.ap.backend.entity.Genre;
import be.ap.backend.entity.Language;
import be.ap.backend.entity.Publisher;
import be.ap.backend.repository.AuthorRepository;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.BookTypeRepository;
import be.ap.backend.repository.GenreRepository;
import be.ap.backend.repository.LanguageRepository;
import be.ap.backend.repository.PublisherRepository;
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
    private final IsbnLookupService isbnLookupService;


    public ExcelController(AuthorRepository authorRepository,
                           BookRepository bookRepository,
                           PublisherRepository publisherRepository,
                           BookTypeRepository bookTypeRepository,
                           GenreRepository genreRepository,
                           LanguageRepository languageRepository,
                           UploadService uploadService,
                           IsbnLookupService isbnLookupService)
                         {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.publisherRepository = publisherRepository;
        this.bookTypeRepository = bookTypeRepository;
        this.genreRepository = genreRepository;
        this.languageRepository = languageRepository;
        this.uploadService = uploadService;
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
        return baos.toByteArray();
    }

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
                <c r="D1" t="inlineStr"><is><t>Beschrijving * (max 500 tekens)</t></is></c>
                <c r="E1" t="inlineStr"><is><t>Didactisch materiaal (JA/NEE)</t></is></c>
                <c r="F1" t="inlineStr"><is><t>Uitgever (optioneel)</t></is></c>
                <c r="G1" t="inlineStr"><is><t>CLIB (optioneel, A/B/C/D)</t></is></c>
                <c r="H1" t="inlineStr"><is><t>Fictie (JA/NEE)</t></is></c>
                <c r="I1" t="inlineStr"><is><t>Boektype *</t></is></c>
                <c r="J1" t="inlineStr"><is><t>Genres * (gescheiden door spaties)</t></is></c>
                <c r="K1" t="inlineStr"><is><t>Jaar van uitgave (optioneel)</t></is></c>
                <c r="L1" t="inlineStr"><is><t>Taal *</t></is></c>
                <c r="M1" t="inlineStr"><is><t>Aantal pagina's *</t></is></c>
                <c r="N1" t="inlineStr"><is><t>Lettergrootte (optioneel: groot/medium/klein)</t></is></c>
                <c r="O1" t="inlineStr"><is><t>Enkel zichtbaar voor deze school (JA/NEE)</t></is></c>
                <c r="P1" t="inlineStr"><is><t>Cover (optioneel, URL)</t></is></c>
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
                if (row == null) continue;

                String isbn          = getCellIsbn(row, 0);
                String titel         = getCellString(row, 1);
                String auteurNaam    = getCellString(row, 2);
                String beschrijving  = getCellString(row, 3);
                String didactisch    = getCellString(row, 4);
                String uitgeverNaam  = getCellString(row, 5);
                String clib          = getCellString(row, 6);
                String fictie        = getCellString(row, 7);
                String boektypeNaam  = getCellString(row, 8);
                String genresRaw     = getCellString(row, 9);
                String jaarStr       = getCellString(row, 10);
                String taalNaam      = getCellString(row, 11);
                String paginasStr    = getCellString(row, 12);
                String lettergrootte = getCellString(row, 13);
                String enkelSchool   = getCellString(row, 14);
                String cover         = getCellString(row, 15);

                int rowNum = i + 1;

                BookLookupDTO lookup = null;
                if (!isbn.isBlank()) {
                    lookup = lookupCache.computeIfAbsent(isbn,
                        key -> isbnLookupService.lookup(key).orElse(null));
                }

                if (lookup != null) {
                    if (titel.isBlank() && lookup.getTitle() != null) titel = lookup.getTitle();
                    if (auteurNaam.isBlank() && lookup.getAuthorName() != null) auteurNaam = lookup.getAuthorName();
                    if (beschrijving.isBlank() && lookup.getDescription() != null) beschrijving = lookup.getDescription();
                    if (uitgeverNaam.isBlank() && lookup.getPublisherName() != null) uitgeverNaam = lookup.getPublisherName();
                    if (jaarStr.isBlank() && lookup.getPublishedYear() != null) jaarStr = String.valueOf(lookup.getPublishedYear());
                    if (paginasStr.isBlank() && lookup.getPages() != null) paginasStr = String.valueOf(lookup.getPages());
                    if (cover.isBlank() && lookup.getCoverUrl() != null) cover = lookup.getCoverUrl();
                    if (taalNaam.isBlank() && lookup.getLanguageCode() != null) {
                        Language byCode = languageByCode.get(lookup.getLanguageCode().toLowerCase());
                        if (byCode != null) taalNaam = byCode.getName();
                    }
                }

                if (titel.isBlank()) {
                    result.addError(rowNum, "Titel is verplicht");
                    continue;
                }
                if (auteurNaam.isBlank()) {
                    result.addError(rowNum, "Auteur is verplicht");
                    continue;
                }
                if (beschrijving.isBlank()) {
                    result.addError(rowNum, "Beschrijving is verplicht");
                    continue;
                }
                if (beschrijving.length() > 500) {
                    beschrijving = beschrijving.substring(0, 500);
                }
                if (boektypeNaam.isBlank()) {
                    result.addError(rowNum, "Boektype is verplicht");
                    continue;
                }
                if (genresRaw.isBlank()) {
                    result.addError(rowNum, "Minstens 1 genre is verplicht");
                    continue;
                }
                if (taalNaam.isBlank()) {
                    result.addError(rowNum, "Taal is verplicht");
                    continue;
                }
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

                Author auteur = authorMap.get(auteurNaam.toLowerCase());
                if (auteur == null) {
                    result.addError(rowNum, "Onbekende auteur: " + auteurNaam);
                    continue;
                }

                if (!didactisch.isBlank() && !didactisch.equalsIgnoreCase("JA") && !didactisch.equalsIgnoreCase("NEE")) {
                    result.addError(rowNum, "Didactisch materiaal moet JA of NEE zijn");
                    continue;
                }

                if (!fictie.isBlank() && !fictie.equalsIgnoreCase("JA") && !fictie.equalsIgnoreCase("NEE")) {
                    result.addError(rowNum, "Fictie moet JA of NEE zijn");
                    continue;
                }

                BookType boektype = bookTypeMap.get(boektypeNaam.toLowerCase());
                if (boektype == null) {
                    result.addError(rowNum, "Onbekend boektype: " + boektypeNaam);
                    continue;
                }

                Language taal = languageMap.get(taalNaam.toLowerCase());
                if (taal == null) {
                    result.addError(rowNum, "Onbekende taal: " + taalNaam);
                    continue;
                }

                Set<String> genreNames = new LinkedHashSet<>();
                for (String token : genresRaw.split("\\s+")) {
                    if (!token.isBlank()) genreNames.add(token.toLowerCase());
                }

                List<Genre> genres = new ArrayList<>();
                boolean genreError = false;
                for (String naam : genreNames) {
                    Genre genre = genreMap.get(naam);
                    if (genre == null) {
                        result.addError(rowNum, "Onbekend genre: " + naam);
                        genreError = true;
                        break;
                    }
                    genres.add(genre);
                }
                if (genreError) continue;

                Publisher uitgever = null;
                if (!uitgeverNaam.isBlank()) {
                    uitgever = publisherMap.get(uitgeverNaam.toLowerCase());
                    if (uitgever == null) {
                        result.addError(rowNum, "Onbekende uitgever: " + uitgeverNaam);
                        continue;
                    }
                }

                if (!clib.isBlank() && !List.of("A","B","C","D").contains(clib.toUpperCase())) {
                    result.addError(rowNum, "Ongeldig CLIB niveau: " + clib + " (A, B, C of D)");
                    continue;
                }

                if (!lettergrootte.isBlank() &&
                    !List.of("groot","medium","klein").contains(lettergrootte.toLowerCase())) {
                    result.addError(rowNum, "Ongeldige lettergrootte: " + lettergrootte);
                    continue;
                }

                Integer jaarVanUitgave = null;
                if (!jaarStr.isBlank()) {
                    try {
                        jaarVanUitgave = Integer.parseInt(jaarStr.replace(".0", "").trim());
                    } catch (NumberFormatException e) {
                        result.addError(rowNum, "Ongeldig jaar: " + jaarStr);
                        continue;
                    }
                }

                Integer aantalPaginas = null;
                if (!paginasStr.isBlank()) {
                    try {
                        aantalPaginas = Integer.parseInt(paginasStr.replace(".0", "").trim());
                        if (aantalPaginas < 1) {
                            result.addError(rowNum, "Aantal pagina's moet minimaal 1 zijn");
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        result.addError(rowNum, "Ongeldig aantal pagina's: " + paginasStr);
                        continue;
                    }
                }

                if (!enkelSchool.isBlank() && !enkelSchool.equalsIgnoreCase("JA") && !enkelSchool.equalsIgnoreCase("NEE")) {
                    result.addError(rowNum, "Enkel zichtbaar voor deze school moet JA of NEE zijn");
                    continue;
                }

                Book book = new Book();
                book.setTitle(titel);
                book.setAuthor(auteur);
                book.setDescription(beschrijving);
                book.setFiction(parseBoolean(fictie, true));
                book.setBookType(boektype);
                book.setGenres(new HashSet<>(genres));
                book.setLanguage(taal);
                if(jaarVanUitgave != null)
                    book.setPublished(Year.of(jaarVanUitgave));
                if(aantalPaginas != null)
                    book.setPages(aantalPaginas);

                if (!isbn.isBlank())        book.setIsbn(isbn);
                if (uitgever != null)       book.setPublisher(uitgever);

                if (clib != null && !clib.isBlank()) {
                    try {
                        book.setClib(Clib.valueOf(clib.trim().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        result.addError(rowNum, "Ongeldige CLIB: " + clib);
                        continue;
                    }
                }

                if (lettergrootte != null && !lettergrootte.isBlank()) {
                    try {
                        book.setFontSize(FontSize.valueOf(lettergrootte.trim().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        result.addError(rowNum, "Ongeldige lettergrootte: " + lettergrootte);
                        continue;
                    }
                }
                if (!cover.isBlank()) {
                    String savedFilename = uploadService.saveCoverFromUrl(cover);
                    if (savedFilename != null) {
                        book.setCover(savedFilename);
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

    String raw = switch (cell.getCellType()) {
        case STRING  -> cell.getStringCellValue().trim();
        case NUMERIC -> {
            long val = (long) cell.getNumericCellValue();
            yield String.valueOf(val);
        }
        case FORMULA -> new DataFormatter().formatCellValue(cell).trim();
        default      -> "";
    };

    return raw;
}
}
