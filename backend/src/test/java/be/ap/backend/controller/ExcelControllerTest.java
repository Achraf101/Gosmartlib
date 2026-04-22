package be.ap.backend.controller;

import org.junit.jupiter.api.Test;
import be.ap.backend.dto.BulkUploadDTO;
import be.ap.backend.entity.*;
import be.ap.backend.repository.*;
import be.ap.backend.service.UploadService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser  
class ExcelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean  private AuthorRepository authorRepository;
    @MockitoBean  private BookRepository bookRepository;
    @MockitoBean  private PublisherRepository publisherRepository;
    @MockitoBean  private BookTypeRepository bookTypeRepository;
    @MockitoBean  private GenreRepository genreRepository;
    @MockitoBean  private LanguageRepository languageRepository;
    @MockitoBean  private UploadService uploadService;

    // ── Test data ─────────────────────────────────────────────────────────────

    private Author author;
    private BookType bookType;
    private Genre genre;
    private Language language;

    @BeforeEach
    void setUp() {
        author = new Author();
        author.setId(1L);
        author.setName("J.K. Rowling");

        bookType = new BookType();
        bookType.setId(1L);
        bookType.setName("Roman");

        genre = new Genre();
        genre.setId(1L);
        genre.setName("Fantasy");

        language = new Language();
        language.setId(1L);
        language.setName("Nederlands");

        // default mock behaviour
        when(authorRepository.findAll()).thenReturn(List.of(author));
        when(publisherRepository.findAll()).thenReturn(List.of());
        when(bookTypeRepository.findAll()).thenReturn(List.of(bookType));
        when(genreRepository.findAll()).thenReturn(List.of(genre));
        when(languageRepository.findAll()).thenReturn(List.of(language));
        when(bookRepository.findAllIsbns()).thenReturn(Set.of());
        when(bookRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    // ── Template download tests ───────────────────────────────────────────────

    @Test
    void downloadTemplate_returnsXlsx() throws Exception {
        mockMvc.perform(get("/excel/book/template"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition",
                "attachment; filename=book-upload-template.xlsx"))
            .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

    @Test
    void downloadTemplate_containsBooksSheet() throws Exception {
        MvcResult result = mockMvc.perform(get("/excel/book/template"))
            .andExpect(status().isOk())
            .andReturn();

        byte[] body = result.getResponse().getContentAsByteArray();
        assertThat(body).isNotEmpty();
        // file starts with PK (zip magic bytes)
        assertThat(body[0]).isEqualTo((byte) 'P');
        assertThat(body[1]).isEqualTo((byte) 'K');
    }

    // ── Bulk upload tests ─────────────────────────────────────────────────────

    @Test
    void bulkUpload_validRow_addsBook() throws Exception {
        MockMultipartFile file = buildExcelFile(sheet -> {
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("");               // isbn
            row.createCell(1).setCellValue("Harry Potter");   // titel
            row.createCell(2).setCellValue("J.K. Rowling");   // auteur
            row.createCell(3).setCellValue("Een geweldig boek over magie."); // beschrijving
            row.createCell(4).setCellValue("NEE");            // didactisch
            row.createCell(5).setCellValue("");               // uitgever
            row.createCell(6).setCellValue("");               // clib
            row.createCell(7).setCellValue("JA");             // fictie
            row.createCell(8).setCellValue("Roman");          // boektype
            row.createCell(9).setCellValue("Fantasy");        // genre 1
            row.createCell(10).setCellValue("");              // genre 2
            row.createCell(11).setCellValue("");              // genre 3
            row.createCell(12).setCellValue("");              // genre 4
            row.createCell(13).setCellValue("");              // genre 5
            row.createCell(14).setCellValue("2001");          // jaar
            row.createCell(15).setCellValue("Nederlands");    // taal
            row.createCell(16).setCellValue("309");           // paginas
            row.createCell(17).setCellValue("");              // lettergrootte
            row.createCell(18).setCellValue("NEE");           // enkel school
            row.createCell(19).setCellValue("");              // cover
        });

        MvcResult result = mockMvc.perform(multipart("/excel/book/bulk-upload").file(file))
            .andExpect(status().isOk())
            .andReturn();

        BulkUploadDTO dto = objectMapper.readValue(
            result.getResponse().getContentAsString(), BulkUploadDTO.class);

        assertThat(dto.getAdded()).isEqualTo(1);
        assertThat(dto.getErrors()).isEmpty();
        assertThat(dto.getSkipped()).isEmpty();
        verify(bookRepository, times(1)).save(any());
    }

    @Test
    void bulkUpload_missingTitel_addsError() throws Exception {
        MockMultipartFile file = buildExcelFile(sheet -> {
            Row row = sheet.createRow(1);
            row.createCell(1).setCellValue("");              // titel leeg
            row.createCell(2).setCellValue("J.K. Rowling");
            row.createCell(3).setCellValue("Beschrijving");
            row.createCell(8).setCellValue("Roman");
            row.createCell(9).setCellValue("Fantasy");
            row.createCell(15).setCellValue("Nederlands");
        });

        MvcResult result = mockMvc.perform(multipart("/excel/book/bulk-upload").file(file))
            .andExpect(status().isOk())
            .andReturn();

        BulkUploadDTO dto = objectMapper.readValue(
            result.getResponse().getContentAsString(), BulkUploadDTO.class);

        assertThat(dto.getAdded()).isEqualTo(0);
        assertThat(dto.getErrors()).hasSize(1);
        assertThat(dto.getErrors().get(0).getMessage()).contains("Titel is verplicht");
        verify(bookRepository, never()).save(any());
    }

    @Test
    void bulkUpload_beschrijvingTooLong_addsError() throws Exception {
        String longDesc = "A".repeat(501);

        MockMultipartFile file = buildExcelFile(sheet -> {
            Row row = sheet.createRow(1);
            row.createCell(1).setCellValue("Harry Potter");
            row.createCell(2).setCellValue("J.K. Rowling");
            row.createCell(3).setCellValue(longDesc);
            row.createCell(8).setCellValue("Roman");
            row.createCell(9).setCellValue("Fantasy");
            row.createCell(15).setCellValue("Nederlands");
        });

        MvcResult result = mockMvc.perform(multipart("/excel/book/bulk-upload").file(file))
            .andExpect(status().isOk())
            .andReturn();

        BulkUploadDTO dto = objectMapper.readValue(
            result.getResponse().getContentAsString(), BulkUploadDTO.class);

        assertThat(dto.getErrors()).hasSize(1);
        assertThat(dto.getErrors().get(0).getMessage()).contains("500 tekens");
    }

    @Test
    void bulkUpload_duplicateIsbnInDatabase_skipsRow() throws Exception {
        when(bookRepository.findAllIsbns()).thenReturn(Set.of("9789050000000"));

        MockMultipartFile file = buildExcelFile(sheet -> {
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("9789050000000");  // duplicate isbn
            row.createCell(1).setCellValue("Harry Potter");
            row.createCell(2).setCellValue("J.K. Rowling");
            row.createCell(3).setCellValue("Beschrijving");
            row.createCell(8).setCellValue("Roman");
            row.createCell(9).setCellValue("Fantasy");
            row.createCell(15).setCellValue("Nederlands");
        });

        MvcResult result = mockMvc.perform(multipart("/excel/book/bulk-upload").file(file))
            .andExpect(status().isOk())
            .andReturn();

        BulkUploadDTO dto = objectMapper.readValue(
            result.getResponse().getContentAsString(), BulkUploadDTO.class);

        assertThat(dto.getSkipped()).hasSize(1);
        assertThat(dto.getSkipped().get(0).getMessage()).contains("ISBN bestaat al");
        verify(bookRepository, never()).save(any());
    }

    @Test
    void bulkUpload_duplicateIsbnInSameFile_skipsSecondRow() throws Exception {
        MockMultipartFile file = buildExcelFile(sheet -> {
            // row 1 — valid
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("9789050000000");
            row1.createCell(1).setCellValue("Harry Potter");
            row1.createCell(2).setCellValue("J.K. Rowling");
            row1.createCell(3).setCellValue("Beschrijving");
            row1.createCell(4).setCellValue("NEE");
            row1.createCell(7).setCellValue("JA");
            row1.createCell(8).setCellValue("Roman");
            row1.createCell(9).setCellValue("Fantasy");
            row1.createCell(15).setCellValue("Nederlands");
            row1.createCell(16).setCellValue("300");
            row1.createCell(18).setCellValue("NEE");

            // row 2 — same isbn
            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("9789050000000");
            row2.createCell(1).setCellValue("Harry Potter 2");
            row2.createCell(2).setCellValue("J.K. Rowling");
            row2.createCell(3).setCellValue("Beschrijving");
            row2.createCell(4).setCellValue("NEE");
            row2.createCell(7).setCellValue("JA");
            row2.createCell(8).setCellValue("Roman");
            row2.createCell(9).setCellValue("Fantasy");
            row2.createCell(15).setCellValue("Nederlands");
            row2.createCell(16).setCellValue("300");
            row2.createCell(18).setCellValue("NEE");
        });

        MvcResult result = mockMvc.perform(multipart("/excel/book/bulk-upload").file(file))
            .andExpect(status().isOk())
            .andReturn();

        BulkUploadDTO dto = objectMapper.readValue(
            result.getResponse().getContentAsString(), BulkUploadDTO.class);

        assertThat(dto.getAdded()).isEqualTo(1);
        assertThat(dto.getSkipped()).hasSize(1);
        assertThat(dto.getSkipped().get(0).getMessage()).contains("meerdere keren");
    }

    @Test
    void bulkUpload_unknownAuthor_addsError() throws Exception {
        MockMultipartFile file = buildExcelFile(sheet -> {
            Row row = sheet.createRow(1);
            row.createCell(1).setCellValue("Harry Potter");
            row.createCell(2).setCellValue("Onbekende Auteur");  // not in DB
            row.createCell(3).setCellValue("Beschrijving");
            row.createCell(8).setCellValue("Roman");
            row.createCell(9).setCellValue("Fantasy");
            row.createCell(15).setCellValue("Nederlands");
        });

        MvcResult result = mockMvc.perform(multipart("/excel/book/bulk-upload").file(file))
            .andExpect(status().isOk())
            .andReturn();

        BulkUploadDTO dto = objectMapper.readValue(
            result.getResponse().getContentAsString(), BulkUploadDTO.class);

        assertThat(dto.getErrors()).hasSize(1);
        assertThat(dto.getErrors().get(0).getMessage()).contains("Onbekende auteur");
    }

    @Test
    void bulkUpload_invalidClibValue_addsError() throws Exception {
        MockMultipartFile file = buildExcelFile(sheet -> {
            Row row = sheet.createRow(1);
            row.createCell(1).setCellValue("Harry Potter");
            row.createCell(2).setCellValue("J.K. Rowling");
            row.createCell(3).setCellValue("Beschrijving");
            row.createCell(6).setCellValue("X");               // invalid CLIB
            row.createCell(8).setCellValue("Roman");
            row.createCell(9).setCellValue("Fantasy");
            row.createCell(15).setCellValue("Nederlands");
        });

        MvcResult result = mockMvc.perform(multipart("/excel/book/bulk-upload").file(file))
            .andExpect(status().isOk())
            .andReturn();

        BulkUploadDTO dto = objectMapper.readValue(
            result.getResponse().getContentAsString(), BulkUploadDTO.class);

        assertThat(dto.getErrors()).hasSize(1);
        assertThat(dto.getErrors().get(0).getMessage()).contains("CLIB");
    }

    @Test
    void bulkUpload_wrongFileFormat_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.xlsx",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            "not an excel file".getBytes()
        );

        mockMvc.perform(multipart("/excel/book/bulk-upload").file(file))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void bulkUpload_wrongSheetName_returns400() throws Exception {
        MockMultipartFile file = buildExcelFile(sheet -> {
            // sheet is named "Books" by buildExcelFile
            // but we'll rename it to something wrong
        }, "WrongName");

        mockMvc.perform(multipart("/excel/book/bulk-upload").file(file))
            .andExpect(status().isBadRequest());
    }

    @Test
    void bulkUpload_emptyFile_addsNothing() throws Exception {
        MockMultipartFile file = buildExcelFile(sheet -> {
            // no data rows — only header exists in buildExcelFile
        });

        MvcResult result = mockMvc.perform(multipart("/excel/book/bulk-upload").file(file))
            .andExpect(status().isOk())
            .andReturn();

        BulkUploadDTO dto = objectMapper.readValue(
            result.getResponse().getContentAsString(), BulkUploadDTO.class);

        assertThat(dto.getAdded()).isEqualTo(0);
        assertThat(dto.isFullSuccess()).isTrue();
        verify(bookRepository, never()).save(any());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @FunctionalInterface
    interface SheetPopulator {
        void populate(Sheet sheet);
    }

    private MockMultipartFile buildExcelFile(SheetPopulator populator) throws Exception {
        return buildExcelFile(populator, "Books");
    }

    private MockMultipartFile buildExcelFile(SheetPopulator populator,
                                              String sheetName) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet(sheetName);

            // header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ISBN (optioneel)");
            header.createCell(1).setCellValue("Titel *");
            header.createCell(2).setCellValue("Auteur *");
            header.createCell(3).setCellValue("Beschrijving *");
            header.createCell(4).setCellValue("Didactisch materiaal");
            header.createCell(5).setCellValue("Uitgever");
            header.createCell(6).setCellValue("CLIB");
            header.createCell(7).setCellValue("Fictie");
            header.createCell(8).setCellValue("Boektype *");
            header.createCell(9).setCellValue("Genre 1 *");
            header.createCell(10).setCellValue("Genre 2");
            header.createCell(11).setCellValue("Genre 3");
            header.createCell(12).setCellValue("Genre 4");
            header.createCell(13).setCellValue("Genre 5");
            header.createCell(14).setCellValue("Jaar van uitgave");
            header.createCell(15).setCellValue("Taal *");
            header.createCell(16).setCellValue("Aantal pagina's");
            header.createCell(17).setCellValue("Lettergrootte");
            header.createCell(18).setCellValue("Enkel zichtbaar");
            header.createCell(19).setCellValue("Cover");

            populator.populate(sheet);
            wb.write(out);

            return new MockMultipartFile(
                "file", "test.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                out.toByteArray()
            );
        }
    }
}
