package be.ap.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.LoanDTO;
import be.ap.backend.dto.TopBookDTO;
import be.ap.backend.dto.UpdateNoteDTO;
import be.ap.backend.dto.UpdateStatusDTO;

import java.util.Map;
import be.ap.backend.enums.LoanStatus;
import be.ap.backend.exception.MissingSessionException;
import be.ap.backend.service.LoanService;
import lombok.AllArgsConstructor;

/**
 * REST controller for managing loans.
 * Handles loan creation, lifecycle updates (pickup, return, extend),
 * and reporting endpoints such as overdue, due-soon, and statistics.
 */
@RestController
@AllArgsConstructor
@RequestMapping("loan")
public class LoanController {
    private final LoanService loanService;
    private final SessionContext sessionContext;

    /**
     * Retrieves all requested loans.
     *
     * @return list of requested loans
     */
    @GetMapping("/requested")
    public ResponseEntity<List<LoanDTO>> getRequested() {
        return ResponseEntity.ok(loanService.getRequested());
    }

    /**
     * Creates a new loan for the currently logged-in user.
     *
     * @param dto loan data transfer object
     * @return created loans
     */
    @PostMapping
    public List<LoanDTO> createLoan(@RequestBody LoanDTO dto) {
        Object raw = sessionContext.getUserId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long userId = Long.valueOf(raw.toString());
        dto.setUserId(userId);
        return loanService.createLoan(dto);
    }

    /**
     * Updates the note of a loan.
     *
     * @param id  loan id
     * @param dto note payload
     * @return updated loan
     */
    @PutMapping("/{id}/note")
    public ResponseEntity<LoanDTO> updateNote(@PathVariable Long id, @RequestBody UpdateNoteDTO dto) {
        return ResponseEntity.ok(loanService.updateNote(id, dto.note()));
    }

    /**
     * Updates the status of a loan.
     *
     * @param id  loan id
     * @param dto status payload
     * @return updated loan
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<LoanDTO> updateStatus(@PathVariable Long id, @RequestBody UpdateStatusDTO dto) {
        return ResponseEntity.ok(loanService.updateStatus(id, dto.status()));
    }

    /**
     * Scans a book copy during pickup.
     *
     * @param id   loan id
     * @param body request body containing book_copy_id
     * @return updated loan
     */
    @PutMapping("/{id}/scan-pickup")
    public ResponseEntity<LoanDTO> scanPickup(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long bookCopyId = Long.valueOf(body.get("book_copy_id").toString());
        return ResponseEntity.ok(loanService.scanPickup(id, bookCopyId));
    }

    /**
     * Scans a returned book copy.
     *
     * @param id   loan id
     * @param body request body containing book_copy_id, optional note and damaged
     *             flag
     * @return updated loan
     */
    @PutMapping("/{id}/scan-return")
    public ResponseEntity<LoanDTO> scanReturn(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long bookCopyId = Long.valueOf(body.get("book_copy_id").toString());
        String note = body.get("note") != null ? body.get("note").toString() : null;
        boolean damaged = Boolean.TRUE.equals(body.get("damaged"));
        return ResponseEntity.ok(loanService.scanReturn(id, bookCopyId, note, damaged));
    }

    /**
     * Marks a loan as picked up.
     *
     * @param id   loan id
     * @param body optional request body containing book_copy_id
     * @return updated loan
     */
    @PutMapping("/{id}/pickup")
    public ResponseEntity<LoanDTO> pickupLoan(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {
        System.out.println("body: " + body);
        Long bookCopyId = null;
        if (body != null && body.get("book_copy_id") != null) {
            bookCopyId = Long.valueOf(body.get("book_copy_id").toString());
        }
        return ResponseEntity.ok(loanService.pickupLoan(id, bookCopyId));
    }

    /**
     * Retrieves all loans for the current user.
     *
     * @return list of user loans
     */
    @GetMapping("/user")
    public ResponseEntity<List<LoanDTO>> getByUserId() {
        Object raw = sessionContext.getUserId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long userId = Long.valueOf(raw.toString());
        return ResponseEntity.ok(loanService.getByUserId(userId));
    }

    /**
     * Extends a loan duration.
     *
     * @param id loan id
     * @return updated loan
     */
    @PutMapping("/{id}/extend")
    public ResponseEntity<LoanDTO> extendLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.extendLoan(id));
    }

    /**
     * Deletes a loan.
     *
     * @param id loan id
     * @return empty response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves number of overdue loans for the current school.
     *
     * @return count of overdue loans
     */
    @GetMapping("/overdue/length")
    public ResponseEntity<Integer> getOverdueLoansLength() {
        Object raw = sessionContext.getSchoolId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long schoolId = Long.valueOf(raw.toString());
        return ResponseEntity.ok(loanService.getOverdueLoansLength(schoolId));
    }

    /**
     * Retrieves all overdue loans for the current school.
     *
     * @return list of overdue loans
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<LoanDTO>> getOverdueLoans() {
        Object raw = sessionContext.getSchoolId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long schoolId = Long.valueOf(raw.toString());
        return ResponseEntity.ok(loanService.getOverdueLoans(schoolId));
    }

    /**
     * Retrieves top borrowed books for the current month.
     *
     * @return list of top books
     */
    @GetMapping("/top-books")
    public ResponseEntity<List<TopBookDTO>> getTopBooksThisMonth() {
        Object raw = sessionContext.getSchoolId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long schoolId = Long.valueOf(raw.toString());
        return ResponseEntity.ok(loanService.getTopBooksThisMonth(schoolId));
    }

    /**
     * Retrieves number of loans due soon.
     *
     * @return count of due soon loans
     */
    @GetMapping("/due-soon/length")
    public ResponseEntity<Integer> getDueSoonLoansLength() {
        Object raw = sessionContext.getSchoolId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long schoolId = Long.valueOf(raw.toString());
        return ResponseEntity.ok(loanService.getDueSoonLoansLength(schoolId));
    }

    /**
     * Retrieves loans that are due soon.
     *
     * @return list of due soon loans
     */
    @GetMapping("/due-soon")
    public ResponseEntity<List<LoanDTO>> getDueSoonLoans() {
        Object raw = sessionContext.getSchoolId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long schoolId = Long.valueOf(raw.toString());
        return ResponseEntity.ok(loanService.getDueSoonLoans(schoolId));
    }

    /**
     * Retrieves top genres for the current month.
     *
     * @return list of top genres
     */
    @GetMapping("/top-genres")
    public ResponseEntity<List<TopBookDTO>> getTopGenresThisMonth() {
        Object raw = sessionContext.getSchoolId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long schoolId = Long.valueOf(raw.toString());
        return ResponseEntity.ok(loanService.getTopGenresThisMonth(schoolId));
    }

    /**
     * Retrieves loans by status for the current school.
     *
     * @param state loan status
     * @return filtered loans
     */
    @GetMapping("/state/{state}")
    public ResponseEntity<List<LoanDTO>> getByStateAndSchool(@PathVariable LoanStatus state) {
        Object raw = sessionContext.getSchoolId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long schoolId = Long.valueOf(raw.toString());
        return ResponseEntity.ok(loanService.getByStateAndSchool(state, schoolId));
    }

}