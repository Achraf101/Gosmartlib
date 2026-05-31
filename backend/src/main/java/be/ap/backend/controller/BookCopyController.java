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

/**
 * REST controller for managing physical book copies.
 *
 * <p>
 * Provides endpoints to retrieve, update, and delete book copies
 * identified by accession ID or location-book relation.
 * </p>
 */
@RestController
@RequestMapping("bookcopy")
@RequiredArgsConstructor
public class BookCopyController {

    private final BookCopyService bookCopyService;
    private final SessionContext sessionContext;

    /**
     * Retrieves a book copy by its accession identifier.
     *
     * @param accessionId unique accession identifier
     * @return detailed book copy information
     */
    @GetMapping("/by-accession/{accessionId}")
    public ResponseEntity<BookCopyDetailDTO> getByAccessionId(@PathVariable String accessionId) {
        return ResponseEntity.ok(bookCopyService.findByAccessionId(accessionId));
    }

    /**
     * Retrieves all book copies linked to a specific location-book entry.
     *
     * @param locationBookId location-book relationship identifier
     * @return list of book copies
     */
    @GetMapping("/locationbook/{locationBookId}")
    public ResponseEntity<List<BookCopyDetailDTO>> getByLocationBook(@PathVariable Long locationBookId) {
        return ResponseEntity.ok(bookCopyService.findByLocationBookId(locationBookId));
    }

    /**
     * Updates the status of a book copy.
     *
     * <p>
     * Only users with BIBLIOTHEEKBEHEERDER or ADMIN role are allowed.
     * </p>
     *
     * @param id   book copy identifier
     * @param body request body containing the new status
     * @return updated book copy
     * @throws UnauthorizedAccessException if user lacks permission
     */
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

    /**
     * Deletes a book copy by its accession identifier.
     *
     * <p>
     * Only users with BIBLIOTHEEKBEHEERDER or ADMIN role are allowed.
     * </p>
     *
     * @param accessionId unique accession identifier
     * @return no content response
     * @throws UnauthorizedAccessException if user lacks permission
     */
    @DeleteMapping("/by-accession/{accessionId}")
    public ResponseEntity<Void> deleteByAccessionId(@PathVariable String accessionId) {
        if (!sessionContext.hasRole(UserRole.BIBLIOTHEEKBEHEERDER) && !sessionContext.hasRole(UserRole.ADMIN)) {
            throw new UnauthorizedAccessException("Geen toegang.");
        }
        bookCopyService.deleteCopy(accessionId);
        return ResponseEntity.noContent().build();
    }
}
