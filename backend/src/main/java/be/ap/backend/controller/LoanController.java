package be.ap.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.dto.LoanDTO;
import be.ap.backend.dto.UpdateNoteDTO;
import be.ap.backend.dto.UpdateStatusDTO;
import be.ap.backend.service.LoanService;

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
    public LoanDTO createLoan(@RequestBody LoanDTO dto) {
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
}
