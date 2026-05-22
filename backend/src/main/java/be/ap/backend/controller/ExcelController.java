package be.ap.backend.controller;

import java.io.IOException;

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

import be.ap.backend.dto.BulkPreviewDTO;
import be.ap.backend.dto.BulkUploadDTO;
import be.ap.backend.service.BookBulkUploadService;
import be.ap.backend.util.ExcelTemplateBuilder;

@RestController
@RequestMapping("/excel/book")
public class ExcelController {

    private final ExcelTemplateBuilder templateBuilder;
    private final BookBulkUploadService bulkUploadService;

    public ExcelController(ExcelTemplateBuilder templateBuilder, 
                          BookBulkUploadService bulkUploadService) {
        this.templateBuilder = templateBuilder;
        this.bulkUploadService = bulkUploadService;
    }

    @GetMapping("template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] xlsx = templateBuilder.buildTemplateXlsx();
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=book-upload-template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(xlsx);
        } catch (IOException e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "Could not generate template", e);
        }
    }

    @PostMapping("bulk-preview")
    public ResponseEntity<BulkPreviewDTO> bulkPreview(@RequestParam("file") MultipartFile file) {
        try {
            BulkPreviewDTO result = bulkUploadService.generatePreview(file.getInputStream());
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "Kon bestand niet lezen", e);
        }
    }

    @PostMapping("bulk-upload")
    public ResponseEntity<BulkUploadDTO> bulkUpload(@RequestParam("file") MultipartFile file) {
        try {
            BulkUploadDTO result = bulkUploadService.processUpload(file.getInputStream());
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "Kon bestand niet lezen", e);
        }
    }
}