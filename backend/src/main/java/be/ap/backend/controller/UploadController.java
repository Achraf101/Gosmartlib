package be.ap.backend.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import be.ap.backend.dto.CoverDTO;
import be.ap.backend.service.UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("upload")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("cover")
    public ResponseEntity<CoverDTO> uploadCover(@RequestParam("file") MultipartFile file,
            @RequestParam("book_id") Long bookId) {
        CoverDTO cover = uploadService.saveCover(file, bookId);
        if (cover.bookId == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(cover);
    }

}
