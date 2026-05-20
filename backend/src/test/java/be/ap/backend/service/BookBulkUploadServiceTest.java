package be.ap.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.ap.backend.dto.BookLookupDTO;
import be.ap.backend.dto.BulkPreviewDTO;
import be.ap.backend.dto.BulkUploadDTO;
import be.ap.backend.entity.Author;
import be.ap.backend.repository.AuthorRepository;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.BookTypeRepository;
import be.ap.backend.repository.GenreRepository;
import be.ap.backend.repository.LanguageRepository;
import be.ap.backend.repository.PublisherRepository;
import be.ap.backend.repository.ThemeRepository;

@ExtendWith(MockitoExtension.class)
class BookBulkUploadServiceTest {

    @Mock private AuthorRepository authorRepository;
    @Mock private BookRepository bookRepository;
    @Mock private PublisherRepository publisherRepository;
    @Mock private BookTypeRepository bookTypeRepository;
    @Mock private GenreRepository genreRepository;
    @Mock private LanguageRepository languageRepository;
    @Mock private ThemeRepository themeRepository;
    @Mock private UploadService uploadService;
    @Mock private IsbnLookupService isbnLookupService;

    @InjectMocks
    private BookBulkUploadService service;

    @Test
    void givenFoundAndNotFoundIsbns_whenGeneratePreview_thenReturnCorrectCounts() throws Exception {
        BookLookupDTO dto = buildLookupDTO("1984", "George Orwell", "http://cover");
        when(isbnLookupService.lookup("9780141036144")).thenReturn(Optional.of(dto));
        when(isbnLookupService.lookup("0000000000000")).thenReturn(Optional.empty());

        BulkPreviewDTO result = service.generatePreview(buildPreviewExcel(List.of("9780141036144", "0000000000000")));

        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getFoundCount()).isEqualTo(1);
        assertThat(result.getNotFoundCount()).isEqualTo(1);
        assertThat(result.getItems().get(0).isFound()).isTrue();
        assertThat(result.getItems().get(1).isFound()).isFalse();
    }

    @Test
    void givenDuplicateIsbn_whenGeneratePreview_thenOnlyProcessedOnce() throws Exception {
        BookLookupDTO dto = buildLookupDTO("1984", "George Orwell", null);
        when(isbnLookupService.lookup("9780141036144")).thenReturn(Optional.of(dto));

        BulkPreviewDTO result = service.generatePreview(buildPreviewExcel(List.of("9780141036144", "9780141036144")));

        assertThat(result.getTotal()).isEqualTo(1);
        verify(isbnLookupService, times(1)).lookup(any());
    }

    @Test
    void givenBlankIsbn_whenGeneratePreview_thenSkipped() throws Exception {
        BulkPreviewDTO result = service.generatePreview(buildPreviewExcel(List.of("")));

        assertThat(result.getTotal()).isEqualTo(0);
        verifyNoInteractions(isbnLookupService);
    }

    @Test
    void givenAllFound_whenGeneratePreview_thenNotFoundCountIsZero() throws Exception {
        BookLookupDTO dto = buildLookupDTO("1984", "George Orwell", null);
        when(isbnLookupService.lookup("9780141036144")).thenReturn(Optional.of(dto));

        BulkPreviewDTO result = service.generatePreview(buildPreviewExcel(List.of("9780141036144")));

        assertThat(result.getFoundCount()).isEqualTo(1);
        assertThat(result.getNotFoundCount()).isEqualTo(0);
    }

    @Test
    void givenExistingIsbn_whenProcessUpload_thenSkipped() throws Exception {
        when(bookRepository.findAllIsbns()).thenReturn(Set.of("9780141036144"));
        stubEmptyRepos();

        BulkUploadDTO result = service.processUpload(buildFullExcel(List.<String[]>of(
                rowData("9780141036144", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "")
        )));

        assertThat(result.getSkipped()).hasSize(1);
        assertThat(result.getSkipped().get(0).getMessage()).contains("ISBN bestaat al");
    }

    @Test
    void givenDuplicateIsbnInFile_whenProcessUpload_thenSecondSkipped() throws Exception {
        when(bookRepository.findAllIsbns()).thenReturn(Set.of());
        stubEmptyRepos();
        when(authorRepository.save(any(Author.class))).thenAnswer(inv -> inv.getArgument(0));
        BookLookupDTO dto = buildLookupDTO("1984", "George Orwell", null);
        when(isbnLookupService.lookup("9780141036144")).thenReturn(Optional.of(dto));

        BulkUploadDTO result = service.processUpload(buildFullExcel(List.of(
                rowData("9780141036144", "1984", "George Orwell", "", "", "", "", "", "", "", "", "", "", "", "", "", ""),
                rowData("9780141036144", "1984", "George Orwell", "", "", "", "", "", "", "", "", "", "", "", "", "", "")
        )));

        assertThat(result.getSkipped()).hasSize(1);
        assertThat(result.getSkipped().get(0).getMessage()).contains("meerdere keren");
    }

    @Test
    void givenInvalidBooleanField_whenProcessUpload_thenErrorAdded() throws Exception {
        when(bookRepository.findAllIsbns()).thenReturn(Set.of());
        Author author = buildAuthor("Auteur");
        when(authorRepository.findAll()).thenReturn(List.of(author));
        stubEmptyReposExceptAuthor();

        BulkUploadDTO result = service.processUpload(buildFullExcel(List.<String[]>of(
                rowData("", "Titel", "Auteur", "Beschrijving", "MISSCHIEN", "", "", "JA",
                        "Boek", "", "", "Nederlands", "", "", "", "NEE", "")
        )));

        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessage()).contains("JA of NEE");
    }

    @Test
    void givenIsbnLookupTitleMismatch_whenProcessUpload_thenSkipped() throws Exception {
        when(bookRepository.findAllIsbns()).thenReturn(Set.of());
        stubEmptyRepos();
        BookLookupDTO dto = buildLookupDTO("1984", "George Orwell", null);
        when(isbnLookupService.lookup("9780141036144")).thenReturn(Optional.of(dto));

        BulkUploadDTO result = service.processUpload(buildFullExcel(List.<String[]>of(
                new String[]{"9780141036144", "Verkeerde Titel", "George Orwell", "", "", "", "", "", "", "", "", "", "", "", "", "", ""}   
        )));

        assertThat(result.getSkipped()).hasSize(1);
        assertThat(result.getSkipped().get(0).getMessage()).contains("titel/auteur");
    }

    @Test
    void givenIsbnNotFoundInExternalDb_whenProcessUpload_thenSkipped() throws Exception {
        when(bookRepository.findAllIsbns()).thenReturn(Set.of());
        stubEmptyRepos();
        when(isbnLookupService.lookup("9780141036144")).thenReturn(Optional.empty());

        BulkUploadDTO result = service.processUpload(buildFullExcel(List.<String[]>of(
                new String[]{"9780141036144", "1984", "George Orwell", "", "", "", "", "", "", "", "", "", "", "", "", "", ""}
        )));

        assertThat(result.getSkipped()).hasSize(1);
        assertThat(result.getSkipped().get(0).getMessage()).contains("externe database");
    }

    private void stubEmptyRepos() {
        when(authorRepository.findAll()).thenReturn(List.of());
        when(publisherRepository.findAll()).thenReturn(List.of());
        when(bookTypeRepository.findAll()).thenReturn(List.of());
        when(genreRepository.findAll()).thenReturn(List.of());
        when(languageRepository.findAll()).thenReturn(List.of());
        when(themeRepository.findAll()).thenReturn(List.of());
    }

    private void stubEmptyReposExceptAuthor() {
        when(publisherRepository.findAll()).thenReturn(List.of());
        when(bookTypeRepository.findAll()).thenReturn(List.of());
        when(genreRepository.findAll()).thenReturn(List.of());
        when(languageRepository.findAll()).thenReturn(List.of());
        when(themeRepository.findAll()).thenReturn(List.of());
    }

    private BookLookupDTO buildLookupDTO(String title, String authorName, String coverUrl) {
        BookLookupDTO dto = new BookLookupDTO();
        dto.setTitle(title);
        dto.setAuthorName(authorName);
        dto.setCoverUrl(coverUrl);
        return dto;
    }

    private Author buildAuthor(String name) {
        Author author = new Author();
        author.setName(name);
        return author;
    }

    private InputStream buildPreviewExcel(List<String> isbns) throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Books");
        sheet.createRow(0).createCell(0).setCellValue("ISBN");
        for (int i = 0; i < isbns.size(); i++) {
            sheet.createRow(i + 1).createCell(0).setCellValue(isbns.get(i));
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

    private InputStream buildFullExcel(List<String[]> rows) throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Books");
        sheet.createRow(0);
        for (int i = 0; i < rows.size(); i++) {
            Row row = sheet.createRow(i + 1);
            String[] cols = rows.get(i);
            for (int j = 0; j < cols.length; j++) {
                row.createCell(j).setCellValue(cols[j]);
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

    private String[] rowData(String... cols) {
        return cols;
    }
}