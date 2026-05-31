package be.ap.backend.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import be.ap.backend.dto.CoverDTO;
import be.ap.backend.entity.Material;
import be.ap.backend.service.UploadService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * REST controller responsible for file uploads related to books.
 *
 * <p>
 * Supports uploading book covers and supplementary materials (e.g. PDFs,
 * files).
 * Delegates storage and persistence logic to {@link UploadService}.
 * </p>
 */
@RestController
@RequestMapping("upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    /**
     * Uploads a cover image for a specific book.
     *
     * @param file   the uploaded file
     * @param bookId the target book ID
     * @return the stored cover metadata
     */
    @PostMapping("cover")
    public ResponseEntity<CoverDTO> uploadCover(@RequestParam("file") MultipartFile file,
            @RequestParam("book_id") Long bookId) {
        CoverDTO cover = uploadService.saveCover(file, bookId);
        if (cover.bookId == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(cover);
    }

    /**
     * Uploads supplementary material for a book.
     *
     * @param file   the uploaded file
     * @param bookId the target book ID
     * @param note   optional note describing the material
     * @return persisted material entity
     */
    @PostMapping("material")
    public ResponseEntity<Material> uploadMaterial(@RequestParam("file") MultipartFile file,
            @RequestParam("book_id") Long bookId,
            @RequestParam(value = "note", required = false) String note) {
        Material material = uploadService.saveMaterial(file, bookId, note);

        return ResponseEntity.ok(material);
    }
}
