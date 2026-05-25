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

@RestController
@RequestMapping("/excel/book")
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelTemplateBuilder templateBuilder;
    private final BookBulkUploadService bulkUploadService;

    @GetMapping("template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        byte[] xlsx = templateBuilder.buildTemplateXlsx();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=book-upload-template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(xlsx);
    }

    @PostMapping("bulk-preview")
    public ResponseEntity<BulkPreviewDTO> bulkPreview(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(bulkUploadService.generatePreview(file.getInputStream()));
    }

    @PostMapping("bulk-upload")
    public ResponseEntity<BulkUploadDTO> bulkUpload(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(bulkUploadService.processUpload(file.getInputStream()));
    }
}