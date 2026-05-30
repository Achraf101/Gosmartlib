package be.ap.backend.controller;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.BookCopyDetailDTO;
import be.ap.backend.entity.UserRole;
import be.ap.backend.enums.CopyStatus;
import be.ap.backend.exception.UnauthorizedAccessException;
import be.ap.backend.service.BookCopyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("bookcopy")
@RequiredArgsConstructor
public class BookCopyController {

    private final BookCopyService bookCopyService;
    private final SessionContext sessionContext;

    @GetMapping("/by-accession/{accessionId}")
    public ResponseEntity<BookCopyDetailDTO> getByAccessionId(@PathVariable String accessionId) {
        return ResponseEntity.ok(bookCopyService.findByAccessionId(accessionId));
    }

    @GetMapping("/locationbook/{locationBookId}")
    public ResponseEntity<List<BookCopyDetailDTO>> getByLocationBook(@PathVariable Long locationBookId) {
        return ResponseEntity.ok(bookCopyService.findByLocationBookId(locationBookId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<BookCopyDetailDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        if (!sessionContext.hasRole(UserRole.BIBLIOTHEEKBEHEERDER) && !sessionContext.hasRole(UserRole.ADMIN)) {
            throw new UnauthorizedAccessException("Geen toegang.");
        }
        CopyStatus status = CopyStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(bookCopyService.updateStatus(id, status));
    }

    @DeleteMapping("/by-accession/{accessionId}")
    public ResponseEntity<Void> deleteByAccessionId(@PathVariable String accessionId) {
        if (!sessionContext.hasRole(UserRole.BIBLIOTHEEKBEHEERDER) && !sessionContext.hasRole(UserRole.ADMIN)) {
            throw new UnauthorizedAccessException("Geen toegang.");
        }
        bookCopyService.deleteCopy(accessionId);
        return ResponseEntity.noContent().build();
    }
}
