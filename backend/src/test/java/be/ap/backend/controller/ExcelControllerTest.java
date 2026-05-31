package be.ap.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import be.ap.backend.dto.BulkPreviewDTO;
import be.ap.backend.dto.BulkUploadDTO;
import be.ap.backend.service.BookBulkUploadService;
import be.ap.backend.util.ExcelTemplateBuilder;

@ExtendWith(MockitoExtension.class)
class ExcelControllerTest {

    @Mock
    private ExcelTemplateBuilder templateBuilder;
    @Mock
    private BookBulkUploadService bulkUploadService;

    @InjectMocks
    private ExcelController controller;

    // --- downloadTemplate ---

    @Test
    void givenTemplateBuilderSucceeds_whenDownloadTemplate_thenReturnOkWithBytes() throws Exception {
        byte[] fakeXlsx = new byte[] { 1, 2, 3 };
        when(templateBuilder.buildTemplateXlsx()).thenReturn(fakeXlsx);

        ResponseEntity<byte[]> response = controller.downloadTemplate();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(fakeXlsx, response.getBody());
        assertEquals("attachment; filename=book-upload-template.xlsx",
                response.getHeaders().getFirst("Content-Disposition"));
        assertEquals(MediaType.APPLICATION_OCTET_STREAM,
                response.getHeaders().getContentType());
    }

    @Test
    void givenTemplateBuilderThrows_whenDownloadTemplate_thenIOExceptionPropagates() throws Exception {
        when(templateBuilder.buildTemplateXlsx()).thenThrow(new IOException("disk error"));

        assertThrows(IOException.class, () -> controller.downloadTemplate());
    }

    // --- bulkPreview ---

    @Test
    void givenValidFile_whenBulkPreview_thenReturnPreviewDTO() throws Exception {
        BulkPreviewDTO dto = new BulkPreviewDTO();
        dto.addFound(2, "9780141036144", "1984", "George Orwell", null);
        when(bulkUploadService.generatePreview(any(InputStream.class))).thenReturn(dto);

        MockMultipartFile file = new MockMultipartFile("file", new byte[] { 1, 2, 3 });
        ResponseEntity<BulkPreviewDTO> response = controller.bulkPreview(file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotal());
    }

    @Test
    void givenFileGetInputStreamThrows_whenBulkPreview_thenIOExceptionPropagates() {
        MockMultipartFile file = new MockMultipartFile("file", new byte[] {}) {
            @Override
            public InputStream getInputStream() throws IOException {
                throw new IOException("unreadable");
            }
        };

        assertThrows(IOException.class, () -> controller.bulkPreview(file));
    }

    @Test
    void givenValidFile_whenBulkPreview_thenServiceReceivesInputStream() throws Exception {
        BulkPreviewDTO dto = new BulkPreviewDTO();
        when(bulkUploadService.generatePreview(any(InputStream.class))).thenReturn(dto);

        MockMultipartFile file = new MockMultipartFile("file", new byte[] { 1, 2, 3 });
        controller.bulkPreview(file);

        verify(bulkUploadService, times(1)).generatePreview(any(InputStream.class));
    }

    // --- bulkUpload ---

    @Test
    void givenValidFile_whenBulkUpload_thenReturnUploadDTO() throws Exception {
        BulkUploadDTO dto = new BulkUploadDTO();
        when(bulkUploadService.processUpload(any(InputStream.class))).thenReturn(dto);

        MockMultipartFile file = new MockMultipartFile("file", new byte[] { 1, 2, 3 });
        ResponseEntity<BulkUploadDTO> response = controller.bulkUpload(file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void givenFileGetInputStreamThrows_whenBulkUpload_thenIOExceptionPropagates() {
        MockMultipartFile file = new MockMultipartFile("file", new byte[] {}) {
            @Override
            public InputStream getInputStream() throws IOException {
                throw new IOException("unreadable");
            }
        };

        assertThrows(IOException.class, () -> controller.bulkUpload(file));
    }

    @Test
    void givenValidFile_whenBulkUpload_thenServiceReceivesInputStream() throws Exception {
        BulkUploadDTO dto = new BulkUploadDTO();
        when(bulkUploadService.processUpload(any(InputStream.class))).thenReturn(dto);

        MockMultipartFile file = new MockMultipartFile("file", new byte[] { 1, 2, 3 });
        controller.bulkUpload(file);

        verify(bulkUploadService, times(1)).processUpload(any(InputStream.class));
    }
}
