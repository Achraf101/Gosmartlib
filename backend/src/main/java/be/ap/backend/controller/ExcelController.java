package be.ap.backend.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import be.ap.backend.dto.BulkPreviewDTO;
import be.ap.backend.dto.BulkUploadDTO;
import be.ap.backend.service.BookBulkUploadService;
import be.ap.backend.util.ExcelTemplateBuilder;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for Excel-based book import functionality.
 *
 * <p>
 * Provides endpoints for downloading a template, generating a bulk upload
 * preview,
 * and executing a bulk upload of books via Excel files.
 * </p>
 */
@RestController
@RequestMapping("/excel/book")
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelTemplateBuilder templateBuilder;
    private final BookBulkUploadService bulkUploadService;

    /**
     * Downloads an Excel template for bulk book uploads.
     *
     * @return Excel file as byte array
     * @throws IOException if template generation fails
     */
    @GetMapping("template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        byte[] xlsx = templateBuilder.buildTemplateXlsx();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=book-upload-template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(xlsx);
    }

    /**
     * Generates a preview of a bulk book upload from an Excel file.
     *
     * <p>
     * Used to validate and inspect data before actual import.
     * </p>
     *
     * @param file uploaded Excel file
     * @return preview result containing parsed and validated data
     * @throws IOException if file reading fails
     */
    @PostMapping("bulk-preview")
    public ResponseEntity<BulkPreviewDTO> bulkPreview(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(bulkUploadService.generatePreview(file.getInputStream()));
    }

    /**
     * Processes a bulk book upload from an Excel file.
     *
     * <p>
     * Persists valid records and returns upload results.
     * </p>
     *
     * @param file uploaded Excel file
     * @return upload result summary
     * @throws IOException if file reading fails
     */
    @PostMapping("bulk-upload")
    public ResponseEntity<BulkUploadDTO> bulkUpload(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(bulkUploadService.processUpload(file.getInputStream()));
    }
}