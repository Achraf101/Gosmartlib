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

import be.ap.backend.dto.LoanDTO;
import be.ap.backend.dto.TopBookDTO;
import be.ap.backend.dto.UpdateNoteDTO;
import be.ap.backend.dto.UpdateStatusDTO;
import be.ap.backend.enums.LoanStatus;
import be.ap.backend.service.LoanService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("loan")
public class LoanController {
    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping("/requested")
    public ResponseEntity<List<LoanDTO>> getRequested() {
        return ResponseEntity.ok(loanService.getRequested());
    }

    @PostMapping
    public List<LoanDTO> createLoan(@RequestBody LoanDTO dto, HttpSession session) {
        Object raw = session.getAttribute("userId");
        Long userId = (raw != null) ? Long.valueOf(raw.toString()) : null;
        dto.setUserId(userId);
        return loanService.createLoan(dto);
    }

    @PutMapping("/{id}/note")
    public ResponseEntity<LoanDTO> updateNote(@PathVariable Long id, @RequestBody UpdateNoteDTO dto) {
        return ResponseEntity.ok(loanService.updateNote(id, dto.note()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<LoanDTO> updateStatus(@PathVariable Long id, @RequestBody UpdateStatusDTO dto) {
        return ResponseEntity.ok(loanService.updateStatus(id, dto.status()));
    }

    @GetMapping("/user")
    public ResponseEntity<List<LoanDTO>> getByUserId(HttpSession session) {
        Object raw = session.getAttribute("userId");
        Long userId = (raw != null) ? Long.valueOf(raw.toString()) : null;
        return ResponseEntity.ok(loanService.getByUserId(userId));
    }

    @PutMapping("/{id}/extend")
    public ResponseEntity<?> extendLoan(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(loanService.extendLoan(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/overdue/length")
    public ResponseEntity<Integer> getOverdueLoansLength(HttpSession session) {
        Long schoolId = Long.valueOf(session.getAttribute("school").toString());
        return ResponseEntity.ok(loanService.getOverdueLoansLength(schoolId));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<LoanDTO>> getOverdueLoans(HttpSession session) {
        Long schoolId = Long.valueOf(session.getAttribute("school").toString());
        return ResponseEntity.ok(loanService.getOverdueLoans(schoolId));
    }

    @GetMapping("/top-books")
    public ResponseEntity<List<TopBookDTO>> getTopBooksThisMonth(HttpSession session) {
        Long schoolId = Long.valueOf(session.getAttribute("school").toString());
        return ResponseEntity.ok(loanService.getTopBooksThisMonth(schoolId));
    }

    @GetMapping("/due-soon/length")
    public ResponseEntity<Integer> getDueSoonLoansLength(HttpSession session) {
        Long schoolId = Long.valueOf(session.getAttribute("school").toString());
        return ResponseEntity.ok(loanService.getDueSoonLoansLength(schoolId));
    }

    @GetMapping("/due-soon")
    public ResponseEntity<List<LoanDTO>> getDueSoonLoans(HttpSession session) {
        Long schoolId = Long.valueOf(session.getAttribute("school").toString());
        return ResponseEntity.ok(loanService.getDueSoonLoans(schoolId));
    }

    @GetMapping("/top-genres")
    public ResponseEntity<List<TopBookDTO>> getTopGenresThisMonth(HttpSession session) {
        Long schoolId = Long.valueOf(session.getAttribute("school").toString());
        return ResponseEntity.ok(loanService.getTopGenresThisMonth(schoolId));
    }

    @GetMapping("/state/{state}")
    public ResponseEntity<List<LoanDTO>> getByStateAndSchool(@PathVariable LoanStatus state, HttpSession session) {
        Long schoolId = Long.valueOf(session.getAttribute("school").toString());
        return ResponseEntity.ok(loanService.getByStateAndSchool(state, schoolId));
    }

}