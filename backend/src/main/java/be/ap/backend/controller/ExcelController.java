package be.ap.backend.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationConstraint.OperatorType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import be.ap.backend.service.UploadService;

@RestController
@RequestMapping("/excel/book")
public class ExcelController {

    private static final int MAX_DATA_ROWS = 500;

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;
    private final BookTypeRepository bookTypeRepository;
    private final GenreRepository genreRepository;
    private final LanguageRepository languageRepository;
    private final UploadService uploadService;


    public ExcelController(AuthorRepository authorRepository,
                           BookRepository bookRepository,
                           PublisherRepository publisherRepository,
                           BookTypeRepository bookTypeRepository,
                           GenreRepository genreRepository,
                           LanguageRepository languageRepository,
                           UploadService uploadService)
                         {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.publisherRepository = publisherRepository;
        this.bookTypeRepository = bookTypeRepository;
        this.genreRepository = genreRepository;
        this.languageRepository = languageRepository;
        this.uploadService = uploadService;
    }

    @GetMapping("template")
    public ResponseEntity<byte[]> downloadTemplate() {
        List<Author> authors = authorRepository.findAll();
        List<Publisher> publishers = publisherRepository.findAll();
        List<BookType> bookTypes = bookTypeRepository.findAll();
        List<Genre> genres = genreRepository.findAll();
        List<Language> languages = languageRepository.findAll();

        try {
            byte[] xlsx = buildTemplateXlsx(authors, publishers, bookTypes, genres, languages);
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=book-upload-template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(xlsx);
        } catch (IOException e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "Could not generate template", e);
        }
    }

    private byte[] buildTemplateXlsx(List<Author> authors,
                                     List<Publisher> publishers,
                                     List<BookType> bookTypes,
                                     List<Genre> genres,
                                     List<Language> languages) throws IOException {

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            XSSFSheet sheet = wb.createSheet("Books");
            XSSFSheet lists = wb.createSheet("_lists");
            wb.setSheetHidden(wb.getSheetIndex(lists), true);

            String authorRef    = writeListColumn(lists, 0, sortedNames(authors,    Author::getName));
            String publisherRef = writeListColumn(lists, 1, sortedNames(publishers, Publisher::getName));
            String bookTypeRef  = writeListColumn(lists, 2, sortedNames(bookTypes,  BookType::getName));
            // Genre values are written for reference (CSV column has no list validation).
            writeListColumn(lists, 3, sortedNames(genres, Genre::getName));
            String languageRef  = writeListColumn(lists, 4, sortedNames(languages,  Language::getName));

            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            CellStyle textStyle = wb.createCellStyle();
            textStyle.setDataFormat(wb.createDataFormat().getFormat("@"));

            CellStyle intStyle = wb.createCellStyle();
            intStyle.setDataFormat(wb.createDataFormat().getFormat("0"));

            String[] headers = {
                "ISBN (optioneel)",
                "Titel *",
                "Auteur * (kies uit lijst)",
                "Beschrijving * (max 500 tekens)",
                "Didactisch materiaal (JA/NEE)",
                "Uitgever (optioneel, kies uit lijst)",
                "CLIB (optioneel, A/B/C/D)",
                "Fictie (JA/NEE)",
                "Boektype * (kies uit lijst)",
                "Genres * (gescheiden door komma's, zie tabblad _lists)",
                "Jaar van uitgave (optioneel)",
                "Taal * (kies uit lijst)",
                "Aantal pagina's *",
                "Lettergrootte (optioneel: groot/medium/klein)",
                "Enkel zichtbaar voor deze school (JA/NEE)",
                "Cover (optioneel, URL)"
            };
            int[] widths = { 18, 32, 28, 60, 22, 28, 12, 12, 22, 45, 14, 22, 14, 24, 30, 35 };

            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, widths[i] * 256);
            }

            sheet.setDefaultColumnStyle(0, textStyle);   // ISBN
            sheet.setDefaultColumnStyle(10, intStyle);   // Jaar
            sheet.setDefaultColumnStyle(12, intStyle);   // Pagina's

            sheet.createFreezePane(0, 1);

            XSSFDataValidationHelper dvh = new XSSFDataValidationHelper(sheet);

            addListValidation(sheet, dvh, 2,  authorRef,    true,
                "Ongeldige auteur",   "Kies een auteur uit de lijst.");
            addListValidation(sheet, dvh, 5,  publisherRef, true,
                "Ongeldige uitgever", "Kies een uitgever uit de lijst.");
            addListValidation(sheet, dvh, 8,  bookTypeRef,  false,
                "Ongeldig boektype",  "Kies een boektype uit de lijst.");
            addListValidation(sheet, dvh, 11, languageRef,  false,
                "Ongeldige taal",     "Kies een taal uit de lijst.");

            addExplicitListValidation(sheet, dvh, 6,  new String[]{"A","B","C","D"}, true,
                "Ongeldig CLIB niveau",  "Kies A, B, C of D.");
            addExplicitListValidation(sheet, dvh, 13, new String[]{"groot","medium","klein"}, true,
                "Ongeldige lettergrootte", "Kies groot, medium of klein.");
            addExplicitListValidation(sheet, dvh, 4,  new String[]{"JA","NEE"}, false,
                "Ongeldig", "Vul JA of NEE in.");
            addExplicitListValidation(sheet, dvh, 7,  new String[]{"JA","NEE"}, false,
                "Ongeldig", "Vul JA of NEE in.");
            addExplicitListValidation(sheet, dvh, 14, new String[]{"JA","NEE"}, false,
                "Ongeldig", "Vul JA of NEE in.");

            DataValidationConstraint yearConstraint = dvh.createIntegerConstraint(
                OperatorType.GREATER_OR_EQUAL, "1000", null);
            DataValidation yearVal = dvh.createValidation(yearConstraint,
                new CellRangeAddressList(1, MAX_DATA_ROWS, 10, 10));
            yearVal.setEmptyCellAllowed(true);
            yearVal.setShowErrorBox(true);
            yearVal.createErrorBox("Ongeldig jaar", "Vul een geldig jaar in (bv. 2000).");
            sheet.addValidationData(yearVal);

            DataValidationConstraint pagesConstraint = dvh.createIntegerConstraint(
                OperatorType.GREATER_OR_EQUAL, "1", null);
            DataValidation pagesVal = dvh.createValidation(pagesConstraint,
                new CellRangeAddressList(1, MAX_DATA_ROWS, 12, 12));
            pagesVal.setEmptyCellAllowed(true);
            pagesVal.setShowErrorBox(true);
            pagesVal.createErrorBox("Ongeldig aantal pagina's", "Vul een positief geheel getal in.");
            sheet.addValidationData(pagesVal);

            DataValidationConstraint descConstraint = dvh.createTextLengthConstraint(
                OperatorType.LESS_OR_EQUAL, "500", null);
            DataValidation descVal = dvh.createValidation(descConstraint,
                new CellRangeAddressList(1, MAX_DATA_ROWS, 3, 3));
            descVal.setShowErrorBox(true);
            descVal.createErrorBox("Beschrijving te lang", "Beschrijving mag maximaal 500 tekens bevatten.");
            sheet.addValidationData(descVal);

            wb.write(baos);
            return baos.toByteArray();
        }
    }

    private <T> List<String> sortedNames(List<T> items, Function<T, String> nameFn) {
        return items.stream()
            .map(nameFn)
            .sorted(Comparator.naturalOrder())
            .toList();
    }

    private String writeListColumn(XSSFSheet listSheet, int columnIndex, List<String> values) {
        String header = "list_" + columnIndex;
        Row headerRow = listSheet.getRow(0);
        if (headerRow == null) headerRow = listSheet.createRow(0);
        headerRow.createCell(columnIndex).setCellValue(header);

        for (int i = 0; i < values.size(); i++) {
            Row row = listSheet.getRow(i + 1);
            if (row == null) row = listSheet.createRow(i + 1);
            row.createCell(columnIndex).setCellValue(values.get(i));
        }

        if (values.isEmpty()) {
            // Empty range still needs a valid reference; point at the header cell only.
            String cell = CellReference.convertNumToColString(columnIndex) + "1";
            return "_lists!$" + cell.replace("1", "$1");
        }

        String col = CellReference.convertNumToColString(columnIndex);
        return "_lists!$" + col + "$2:$" + col + "$" + (values.size() + 1);
    }

    private void addListValidation(Sheet sheet, XSSFDataValidationHelper dvh, int colIdx,
                                   String formulaRef, boolean allowBlank,
                                   String errorTitle, String errorMsg) {
        DataValidationConstraint constraint = dvh.createFormulaListConstraint(formulaRef);
        DataValidation validation = dvh.createValidation(constraint,
            new CellRangeAddressList(1, MAX_DATA_ROWS, colIdx, colIdx));
        validation.setEmptyCellAllowed(allowBlank);
        validation.setShowErrorBox(true);
        validation.createErrorBox(errorTitle, errorMsg);
        sheet.addValidationData(validation);
    }

    private void addExplicitListValidation(Sheet sheet, XSSFDataValidationHelper dvh, int colIdx,
                                           String[] values, boolean allowBlank,
                                           String errorTitle, String errorMsg) {
        DataValidationConstraint constraint = dvh.createExplicitListConstraint(values);
        DataValidation validation = dvh.createValidation(constraint,
            new CellRangeAddressList(1, MAX_DATA_ROWS, colIdx, colIdx));
        validation.setEmptyCellAllowed(allowBlank);
        validation.setShowErrorBox(true);
        validation.createErrorBox(errorTitle, errorMsg);
        sheet.addValidationData(validation);
    }

    @PostMapping("bulk-upload")
    public ResponseEntity<BulkUploadDTO> bulkUpload(@RequestParam("file") MultipartFile file) {
        Map<String, Author> authorMap = authorRepository.findAll().stream()
            .collect(Collectors.toMap(a -> a.getName().toLowerCase(), a -> a));
        Map<String, Publisher> publisherMap = publisherRepository.findAll().stream()
            .collect(Collectors.toMap(p -> p.getName().toLowerCase(), p -> p));
        Map<String, BookType> bookTypeMap = bookTypeRepository.findAll().stream()
            .collect(Collectors.toMap(b -> b.getName().toLowerCase(), b -> b));
        Map<String, Genre> genreMap = genreRepository.findAll().stream()
            .collect(Collectors.toMap(g -> g.getName().toLowerCase(), g -> g));
        Map<String, Language> languageMap = languageRepository.findAll().stream()
            .collect(Collectors.toMap(l -> l.getName().toLowerCase(), l -> l));

        Set<String> existingIsbns = bookRepository.findAllIsbns();
        Set<String> seenIsbns = new HashSet<>();
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
                String genresCsv     = getCellString(row, 9);
                String jaarStr       = getCellString(row, 10);
                String taalNaam      = getCellString(row, 11);
                String paginasStr    = getCellString(row, 12);
                String lettergrootte = getCellString(row, 13);
                String enkelSchool   = getCellString(row, 14);
                String cover         = getCellString(row, 15);

                int rowNum = i + 1;

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
                    result.addError(rowNum, "Beschrijving mag maximaal 500 tekens bevatten");
                    continue;
                }
                if (boektypeNaam.isBlank()) {
                    result.addError(rowNum, "Boektype is verplicht");
                    continue;
                }
                if (genresCsv.isBlank()) {
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

                List<String> genreNamen = Arrays.stream(genresCsv.split(","))
                    .map(String::trim)
                    .filter(g -> !g.isBlank())
                    .map(String::toLowerCase)
                    .distinct()
                    .toList();

                if (genreNamen.isEmpty()) {
                    result.addError(rowNum, "Minstens 1 genre is verplicht");
                    continue;
                }

                List<Genre> genres = new ArrayList<>();
                boolean genreError = false;
                for (String naam : genreNamen) {
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
                // book.setDidactischMateriaal(parseBoolean(didactisch, false));
                book.setFiction(parseBoolean(fictie, true));
                book.setBookType(boektype);
                book.setGenres(new HashSet<>(genres));
                book.setLanguage(taal);
                if(jaarVanUitgave != null)
                    book.setPublished(Year.of(jaarVanUitgave));
                if(aantalPaginas != null)
                    book.setPages(aantalPaginas);
                // book.setEnkelZichtbaarVoorDezeSchool(parseBoolean(enkelSchool, false));

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
                // Prevents ISBN like 9781234567890 coming back as "9.78123456789E12"
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
            case NUMERIC -> {
                // convert directly from double to avoid scientific notation entirely
                long val = (long) cell.getNumericCellValue();
                yield String.valueOf(val);
            }
            case FORMULA -> new DataFormatter().formatCellValue(cell).trim();
            default      -> "";
        };
    }
}
