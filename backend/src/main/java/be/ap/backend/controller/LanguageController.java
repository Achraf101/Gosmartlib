package be.ap.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.entity.Language;
import be.ap.backend.service.LanguageService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("language")
@RequiredArgsConstructor
public class LanguageController {

    private final LanguageService languageService;

    @GetMapping
    public ResponseEntity<List<Language>> getAll() {
        return ResponseEntity.ok(languageService.getAll());
    }

    @GetMapping("/search/{query}")
    public ResponseEntity<List<Language>> search(@PathVariable String query) {
        return ResponseEntity.ok(languageService.search(query));
    }

    @PostMapping
    public ResponseEntity<Language> addLanguage(@RequestBody Language language) {
        return ResponseEntity.ok(languageService.addLanguage(language));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Language> findByCode(@PathVariable String code) {
        return ResponseEntity.ok(languageService.findByCode(code));
    }
}
