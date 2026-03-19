package be.ap.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import be.ap.backend.dto.SchoolDTO;
import be.ap.backend.service.SchoolService;

@RestController
@RequestMapping("school")
public class SchoolController {
    private final SchoolService schoolService;

    public SchoolController(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @PostMapping
    public ResponseEntity<SchoolDTO> addSchool(@RequestBody SchoolDTO dto) {
        return ResponseEntity.ok(schoolService.addSchool(dto));
    }

    @GetMapping
    public ResponseEntity<List<SchoolDTO>> getAll() {
        return ResponseEntity.ok(schoolService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SchoolDTO> findById(@PathVariable Long id) {
        return schoolService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
